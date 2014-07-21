package com.kciray.android.filemanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.kciray.android.commons.io.Q;
import com.kciray.android.commons.sys.Global;

import java.util.LinkedList;
import java.util.List;

class DBHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String DATABASE_NAME = "main.db";

    private static class BookmarkTable implements BaseColumns {
        private static enum COLUMNS{TABLE_NAME}
        
        private static final String TABLE_NAME = "bookmarks";
        private static final String COLUMN_NAME = "name";
        private static final String COLUMN_PATH = "path";

        static ContentValues getRow(String columnName, String columnPath) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, columnName);
            values.put(COLUMN_PATH, columnPath);
            return values;
        }
    }

    public DBHelper() {
        super(Global.getContext(), DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery =
                "CREATE TABLE " + BookmarkTable.TABLE_NAME +
                        "(" + BookmarkTable._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT, " +
                        BookmarkTable.COLUMN_NAME + TEXT_TYPE + "," +
                        BookmarkTable.COLUMN_PATH + TEXT_TYPE + ");";

        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {

    }

    public SQLiteDatabase getDB() {
        return super.getWritableDatabase();
    }

    public void addBookmarkToDB(String name, String path) {
        getDB().insert(BookmarkTable.TABLE_NAME, null, BookmarkTable.getRow(name, path));
    }

    public List<Bookmark> getAllBookmarks() {
        List<Bookmark> bookmarks = new LinkedList<>();
        String query = "SELECT * FROM " + BookmarkTable.TABLE_NAME;
        Cursor cursor = getDB().rawQuery(query, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(1);
            String path = cursor.getString(2);
            Bookmark bookmark = new Bookmark(name, path);
            bookmarks.add(bookmark);
        }

        return bookmarks;
    }

    public void deleteBookmarkFromDB(String path) {
        String deleteQuery = "DELETE FROM " + BookmarkTable.TABLE_NAME +
                " WHERE " + BookmarkTable.COLUMN_PATH + "='" + path + "'";

        getDB().execSQL(deleteQuery);
    }
}