package com.kciray.android.filemanager;

import android.widget.ImageButton;

import com.kciray.android.commons.gui.ToastUtils;
import com.kciray.android.commons.sys.L;
import com.kciray.android.commons.sys.root.FileMgr;
import com.kciray.commons.io.ExFile;

import java.util.LinkedList;
import java.util.List;

public class BookmarkManager{
    private MainActivity mainActivity;
    private List<Bookmark> bookmarkList = new LinkedList<>();
    private static BookmarkManager instance;

    public void setAddBookmarkButton(ImageButton addBookmarkButton) {
        this.addBookmarkButton = addBookmarkButton;
    }

    private ImageButton addBookmarkButton;

    private BookmarkManager() {
    }

    public void loadAllBookmarks() {
        bookmarkList = mainActivity.getDbHelper().getAllBookmarks();
    }

    public void addBookmarksToNavDrawer() {
        for (Bookmark bookmark : bookmarkList) {
            mainActivity.addElementToCategory(MainActivity.DrawerCategories.BOOKMARKS,
                    bookmark.label, bookmark.dir);
        }
    }

    public static BookmarkManager getInstance() {
        if (instance == null) {
            instance = new BookmarkManager();
        }
        return instance;
    }

    public void addBookmark(String label, String path) {
        MainActivity main = MainActivity.getInstance();
        Bookmark bookmark = new Bookmark(label, FileMgr.getFile(path));
        bookmarkList.add(bookmark);
        mainActivity.addElementToCategory(MainActivity.DrawerCategories.BOOKMARKS, label, FileMgr.getFile(path));
        updateBookmarkButton(FileMgr.getFile(path));
        main.getDbHelper().addBookmarkToDB(label, path);
        ToastUtils.show(String.format(L.tr(R.string.add_bookmark_toast), label));
    }

    public void deleteBookmark(String path){
        MainActivity main = MainActivity.getInstance();
        ExFile file = FileMgr.getFile(path);
        deleteBookmark(file);
        mainActivity.removeElementFromCategory(MainActivity.DrawerCategories.BOOKMARKS, file);
        updateBookmarkButton(file);
        main.getDbHelper().deleteBookmarkFromDB(file.getFullPath());
        ToastUtils.show(L.tr(R.string.delete_bm_success));
    }

    public void updateBookmarkButton(ExFile dir) {
        if (mainActivity.getCurrentDir().equals(dir)) {
            Bookmark bookmark = getBookmark(dir);
            if (bookmark != null) {
                addBookmarkButton.setImageResource(R.drawable.ic_blue_star);
            } else {
                addBookmarkButton.setImageResource(R.drawable.ic_gray_star);
            }
        }
    }

    public Bookmark getBookmark(ExFile dir) {
        for (Bookmark bookmark : bookmarkList) {
            if (bookmark.dir.equals(dir)) {
                return bookmark;
            }
        }
        return null;
    }

    public void deleteBookmark(ExFile dir) {
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
    ExFile dir;

    public Bookmark(String label, ExFile dir) {
        this.label = label;
        this.dir = dir;
    }
}