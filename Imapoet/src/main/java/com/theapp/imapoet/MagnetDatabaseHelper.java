package com.theapp.imapoet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper class, very similar to examples in the android docs, extends SQLiteOpenHelper to create and delete the tables used in the application. The Strings are stored in MagnetDatabaseContract.
 * Created by whitney on 7/1/14.
 */
public class MagnetDatabaseHelper extends SQLiteOpenHelper {
    public MagnetDatabaseHelper(Context context) {
        super(context, MagnetDatabaseContract.DATABASE_NAME, null, MagnetDatabaseContract.DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_CONTINUOUS_AWARDS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_ON_SAVE_AWARDS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_CONTINUOUS_STATISTICS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_ON_SAVE_STATISTICS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_SETTINGS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_SAVED_POEMS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_SAVED_POEMS_MAGNET_DETAIL_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_PACKS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_MAGNETS_TABLE);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.CREATE_LAST_SAVED_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_PACKS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_MAGNETS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_SAVED_POEMS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_SAVED_POEMS_DETAIL);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_CONTINUOUS_STATISTICS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_ON_SAVE_STATISTICS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_CONTINUOUS_AWARDS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_ON_SAVE_AWARDS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_SETTINGS);
        db.execSQL(MagnetDatabaseContract.MagnetEntry.SQL_DELETE_LAST_POEM);
        onCreate(db);
    }

}
