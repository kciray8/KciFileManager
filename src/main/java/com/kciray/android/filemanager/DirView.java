/****************************************************************************
 **
 ** KciFileManager is open file manager for Android, quick and convenient
 ** Copyright (C) 2014 Yaroslav (aka KciRay).
 ** Contact: Yaroslav (kciray8@gmail.com)
 **
 ** This program is free software: you can redistribute it and/or modify
 ** it under the terms of the GNU General Public License as published by
 ** the Free Software Foundation, either version 3 of the License, or
 ** (at your option) any later version.
 **
 ** This program is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ** GNU General Public License for more details.
 **
 ** You should have received a copy of the GNU General Public License
 ** along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **
 *****************************************************************************/

package com.kciray.android.filemanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.kciray.android.commons.gui.DialogUtils;
import com.kciray.android.commons.gui.ToastUtils;
import com.kciray.android.commons.gui.ViewUtils;
import com.kciray.android.commons.sys.Global;
import com.kciray.android.commons.sys.KLog;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.sys.root.FileMgr;
import com.kciray.commons.io.ExFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScrollPosition {
    private int position;
    private int y;

    public ScrollPosition(ListView list) {
        position = list.getFirstVisiblePosition();
        y = list.getChildAt(0).getTop();
    }

    public void restore(final ListView list) {
        list.post(() -> list.setSelectionFromTop(position, y));
    }

    @Override
    public String toString() {
        return "[pos=" + position + ",y=" + y + "]";
    }
}

public class DirView extends FrameLayout implements AbsListView.OnScrollListener {
    public ExFile getDirectory() {
        return directory;
    }

    private ExFile directory;
    private ExFile newDirectory;
    private Context context;
    private DirViewAdapter adapter;

    public ListView getListView() {
        return listView;
    }

    private FileView listView;
    private TextView statusView;
    Activity activity;
    DirElement backNavElement;
    View mainLayout;

    public ImageButton getAddBookmarkButton() {
        return addBookmarkButton;
    }

    private ImageButton addBookmarkButton;

    static Map<String, ScrollPosition> pathToScroll = new HashMap<>();

    public DirView(Activity activity, String dir) {
        super(activity);
        this.context = activity;
        this.activity = activity;

        mainLayout = ViewUtils.viewFromRes(R.layout.main_layout);
        listView = (FileView) mainLayout.findViewById(R.id.fileView);
        statusView = (TextView) mainLayout.findViewById(R.id.status_view);

        adapter = new DirViewAdapter(context);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            DirElement dirElement = adapter.getItem(position);

            if (dirElement.isBackButton()) {
                goUp();
            } else if (!dirElement.isThisButton()) {
                ExFile dirElementFile = dirElement.getFile();
                if (dirElementFile.isDir()) {
                    goToDir(dirElementFile);
                }
            }
        });

        activity.registerForContextMenu(listView);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        addView(mainLayout);

        View bottomBar = mainLayout.findViewById(R.id.bottom_bar);
        addBookmarkButton = (ImageButton) bottomBar.findViewById(R.id.add_bookmark_button);
        BookmarkManager.getInstance().setAddBookmarkButton(addBookmarkButton);
        addBookmarkButton.setOnClickListener(v -> {
            if (BookmarkManager.getInstance().getBookmark(directory) == null) {
                DialogUtils.inputString(L.tr(R.string.input_bookmark_name), directory.getName(),
                        str -> {
                            BookmarkManager.getInstance().addBookmark(str, directory.getFullPath());
                        }
                );
            } else {
                DialogUtils.askQuestion(L.tr(R.string.confirm), L.tr(R.string.delete_bookmark_q), () -> {
                    BookmarkManager.getInstance().deleteBookmark(directory.getFullPath());
                });
            }
        });

        directory = FileMgr.getFile(dir);
    }

    public void goToDir(ExFile directory) {
        newDirectory = directory;
        rebuildDir();
    }

    public void goUp() {
        if (directory.getParent() != null) {
            goToDir(directory.getParent());
        }
    }

    public void setStatus(String str) {
        statusView.setText(str);
    }

    public void rebuildDir() {
        newDirectory = FileMgr.getFile(newDirectory.getFullPath());//Renovate temp dir (StdDir or ShellDir)
        newDirectory.loadDirInfo(() -> {
            newDirectory.getSubDirsAsync(list -> {
                if (list != null) {
                    directory = newDirectory;

                    Global.getContext().runOnUiThread(() -> {
                        adapter.clear();

                        if (directory.getParent() != null) {
                            backNavElement = DirElement.getBackNavElement(context, directory.getParent(), this);
                            adapter.addElement(backNavElement);
                        }

                        DirElement thisNavElement = DirElement.getThisNavElement(context, directory, this);
                        adapter.addElement(thisNavElement);

                        for (ExFile dirElementFile : list) {
                            DirElement dirElement;

                            if (FileScanner.cachedView(dirElementFile.getFullPath())) {
                                dirElement = FileScanner.getCachedView(dirElementFile.getFullPath());
                            } else {
                                dirElement = new DirElement(context, dirElementFile, this);
                                FileScanner.addToCache(dirElementFile.getFullPath(), dirElement);
                            }

                            adapter.addElement(dirElement);
                        }

                        statusView.setText(directory.getFullPath());
                        adapter.autoSort();
                        adapter.notifyDataSetChanged();

                        BookmarkManager.getInstance().updateBookmarkButton(directory);
                        MainActivity.addToHistory(directory);
                    });
                } else {
                    ToastUtils.show(getContext().getString(R.string.access_denied));
                    newDirectory = null;
                }
            });
        });
    }

    public void addNewFolder() {
        DialogUtils.inputString(L.tr(R.string.enter_name_new_folder), str -> {
            ExFile newFile = FileMgr.getFile(directory, str);
            newFile.makeDir(success -> {
                if (success) {
                    newFile.loadDirInfo(() -> {
                        Global.getContext().runOnUiThread(() -> {
                            dynamicallyAddFile(newFile);
                        });
                    });
                }
            });
        });
    }

    private void dynamicallyAddFile(ExFile file) {
        DirElement dirElement = new DirElement(context, file, this);
        FileScanner.addToCache(file.getFullPath(), dirElement);
        adapter.addElement(dirElement);
        adapter.notifyDataSetChanged();
        adapter.autoSort();
    }

    private void dynamicallyRemoveDirElement(DirElement dirElement) {
        if (!dirElement.isBackButton()) {
            adapter.removeElement(dirElement);
            adapter.notifyDataSetChanged();
        } else {
            findAndDeleteViewWithFile(dirElement.getFile());
        }
    }

    public void handleContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            DirElement element = adapter.getItem(info.position);

            ExFile file = element.getFile();
            showActionsForFile(file, menu);
        }
    }

    private void showActionsForFile(ExFile file, ContextMenu menu) {
        boolean isRoot = file.getFullPath().equals("/");

        menu.setHeaderTitle(L.tr(R.string.actions));
        menu.setHeaderIcon(R.drawable.info);

        if (!isRoot) {
            menu.add(Menu.NONE, FileMenu.OPEN_INTENT.ordinal(),
                    Menu.NONE, L.tr(R.string.open_in_other_program));
        }

        if (!isRoot) {
            menu.add(Menu.NONE, FileMenu.DELETE.ordinal(),
                    Menu.NONE, L.tr(R.string.action_delete));
        }

        if (!file.isDir()) {
            menu.add(Menu.NONE, FileMenu.PROPERTIES.ordinal(),
                    Menu.NONE, L.tr(R.string.action_properties));
        }

        if (!isRoot) {
            menu.add(Menu.NONE, FileMenu.RENAME.ordinal(),
                    Menu.NONE, getContext().getString(R.string.rename));
        }

        if ((file != null) && (file.isDir())) {
            menu.add(Menu.NONE, FileMenu.CALC_SIZE.ordinal(),
                    Menu.NONE, getContext().getString(R.string.calc_size));
        }

        menu.add(Menu.NONE, FileMenu.SEND_TO_HOME_SCREEN.ordinal(),
                Menu.NONE, getContext().getString(R.string.create_shortcut));

        menu.add(Menu.NONE, FileMenu.COPY_FULL_PATH.ordinal(),
                Menu.NONE, getContext().getString(R.string.copy_full_path));
    }

    public void deleteItem(int position) {
        final DirElement dirElement = adapter.getItem(position);
        String fileName = dirElement.getFile().getName();

        DialogUtils.askQuestion(L.tr(R.string.confirm), String.format(L.tr(R.string.confirm_delete_file), fileName),
                () -> {
                    if (dirElement.isBackButton()) {
                        goUp();
                    }
                    boolean success = recursiveDelete(new File(dirElement.getFile().getFullPath()));
                    if (success) {
                        dynamicallyRemoveDirElement(dirElement);
                    } else {
                        ToastUtils.show(L.tr(R.string.error_delete_file));
                    }
                }
        );
    }

    private void findAndDeleteViewWithFile(ExFile file) {
        for (DirElement element : adapter.elements) {
            if (element.getFile().getFullPath().equals(file.getFullPath())) {
                adapter.removeElement(element);
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void calcFolderSize(int position) {
        DirElement dirElement = adapter.getItem(position);
        ExFile file = dirElement.getFile();
        boolean isRoot = file.getFullPath().equals("/");
        String dirName = isRoot ? "/" : dirElement.getFile().getName();

        final ProgressDialog progressDialog = DialogUtils.showProgressDialog(
                "Подсчёт размера для папки " + dirName);

        file.getDirSize(dirSize -> {
            final String strSize = String.format("%,d %s", dirSize, "[Byte]");
            KLog.v("&77777");
            activity.runOnUiThread(() -> {
                progressDialog.cancel();
                DialogUtils.showMessage("Размер директории:", strSize);
            });
        }, () -> {//On deny
            activity.runOnUiThread(() -> {
                progressDialog.cancel();
                DialogUtils.showMessage("Размер директории:", "Требуются root-права!");
            });
        });
    }

    public void openIntent(int position) {
        final DirElement dirElement = adapter.getItem(position);
        ExFile file = dirElement.getFile();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file.toFile()));
        MainActivity.getInstance().startActivity(intent);
    }

    public void sendToHomeScreen(int position) {
        final DirElement dirElement = adapter.getItem(position);
        ExFile file = dirElement.getFile();

        Intent shortcutIntent = new Intent(getContext(), MainActivity.class);
        //TODO fix bug - when I two times click on shortcut (And app not executing) - onCreate not call
        shortcutIntent.putExtra(MainActivity.EXTRA_FILE_PATH, file.getFullPath());

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getContext(), R.drawable.folder));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        getContext().sendBroadcast(addIntent);
        ToastUtils.show(getContext().getString(R.string.create_shortcut_wait));
    }

    boolean recursiveDelete(File f) {
        if (f.isDirectory()) {
            File[] listFiles = f.listFiles();
            if (listFiles != null)
                for (File c : listFiles)
                    recursiveDelete(c);
        }
        return f.delete();
    }

    public void showProp(int position) {
        DirElement dirElement = adapter.getItem(position);

        String message = L.tr(R.string.size) + " = " + dirElement.getFile().getSize() + " " + L.tr(R.string.bytes);
        DialogUtils.showMessage(L.tr(R.string.action_properties), message);
    }

    public void renameItem(int itemIndex) {
        DirElement dirElement = adapter.getItem(itemIndex);
        dirElement.rename();
        adapter.autoSort();
    }

    public void showRootActions() {

    }

    public void updateRootDirectory(ExFile file) {
        directory = file;
        setStatus(directory.getFullPath());
        //Update file path
        for (DirElement dirElement : adapter.elements) {
            ExFile exFile = FileMgr.getFile(directory.getFullPath());
            dirElement.setFile(exFile.append(dirElement.getFile().getName()));
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            String path = directory.getFullPath();
            pathToScroll.put(path, new ScrollPosition(listView));
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }


    public void refresh() {
        FileScanner.clear();
        goToDir(directory);
    }

    public void copyFullPath(int position) {
        final DirElement dirElement = adapter.getItem(position);
        ExFile file = dirElement.getFile();
        String fullPath = file.getFullPath();

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(fullPath);

        ToastUtils.show(String.format(getContext().getString(R.string.path_was_copy), fullPath));
    }
}

class DirViewAdapter extends BaseAdapter {
    List<DirElement> elements = new ArrayList<>();
    private Context context;

    DirViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public DirElement getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return elements.get(position).getView();
    }

    public void addElement(DirElement element) {
        elements.add(element);
    }

    public void removeElement(DirElement element) {
        elements.remove(element);
    }

    public void clear() {
        elements.clear();
    }

    public void autoSort() {
        SharedPreferences mainPref = MainActivity.getInstance().getMainPref();
        String name = MainActivity.getInstance().getResources().getString(R.string.autoSort);
        boolean autoSort = mainPref.getBoolean(name, true);
        if (autoSort) {
            sort();
        }
    }

    public void sort() {
        Collections.sort(elements, DirElement.getComparator());
    }
}