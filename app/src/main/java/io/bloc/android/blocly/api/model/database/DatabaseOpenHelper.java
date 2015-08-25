package io.bloc.android.blocly.api.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.bloc.android.blocly.api.model.database.table.Table;

/**
 * Created by Mike on 8/25/2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String NAME = "blocly_db";

    private static final int VERSION = 1;

    private Table[] tables;

    public DatabaseOpenHelper(Context context, Table... tables) {
        super(context, NAME, null, VERSION);
        this.tables = tables;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Table table : tables) {
            db.execSQL(table.getCreateStatement());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Table table : tables) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }
/*#53 Named the database blocly_db and the name is a static final variable. The version of Blocly
* has remained 1 for now. The version and name at the end of this code block is passed to the super constructor.
*onCreate(SQLiteDatabase)method is invoke when the database is created. Add all of the Table objects to
* execute the Create statements. For an upgrade, the onUpgrade(SQLiteDatabase)method is invoked. Add in the
* new version to replace the old version and invoke the upgrade method.
*/

}

