package io.bloc.android.blocly.api.model.database.table;

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
    /*#52 Established a name and create statement for the Table class, added the id column for each table.
     * At the end, the tables execute it's upgrades within this method.
     */
}