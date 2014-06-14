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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.kciray.android.commons.gui.DialogUtils;
import com.kciray.android.commons.gui.KciNavDrawer;
import com.kciray.android.commons.sys.App;
import com.kciray.android.commons.sys.Global;

import java.io.File;

public class MainActivity extends ActionBarActivity implements KciNavDrawer.OnItemClick<MainActivity.DrawerCategories>, SharedPreferences.OnSharedPreferenceChangeListener {
    DirView activeDirView;

    enum DrawerCategories {SYSTEM, BOOKMARKS, LOL}

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
    KciNavDrawer<DrawerCategories> navDrawer;
    boolean devMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.context = this;
        mainActivity = this;
        mainPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = mainPref.getBoolean(getStr(R.string.firstLaunch), true);
        if (firstLaunch) {
            setDefaultPrefForActive();
            mainPref.edit().putBoolean(getStr(R.string.firstLaunch), false).commit();
        }

        String rootSd = Environment.getExternalStorageDirectory().getPath();
        String dir;

        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);
        if (autoSaveLastDir) {
            dir = mainPref.getString(getStr(R.string.autoSaveLastDirStr), rootSd);
        } else {
            dir = rootSd;
        }

        devMode = mainPref.getBoolean(getStr(R.string.devMode), false);
        activeDirView = new DirView(this, dir);

        navDrawer = new KciNavDrawer<>(this);
        navDrawer.setMainContent(activeDirView);
        navDrawer.registerOnClickItemListener(this);
        navDrawer.addCategory(DrawerCategories.SYSTEM, R.string.cat_system);
        navDrawer.addCategory(DrawerCategories.BOOKMARKS, R.string.cat_bookmarks);

        File root = new File("/");
        addElementToCategory(DrawerCategories.SYSTEM, R.string.root, root);

        File sdCard = Environment.getExternalStorageDirectory();
        addElementToCategory(DrawerCategories.SYSTEM, R.string.sd_card, sdCard);

        if (devMode) {
            File internalDir = App.getInternalDir();
            addElementToCategory(DrawerCategories.SYSTEM, R.string.internal_dir, internalDir);
        }

        drawerToggle = navDrawer.addButtonToActivity(this);
        setContentView(navDrawer);
        mainPref.registerOnSharedPreferenceChangeListener(this);
    }

    private void setDefaultPrefForActive() {
        mainPref.edit().putBoolean(getStr(R.string.devMode), false).commit();
    }
    public void addElementToCategory(DrawerCategories category, int titleRes, File file){
        navDrawer.addInfoViewToCategory(category,
                getIVBookmark(this.getStr(titleRes), file.getAbsolutePath()), file);
    }

    @Override
    public void onClickItem(int categoryId, Object data) {
        DrawerCategories categories = DrawerCategories.values()[categoryId];


        switch (categories) {
            case BOOKMARKS:
            case SYSTEM:
                final File bookmarkFile = (File) data;
                activeDirView.goToDir(bookmarkFile);

                //TODO - file operation IN OTHER THREAD!!!
                Handler handler = new Handler(getBaseContext().getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        navDrawer.closeDrawers();
                    }
                });

                break;
        }

    }

    private View getIVBookmark(String title, String description) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View infoView = inflater.inflate(R.layout.drawer_bookmark, null);
        TextView labelView = (TextView) infoView.findViewById(R.id.label);
        labelView.setText(title);
        TextView descView = (TextView) infoView.findViewById(R.id.description);
        descView.setText(description);

        return infoView;
    }

    /*  Buttons for Drawer in ActionBar */
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*/  Buttons for Drawer in ActionBar /*/

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

    void startNewActivity(Class<?> cls) {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getStr(R.string.devMode))) {
            DialogUtils.askQuestion("Требуется перезапуск!", "Изменения будут доступны только после перезапуска. Перезапустить приложение сейчас?",
                    new Runnable() {
                        @Override
                        public void run() {
                            App.restart();
                        }
                    });
        }
    }
}