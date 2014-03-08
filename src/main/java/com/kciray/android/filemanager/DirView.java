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
import android.content.Context;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.kciray.android.L;
import com.kciray.android.OnInputListener;
import com.kciray.android.gui.GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirView extends FrameLayout {
    private File directory;
    private Context context;
    private DirViewAdapter adapter;
    private ListView listView;

    public DirView(Activity activity) {
        super(activity);
        this.context = activity;
        listView = new ListView(context);

        String sdRoot = Environment.getExternalStorageDirectory().getPath();
        setDirectory(sdRoot);

        adapter = new DirViewAdapter(context);
        rebuildDir();

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DirElement dirElement = adapter.getItem(position);

                if (dirElement.isBackButton()) {
                    if (directory.getParentFile() != null) {
                        directory = directory.getParentFile();
                        rebuildDir();
                    }
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
        addView(listView);
    }

    private void rebuildDir() {
        adapter.clear();
        adapter.addElement(DirElement.getBackNavElement(context));

        File[] subFiles = null;
        subFiles = directory.listFiles();

        if (subFiles != null) {
            for (File dirElementFile : subFiles) {
                DirElement dirElement;

                if (FileScanner.cachedView(dirElementFile)) {
                    dirElement = FileScanner.getCachedView(dirElementFile);
                } else {
                    dirElement = new DirElement(context, dirElementFile);
                    FileScanner.addToCach(dirElementFile, dirElement);
                }

                adapter.addElement(dirElement);
            }
        }

        adapter.notifyDataSetChanged();
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
        DirElement dirElement = new DirElement(context, file);
        FileScanner.addToCach(file, dirElement);
        adapter.addElement(dirElement);
        adapter.notifyDataSetChanged();
    }

    private void dynamicallyRemoveDirElement(DirElement dirElement) {
        adapter.removeElement(dirElement);
        adapter.notifyDataSetChanged();
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
            menu.setHeaderTitle(L.tr(R.string.actions));
            menu.setHeaderIcon(R.drawable.info);

            menu.add(Menu.NONE, FileMenu.DELETE.ordinal(), Menu.NONE, L.tr(R.string.action_delete));
            menu.add(Menu.NONE, FileMenu.PROPERTIES.ordinal(), Menu.NONE, L.tr(R.string.action_properties));
            menu.add(Menu.NONE, FileMenu.RENAME.ordinal(), Menu.NONE, "Переименовать");
        }
    }

    public void deleteItem(int position) {
        final DirElement dirElement = adapter.getItem(position);
        String fileName = dirElement.getFile().getName();

        GUI.askQuestion(L.tr(R.string.confirm), String.format(L.tr(R.string.confirm_delete_file), fileName),
                new Runnable() {
                    @Override
                    public void run() {
                        boolean success = recursiveDelete(dirElement.getFile());
                        if (success) {
                            dynamicallyRemoveDirElement(dirElement);
                        } else {
                            GUI.toast(L.tr(R.string.error_delete_file));
                        }
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

        String message = L.tr(R.string.size) + " = " + dirElement.getFile().length() + " " +L.tr(R.string.bytes);
        GUI.showMessage(L.tr(R.string.action_properties), message);
    }

    public void renameItem(int itemIndex) {
        DirElement dirElement = adapter.getItem(itemIndex);
        dirElement.rename();
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
}