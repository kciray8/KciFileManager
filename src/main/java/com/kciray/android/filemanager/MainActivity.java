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
import android.widget.ImageButton;
import android.widget.TextView;

import com.kciray.android.commons.compat.Eclair;
import com.kciray.android.commons.gui.ActivityUtils;
import com.kciray.android.commons.gui.DialogUtils;
import com.kciray.android.commons.gui.KciNavDrawer;
import com.kciray.android.commons.gui.ToastUtils;
import com.kciray.android.commons.sys.AppUtils;
import com.kciray.android.commons.sys.BundleUtils;
import com.kciray.android.commons.sys.Global;
import com.kciray.android.commons.sys.IntentUtils;
import com.kciray.android.commons.sys.KLog;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.sys.LBroadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements KciNavDrawer.OnItemClick<MainActivity.DrawerCategories>, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int REQUEST_CODE_TO_HISTORY = 1;

    //Extras
    public static final String EXTRA_FILE_PATH = "com.kciray.intent.extra.EXTRA_FILE_PATH";

    private DirView activeDirView;
    private static int historyMaxSize;

    public static void addToHistory(File file) {
        if (history.size() == historyMaxSize) {
            history.remove(history.size() - 1);
        }
        history.add(0, file);
    }

    public static List<File> getHistory() {
        return history;
    }

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
    BookmarkManager bookmarkManager;
    DBHelper dbHelper;
    private static List<File> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentUtils.outExtras(getIntent());
        BundleUtils.outExtras(savedInstanceState);
        mainActivity = this;
        Global.setContext(this);
        dbHelper = new DBHelper();

        bookmarkManager = BookmarkManager.getInstance();
        bookmarkManager.setMainActivity(this);
        bookmarkManager.register();
        bookmarkManager.loadAllBookmarks();

        mainPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = mainPref.getBoolean(getStr(R.string.firstLaunch), true);
        if (firstLaunch) {
            setDefaultPrefForActive();
            mainPref.edit().putBoolean(getStr(R.string.firstLaunch), false).commit();
        }
        historyMaxSize = new Integer(mainPref.getString(getStr(R.string.historyLength), "50"));

        String rootSd = Environment.getExternalStorageDirectory().getPath();
        String dir;

        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);
        if (autoSaveLastDir) {
            dir = mainPref.getString(getStr(R.string.autoSaveLastDirStr), rootSd);
        } else {
            dir = rootSd;
        }

        Intent launchIntent = getIntent();
        if(launchIntent != null){
            if(launchIntent.hasExtra(EXTRA_FILE_PATH)){
                dir = launchIntent.getStringExtra(EXTRA_FILE_PATH);
            }
        }

        devMode = mainPref.getBoolean(getStr(R.string.devMode), false);
        activeDirView = new DirView(this, dir);
        activeDirView.goToDir(new File(dir));

        navDrawer = new KciNavDrawer<>(this);
        navDrawer.setMainContent(activeDirView);
        navDrawer.registerOnClickItemListener(this);

        navDrawer.registerOnCreatePopupMenuListener((popupMenu, categoryId, data) -> {
            if (categoryId == DrawerCategories.BOOKMARKS.ordinal()) {
                final MenuItem itemDelete = popupMenu.getMenu().add(R.string.delete_bookmark);
                popupMenu.setOnMenuItemClickListener((item) -> {
                            if (item == itemDelete) {
                                Intent delBookmark = new Intent(BookmarkManager.DELETE_BOOKMARK);
                                delBookmark.putExtra(BookmarkManager.BOOKMARK_DIR, (File) data);
                                LBroadManager.send(delBookmark);
                            }
                            return true;
                        }
                );
            }
        });

        navDrawer.addCategory(DrawerCategories.SYSTEM, R.string.cat_system);
        navDrawer.addCategory(DrawerCategories.BOOKMARKS, R.string.cat_bookmarks);

        File root = new File("/");
        addElementToCategory(DrawerCategories.SYSTEM, R.string.root, root);

        File sdCard = Environment.getExternalStorageDirectory();
        addElementToCategory(DrawerCategories.SYSTEM, R.string.sd_card, sdCard);

        File DCIM = new File(Environment.getExternalStorageDirectory(), Eclair.DIRECTORY_DCIM);
        addElementToCategory(DrawerCategories.SYSTEM, R.string.dcim, DCIM);

        File downloads = new File(Environment.getExternalStorageDirectory(), Eclair.DIRECTORY_DOWNLOADS);
        addElementToCategory(DrawerCategories.SYSTEM, R.string.downloads, downloads);

        if (devMode) {
            File internalDir = AppUtils.getInternalDir();
            addElementToCategory(DrawerCategories.SYSTEM, R.string.internal_dir, internalDir);
        }

        drawerToggle = navDrawer.addButtonToActivity(this);
        setContentView(navDrawer);
        mainPref.registerOnSharedPreferenceChangeListener(this);

        bookmarkManager.addBookmarksToNavDrawer();
        configBottomBar();
    }

    private void configBottomBar() {
        View bottomBar = findViewById(R.id.bottom_bar);
        ImageButton refreshButton = (ImageButton) bottomBar.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v->{
            activeDirView.refresh();
            ToastUtils.show(getString(R.string.update_done));
        });
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }

    private void setDefaultPrefForActive() {
        mainPref.edit().putBoolean(getStr(R.string.devMode), false).commit();
    }

    public void addElementToCategory(DrawerCategories category, int titleRes, File file) {
        addElementToCategory(category, L.tr(titleRes), file);
    }

    public void addElementToCategory(DrawerCategories category, String title, File file) {
        navDrawer.addInfoViewToCategory(category,
                getIVBookmark(title, file.getAbsolutePath()), file);
    }

    public void removeElementFromCategory(DrawerCategories category, File data) {
        navDrawer.deleteViewFromCategory(category, data);
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
                handler.post(navDrawer::closeDrawers);

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
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLastDir();
    }

    private void saveLastDir() {
        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);

        if (autoSaveLastDir) {
            String path = activeDirView.getDirectory().getAbsolutePath();
            String name = getStr(R.string.autoSaveLastDirStr);
            mainPref.edit().putString(name, path).commit();
        }
    }

    public File getCurrentDir() {
        return activeDirView.getDirectory();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            activeDirView.goUp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void goToDirFromHis(int id) {
        File dir = history.get(id);
        activeDirView.goToDir(dir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem showFolderPropItem = menu.findItem(R.id.show_root_actions);
        showFolderPropItem.setOnMenuItemClickListener(item -> {
            openContextMenu(activeDirView.backNavElement.getView());
            return false;
        });

        MenuItem addNewFileItem = menu.findItem(R.id.add_new_file);
        addNewFileItem.setOnMenuItemClickListener(item -> {
            activeDirView.addNewFile();
            return false;
        });

        MenuItem addNewFolderItem = menu.findItem(R.id.add_new_folder);
        addNewFolderItem.setOnMenuItemClickListener(item -> {
            activeDirView.addNewFolder();
            return false;
        });

        addMenuAction(menu, R.id.history, () -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivityForResult(intent, REQUEST_CODE_TO_HISTORY);
        });

        addMenuAction(menu, R.id.settings, () -> {
            Intent intent = new Intent(MainActivity.this, FMPreferenceActivity.class);
            startActivity(intent);
        });

        addMenuAction(menu, R.id.about, () -> {
            ActivityUtils.start(AboutActivity.class);
            overridePendingTransition(R.anim.rotate_in, 0);
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TO_HISTORY) {
            if (resultCode == Activity.RESULT_OK) {
                int id = data.getIntExtra(HistoryActivity.EXTRA_ID, 0);
                goToDirFromHis(id);
            }
        }
    }

    private void addMenuAction(Menu menu, int itemId, final Runnable runnable) {
        MenuItem settingsItem = menu.findItem(itemId);
        if (settingsItem != null)
            settingsItem.setOnMenuItemClickListener(item -> {
                runnable.run();
                return false;
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
            case OPEN_INTENT:
                activeDirView.openIntent(itemIndex);
                break;
            case SEND_TO_HOME_SCREEN:
                activeDirView.sendToHomeScreen(itemIndex);
                break;
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getStr(R.string.devMode))) {
            DialogUtils.askQuestion("Требуется перезапуск!", "Изменения будут доступны только после перезапуска. Перезапустить приложение сейчас?",
                    AppUtils::restart);
        }
        if (key.equals(getStr(R.string.historyLength))) {
            historyMaxSize = new Integer(mainPref.getString(getStr(R.string.historyLength), "50"));
        }
    }
}