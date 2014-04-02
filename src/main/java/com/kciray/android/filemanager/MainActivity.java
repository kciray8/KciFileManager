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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.kciray.Q;
import com.kciray.android.Common;

public class MainActivity extends Activity {
    DirView activeDirView;

    public SharedPreferences getMainPref() {
        return mainPref;
    }

    SharedPreferences mainPref;

    public static MainActivity getInstance() {
        return mainActivity;
    }

    public String getStr(int id) {
        return getResources().getString(id);
    }

    static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        mainPref = PreferenceManager.getDefaultSharedPreferences(this);
        Common.setContext(this);

        String rootSd = Environment.getExternalStorageDirectory().getPath();
        String dir;

        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);
        if (autoSaveLastDir) {
            dir = mainPref.getString(getStr(R.string.autoSaveLastDirStr), rootSd);
        } else {
            dir = rootSd;
        }
        Q.out(dir);

        activeDirView = new DirView(this, dir);
        setContentView(activeDirView);
    }

    @Override
    protected void onDestroy() {
        String path = activeDirView.getDirectory().getAbsolutePath();
        String name = getStr(R.string.autoSaveLastDirStr);
        mainPref.edit().putString(name, path).commit();

        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            activeDirView.goUp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem showFolderPropItem = menu.findItem(R.id.show_root_actions);
        showFolderPropItem.setOnMenuItemClickListener(new MenuItem.
                OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openContextMenu(activeDirView.backNavElement.getView());
                return false;
            }
        });

        MenuItem addNewFileItem = menu.findItem(R.id.add_new_file);
        addNewFileItem.setOnMenuItemClickListener(new MenuItem.
                OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activeDirView.addNewFile();
                return false;
            }
        });

        MenuItem addNewFolderItem = menu.findItem(R.id.add_new_folder);
        addNewFolderItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activeDirView.addNewFolder();
                return false;
            }
        });


        addMenuAction(menu, R.id.settings, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, FMPreferenceActivity.class);
                startActivity(intent);
            }
        });

        addMenuAction(menu, R.id.close, new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
        addMenuAction(menu, R.id.about, new Runnable() {
            @Override
            public void run() {
                startNewActivity(AboutActivity.class);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    void startNewActivity(Class<?> cls){
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    private void addMenuAction(Menu menu, int itemId, final Runnable runnable) {
        MenuItem settingsItem = menu.findItem(itemId);
        if (settingsItem != null)
            settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    runnable.run();
                    return false;
                }
            });
    }

    public void addNewFolder(MenuItem item) {
        activeDirView.addNewFolder();
    }

    public void addNewFile(MenuItem item) {
        activeDirView.addNewFile();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Q.out(menu);
        super.onCreateContextMenu(menu, v, menuInfo);
        activeDirView.handleContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        FileMenu menuEnum = FileMenu.values()[menuItemIndex];
        int itemIndex = info.position;

        switch (menuEnum) {
            case DELETE:
                activeDirView.deleteItem(itemIndex);
                break;
            case PROPERTIES:
                activeDirView.showProp(itemIndex);
                break;
            case RENAME:
                activeDirView.renameItem(itemIndex);
                break;
            case CALC_SIZE:
                activeDirView.calcFolderSize(itemIndex);
                break;
        }

        return true;
    }


}
