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
import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kciray.Q;
import com.kciray.android.Common;
import com.kciray.android.L;
import com.kciray.android.OnInputListener;
import com.kciray.android.commons.io.FileUtils;
import com.kciray.android.gui.GUI;

import java.io.File;
import java.io.IOException;
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
        list.post(new Runnable() {
            @Override
            public void run() {
                list.setSelectionFromTop(position, y);
            }
        });
    }

    @Override
    public String toString() {
        return "[pos=" + position + ",y=" + y + "]";
    }
}

public class DirView extends LinearLayout implements AbsListView.OnScrollListener {
    public File getDirectory() {
        return directory;
    }

    private File directory;
    private Context context;
    private DirViewAdapter adapter;

    public ListView getListView() {
        return listView;
    }

    private FileView listView;
    private TextView statusView;
    Activity activity;
    DirElement backNavElement;

    static Map<String, ScrollPosition> pathToScroll = new HashMap<>();

    public DirView(Activity activity, String dir) {
        super(activity);
        this.context = activity;
        this.activity = activity;
        listView = new FileView(context);

        statusView = (TextView) GUI.viewFromRes(R.layout.status_bar);

        setDirectory(dir);

        adapter = new DirViewAdapter(context);
        rebuildDir();

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                DirElement dirElement = adapter.getItem(position);

                if (dirElement.isBackButton()) {
                    goUp();
                } else {
                    File dirElementFile = dirElement.getFile();
                    if (dirElementFile.isDirectory()) {
                        dirElementFile.listFiles();

                        if (dirElementFile.listFiles() != null) {
                            directory = dirElementFile;
                            rebuildDir();
                        } else {
                            GUI.toast(L.tr(R.string.error_open_folder));
                        }
                    }
                }

            }
        });

        activity.registerForContextMenu(listView);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        setOrientation(VERTICAL);

        addView(statusView);
        addView(listView);
    }

    public void goUp() {
        if (directory.getParentFile() != null) {
            directory = directory.getParentFile();
            rebuildDir();
        }
    }

    public void setStatus(String str) {
        statusView.setText(str);
    }

    private void rebuildDir() {
        adapter.clear();
        backNavElement = DirElement.getBackNavElement(context, directory, this);
        adapter.addElement(backNavElement);

        File[] subFiles = null;
        subFiles = directory.listFiles();

        if (subFiles != null) {
            for (File dirElementFile : subFiles) {
                DirElement dirElement;

                if (FileScanner.cachedView(dirElementFile)) {
                    dirElement = FileScanner.getCachedView(dirElementFile);
                } else {
                    dirElement = new DirElement(context, dirElementFile, this);
                    FileScanner.addToCache(dirElementFile, dirElement);
                }

                adapter.addElement(dirElement);
            }
        }

        statusView.setText(directory.toString());
        adapter.autoSort();
        adapter.notifyDataSetChanged();

        if (pathToScroll.containsKey(directory.getAbsolutePath())) {
            ScrollPosition scrollPosition = pathToScroll.get(directory.getAbsolutePath());
            //scrollPosition.restore(listView);//Intermitted!!!
            //TODO - fix!
        }
    }

    public void setDirectory(String directory) {
        this.directory = new File(directory);
    }

    public void addNewFolder() {
        GUI.inputString(L.tr(R.string.enter_name_new_folder), new OnInputListener() {
            @Override
            public void onInput(String str) {
                File newFile = new File(directory, str);
                boolean success = newFile.mkdir();

                if (success) {
                    dynamicallyAddFile(newFile);
                }
            }
        });
    }

    private void dynamicallyAddFile(File file) {
        DirElement dirElement = new DirElement(context, file, this);
        FileScanner.addToCache(file, dirElement);
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

    public void addNewFile() {
        GUI.inputString(L.tr(R.string.enter_name_new_file), new OnInputListener() {
            @Override
            public void onInput(String str) {
                File newFile = new File(directory, str);
                boolean success = false;
                try {
                    success = newFile.createNewFile();
                    if (success) {
                        dynamicallyAddFile(newFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void handleContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v == listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            DirElement element = adapter.getItem(info.position);
            if (element.isBackButton()) {
                File file = element.getFile();
                showActionsForFile(file, menu);
            } else {
                File file = element.getFile();
                showActionsForFile(file, menu);
            }
        }
    }

    private void showActionsForFile(File file, ContextMenu menu) {
        boolean isRoot = file.getAbsolutePath().equals("/");
        Q.out(file.getAbsolutePath());

        menu.setHeaderTitle(L.tr(R.string.actions));
        menu.setHeaderIcon(R.drawable.info);

        if (!isRoot) {
            menu.add(Menu.NONE, FileMenu.DELETE.ordinal(),
                    Menu.NONE, L.tr(R.string.action_delete));
        }

        if (!file.isDirectory()) {
            menu.add(Menu.NONE, FileMenu.PROPERTIES.ordinal(),
                    Menu.NONE, L.tr(R.string.action_properties));
        }

        if (!isRoot) {
            menu.add(Menu.NONE, FileMenu.RENAME.ordinal(),
                    Menu.NONE, "Переименовать");
        }

        if ((file != null) && (file.isDirectory())) {
            menu.add(Menu.NONE, FileMenu.CALC_SIZE.ordinal(),
                    Menu.NONE, "Подсчитать размер папки");
        }
    }

    public void deleteItem(int position) {
        final DirElement dirElement = adapter.getItem(position);
        String fileName = dirElement.getFile().getName();

        GUI.askQuestion(L.tr(R.string.confirm), String.format(L.tr(R.string.confirm_delete_file), fileName),
                new Runnable() {
                    @Override
                    public void run() {
                        if (dirElement.isBackButton()) {
                            goUp();
                        }
                        boolean success = recursiveDelete(dirElement.getFile());
                        if (success) {
                            dynamicallyRemoveDirElement(dirElement);
                        } else {
                            GUI.toast(L.tr(R.string.error_delete_file));
                        }
                    }
                });
    }

    private void findAndDeleteViewWithFile(File file) {
        for (DirElement element : adapter.elements) {
            if (element.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                adapter.removeElement(element);
                adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    public void calcFolderSize(int position) {
        final DirElement dirElement = adapter.getItem(position);
        File file = dirElement.getFile();
        boolean isRoot = file.getAbsolutePath().equals("/");
        String dirName = isRoot ? "/" : dirElement.getFile().getName();

        final ProgressDialog progressDialog = GUI.showProgressDialog(
                "Подсчёт размера для папки " + dirName);

        Common.runParallel(new Runnable() {
            @Override
            public void run() {
                final long dirSize = FileUtils.sizeOfDirectory(dirElement.getFile());
                final String strSize = String.format("%,d %s", dirSize, "[Byte]");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                        GUI.showMessage("Размер директории:", strSize);
                        dirElement.setFileSize(dirSize);
                    }
                });
            }
        });
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

        String message = L.tr(R.string.size) + " = " + dirElement.getFile().length() + " " + L.tr(R.string.bytes);
        GUI.showMessage(L.tr(R.string.action_properties), message);
    }

    public void renameItem(int itemIndex) {
        DirElement dirElement = adapter.getItem(itemIndex);
        dirElement.rename();
        adapter.autoSort();
    }

    public void showRootActions() {

    }

    public void updateRootDirectory(File file) {
        directory = file;
        setStatus(directory.toString());
        //Update file path
        for (DirElement dirElement : adapter.elements) {
            dirElement.setFile(new File(directory, dirElement.getFile().getName()));
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            String path = directory.getAbsolutePath();
            pathToScroll.put(path, new ScrollPosition(listView));
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
        if(autoSort){
            sort();
        }
    }

    public void sort() {
        Collections.sort(elements, DirElement.getComparator());
    }
}