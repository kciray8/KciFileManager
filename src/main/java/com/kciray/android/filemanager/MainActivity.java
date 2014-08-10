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
import com.kciray.android.commons.sys.Global;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.sys.root.FileMgr;
import com.kciray.commons.io.ExFile;

import java.util.ArrayList;
import java.util.List;

import static com.kciray.android.commons.sys.KLog.mark;


public class MainActivity extends ActionBarActivity implements KciNavDrawer.OnItemClick<MainActivity.DrawerCategories>, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int REQUEST_CODE_TO_HISTORY = 1;

    //Extras
    public static final String EXTRA_FILE_PATH = "com.kciray.intent.extra.EXTRA_FILE_PATH";

    private DirView activeDirView;
    private static int historyMaxSize;

    public static void addToHistory(ExFile file) {
        if (history.size() == historyMaxSize) {
            history.remove(history.size() - 1);
        }

        if (history.size() > 0) {
            ExFile lastFile = history.get(0);
            if(lastFile.equals(file)){
                return;//Not add duplicate
            }
        }

        history.add(0, file);
    }

    public static List<ExFile> getHistory() {
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
    private static List<ExFile> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        Global.setContext(this);
        dbHelper = new DBHelper();

        bookmarkManager = BookmarkManager.getInstance();
        bookmarkManager.setMainActivity(this);
        bookmarkManager.loadAllBookmarks();

        mainPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = mainPref.getBoolean(getStr(R.string.firstLaunch), true);
        if (firstLaunch) {
            setDefaultPrefForActive();
            mainPref.edit().putBoolean(getStr(R.string.firstLaunch), false).commit();
        }
        historyMaxSize = Integer.valueOf(mainPref.getString(getStr(R.string.historyLength), "50"));

        updateEngine();

        String rootSd = Environment.getExternalStorageDirectory().getPath();
        String dir;

        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);
        if (autoSaveLastDir) {
            dir = mainPref.getString(getStr(R.string.autoSaveLastDirStr), rootSd);
        } else {
            dir = rootSd;
        }
        dir = "/";

        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            if (launchIntent.hasExtra(EXTRA_FILE_PATH)) {
                dir = launchIntent.getStringExtra(EXTRA_FILE_PATH);
            }
        }

        devMode = mainPref.getBoolean(getStr(R.string.devMode), false);
        activeDirView = new DirView(this, dir);
        activeDirView.goToDir(FileMgr.getFile(dir));

        navDrawer = new KciNavDrawer<>(this);
        navDrawer.setMainContent(activeDirView);
        navDrawer.registerOnClickItemListener(this);

        navDrawer.registerOnCreatePopupMenuListener((popupMenu, categoryId, data) -> {
            if (categoryId == DrawerCategories.BOOKMARKS.ordinal()) {
                final MenuItem itemDelete = popupMenu.getMenu().add(R.string.delete_bookmark);
                popupMenu.setOnMenuItemClickListener((item) -> {
                            if (item == itemDelete) {
                                BookmarkManager.getInstance().deleteBookmark(((ExFile) data).getFullPath());
                            }
                            return true;
                        }
                );
            }
        });

        navDrawer.addCategory(DrawerCategories.SYSTEM, R.string.cat_system);
        navDrawer.addCategory(DrawerCategories.BOOKMARKS, R.string.cat_bookmarks);

        ExFile root = FileMgr.getFile("/");
        addElementToCategory(DrawerCategories.SYSTEM, R.string.root, root);

        ExFile sdCard = FileMgr.getFile(Environment.getExternalStorageDirectory().getAbsolutePath());
        addElementToCategory(DrawerCategories.SYSTEM, R.string.sd_card, sdCard);

        ExFile DCIM = FileMgr.getFile(Environment.getExternalStorageDirectory().getAbsolutePath()).append(Eclair.DIRECTORY_DCIM);
        addElementToCategory(DrawerCategories.SYSTEM, R.string.dcim, DCIM);

        ExFile downloads = FileMgr.getFile(Environment.getExternalStorageDirectory().getAbsolutePath()).append(Eclair.DIRECTORY_DOWNLOADS);
        addElementToCategory(DrawerCategories.SYSTEM, R.string.downloads, downloads);

        if (devMode) {
            ExFile internalDir = FileMgr.getFile(AppUtils.getInternalDir());
            addElementToCategory(DrawerCategories.SYSTEM, R.string.internal_dir, internalDir);
        }

        drawerToggle = navDrawer.addButtonToActivity(this);
        setContentView(navDrawer);
        mainPref.registerOnSharedPreferenceChangeListener(this);

        bookmarkManager.addBookmarksToNavDrawer();
        configBottomBar();
    }

    private void updateEngine() {
        String engine = mainPref.getString(getStr(R.string.engine), "1");
        FileMgr.getIns().setPrefEngine(Integer.valueOf(engine));

        String subtitle;
        if (!FileMgr.getIns().nullShell()) {
            subtitle = getString(R.string.busybox_mode);
        } else {
            subtitle = "";
        }
        getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void configBottomBar() {
        View bottomBar = findViewById(R.id.bottom_bar);
        ImageButton refreshButton = (ImageButton) bottomBar.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v -> {
            activeDirView.refresh();
            ToastUtils.show(getString(R.string.update_done));
        });

        ImageButton inputAndNavigateButton = (ImageButton) bottomBar.findViewById(R.id.input_and_navigate_button);
        inputAndNavigateButton.setOnClickListener(v -> {
            DialogUtils.inputString(getString(R.string.input_path), path -> {
                activeDirView.goToDir(FileMgr.getFile(path));
            });
        });

        bottomBar.findViewById(R.id.open_history).setOnClickListener(v -> {
            openHistory();
        });
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }

    private void setDefaultPrefForActive() {
        mainPref.edit().putBoolean(getStr(R.string.devMode), false).commit();
    }

    public void addElementToCategory(DrawerCategories category, int titleRes, ExFile file) {
        addElementToCategory(category, L.tr(titleRes), file);
    }

    public void addElementToCategory(DrawerCategories category, String title, ExFile file) {
        navDrawer.addInfoViewToCategory(category,
                getIVBookmark(title, file.getFullPath()), file);
    }

    public void removeElementFromCategory(DrawerCategories category, ExFile data) {
        navDrawer.deleteViewFromCategory(category, data);
    }

    @Override
    public void onClickItem(int categoryId, Object data) {
        DrawerCategories categories = DrawerCategories.values()[categoryId];

        switch (categories) {
            case BOOKMARKS:
            case SYSTEM:
                final ExFile bookmarkFile = (ExFile) data;
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
        mark();
        super.onStop();
        saveLastDir();
    }

    private void saveLastDir() {
        boolean autoSaveLastDir = mainPref.getBoolean(getStr(R.string.autoSaveLastDir), true);

        if (autoSaveLastDir) {
            String path = activeDirView.getDirectory().getFullPath();
            String name = getStr(R.string.autoSaveLastDirStr);
            mainPref.edit().putString(name, path).commit();
        }
    }

    public ExFile getCurrentDir() {
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
        ExFile dir = history.get(id);
        activeDirView.goToDir(dir);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem addNewFolderItem = menu.findItem(R.id.add_new_folder);
        addNewFolderItem.setOnMenuItemClickListener(item -> {
            activeDirView.addNewFolder();
            return false;
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

    private void openHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TO_HISTORY);
        overridePendingTransition(R.anim.slide_from_right, 0);
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
            case COPY_FULL_PATH:
                activeDirView.copyFullPath(itemIndex);
                break;
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getStr(R.string.devMode))) {
            DialogUtils.askQuestion(getString(R.string.need_restart), getString(R.string.need_restart_text),
                    AppUtils::restart);
        }
        if (key.equals(getStr(R.string.engine))) {
            updateEngine();
            activeDirView.refresh();
        }

        if (key.equals(getStr(R.string.historyLength))) {
            historyMaxSize = new Integer(mainPref.getString(getStr(R.string.historyLength), "50"));
        }
    }
}