package io.bloc.android.blocly.api.model.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Mike on 8/21/2015.
 */
public abstract class Table {
    public static interface Builder {

        public long insert(SQLiteDatabase writableDB);
    }
    /*#54 Created the builder class. Each class using Builder has to be able to insert it's data
     *into the SQLiteDatabase.
     */
    protected static final String COLUMN_ID = "id";

    public abstract String getName();

    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writableDatabase, int oldVersion, int newVersion) {
        // Nothing
    }

    public Cursor fetchRow(SQLiteDatabase readonlyDatabase, long rowId) {
        return readonlyDatabase.query(true, getName(), null, COLUMN_ID + " = ?",
                new String[] {String.valueOf(rowId)}, null, null, null, null);
    }
    //#55 The Table class will be able to retrieve a row with fetchRow using the row identifier id.

    protected static String getString(Cursor cursor, String column){
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1){
            return "";
        }
        return cursor.getString(columnIndex);
    }

    protected static long getLong(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return -1l;
        }
        return cursor.getLong(columnIndex);
    }

    protected static boolean getBoolean(Cursor cursor, String column) {
        return getLong(cursor, column) == 1l;
    }

    /*#52 Established a name and create statement for the Table class, added the id column for each table.
     * At the end, the tables execute it's upgrades within this method.
     */
    /*#55 getString(cursor, String) returns a String object for the column specified. Added a method for
     * recovering a boolean from a Cursor. getBoolean(Cursor, String) returns true if the integer in column is 1.
     * */
}
