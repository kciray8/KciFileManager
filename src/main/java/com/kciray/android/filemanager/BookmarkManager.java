package com.kciray.android.filemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageButton;

import com.kciray.android.commons.gui.ToastUtils;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.sys.LBroadManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class BookmarkManager extends BroadcastReceiver {
    public static final String ADD_NEW_BOOKMARK = "com.kciray.android.intent.ADD_NEW_BOOKMARK";
    public static final String DELETE_BOOKMARK = "com.kciray.android.intent.DELETE_BOOKMARK";
    public static final String BOOKMARK_LABEL = "label";
    public static final String BOOKMARK_DIR = "dir";
    private MainActivity mainActivity;
    private List<Bookmark> bookmarkList = new LinkedList<>();
    private static BookmarkManager instance;
    private boolean lastBtnStateWasOff;

    public void register() {
        LBroadManager.registerReceiver(this, BookmarkManager.ADD_NEW_BOOKMARK);
        LBroadManager.registerReceiver(this, BookmarkManager.DELETE_BOOKMARK);
    }

    public void setAddBookmarkButton(ImageButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
    }

    private ImageButton addBookmarkButton;

    private BookmarkManager() {
    }

    public void loadAllBookmarks() {
        bookmarkList = mainActivity.getDbHelper().getAllBookmarks();

        for (Bookmark bookmark : bookmarkList) {
            mainActivity.addElementToCategory(MainActivity.DrawerCategories.BOOKMARKS,
                    bookmark.label, new File(bookmark.dir));
        }
    }

    public static BookmarkManager getInstance() {
        if (instance == null) {
            instance = new BookmarkManager();
        }
        return instance;
    }

    public void addBookmark(String label, File file) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity main = MainActivity.getInstance();

        switch (intent.getAction()) {
            case ADD_NEW_BOOKMARK:
                String label = intent.getStringExtra(BOOKMARK_LABEL);
                File dir = (File) intent.getSerializableExtra(BOOKMARK_DIR);
                Bookmark bookmark = new Bookmark(label, dir.getAbsolutePath());

                bookmarkList.add(bookmark);
                mainActivity.addElementToCategory(MainActivity.DrawerCategories.BOOKMARKS, label, dir);
                updateBookmarkButton(dir.getAbsolutePath());

                main.getDbHelper().addBookmarkToDB(label, dir.getAbsolutePath());
                ToastUtils.show(String.format(L.tr(R.string.add_bookmark_toast), label));
                break;

            case DELETE_BOOKMARK:
                File bookmarkFile = (File) intent.getSerializableExtra(BOOKMARK_DIR);
                String path = bookmarkFile.getAbsolutePath();

                deleteBookmark(path);
                mainActivity.removeElementFromCategory(MainActivity.DrawerCategories.BOOKMARKS, bookmarkFile);
                updateBookmarkButton(path);
                ToastUtils.show(L.tr(R.string.delete_bm_success));
                break;
        }
    }

    public void updateBookmarkButton(String dir) {
        Bookmark bookmark = getBookmark(dir);
        if (bookmark != null) {
            addBookmarkButton.setImageResource(R.drawable.blue_star);
        } else {
            addBookmarkButton.setImageResource(R.drawable.gray_star);
        }
    }

    public Bookmark getBookmark(String dir) {
        for (Bookmark bookmark : bookmarkList) {
            if (bookmark.dir.equals(dir)) {
                return bookmark;
            }
        }
        return null;
    }

    public void deleteBookmark(String dir) {
        for (Bookmark bookmark : bookmarkList) {
            if (bookmark.dir.equals(dir)) {
                bookmarkList.remove(bookmark);
                return;
            }
        }
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}

class Bookmark {
    String label;
    String dir;

    public Bookmark(String label, String dir) {
        this.label = label;
        this.dir = dir;
    }
}