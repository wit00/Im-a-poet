package com.theapp.imapoet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.apache.http.auth.AUTH;


public class MagnetContentProvider extends ContentProvider {
    private MagnetDatabaseHelper magnetDatabaseHelper;
    private static final String AUTHORITY = "com.theapp.imapoet.provider.magnetcontentprovider";
    private static UriMatcher uriMatcher;

    private static final int MAGNET = 1;
    private static final int CONTINUOUS_AWARD = 2;
    private static final int INSERT_ON_SAVE_AWARD = 3;
    private static final int SETTINGS = 4;
    private static final int UPDATE_SETTINGS = 5;
    private static final int INSERT_SETTINGS = 6;
    private static final int INSERT_POEM = 7;
    private static final int INSERT_POEM_DETAIL = 8;
    private static final int INSERT_PACKS = 9;
    private static final int INSERT_PACK_MAGNETS = 10;
    private static final int PACKS = 11;
    private static final int MAGNETS = 12;
    private static final int DELETE_MAGNET = 13;
    private static final int INSERT_CURRENT_POEM = 14;
    private static final int GET_CURRENT_POEM = 15;
    private static final int DELETE_CURRENT_POEM = 16;
    private static final int UPDATE_POEM = 17;
    private static final int UPDATE_POEM_DETAIL = 18;
    private static final int DELETE_POEM_DETAIL = 19;
    private static final int SAVED_POEMS = 20;
    private static final int SAVED_POEM = 21;
    private static final int UPDATE_CURRENT_POEM = 22;
    private static final int DELETE_POEM = 23;
    private static final int INSERT_ON_SAVE_STATISTIC = 24;
    private static final int INSERT_CONTINUOUS_STATISTIC = 25;
    private static final int INSERT_CONTINUOUS_AWARD = 26;
    private static final int ON_SAVE_AWARD = 27;
    private static final int STATISTIC_CONTINUOUS = 28;
    private static final int STATISTIC_ON_SAVE = 29;
    private static final int AWARD = 30;
    private static final int UPDATE_CONTINUOUS_STATISTIC = 31;
    private static final int UPDATE_CONTINUOUS_AWARD = 32;
    private static final int UPDATE_ONSAVE_STATISTIC = 33;
    private static final int UPDATE_ONSAVE_AWARD = 34;
    private static final int ALL_STATISTICS = 35;
    private static final int UPDATE_PACK = 36;
    private static final int INSERT_AWARDS = 37;
    private static final int INSERT_AWARD_DETAILS = 38;
    private static final int AWARDS = 39;
    private static final int AWARDS_DETAIL = 40;
    private static final int UPDATE_AWARD = 41;
    private static final int UPDATE_AWARD_DETAIL = 42;





    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"magnet",MAGNET);
        uriMatcher.addURI(AUTHORITY,"award/continuous",CONTINUOUS_AWARD);
        uriMatcher.addURI(AUTHORITY,"award",AWARD);
        uriMatcher.addURI(AUTHORITY,"award/onSave",ON_SAVE_AWARD);
        uriMatcher.addURI(AUTHORITY,"insert/award/on_save",INSERT_ON_SAVE_AWARD);
        uriMatcher.addURI(AUTHORITY,"insert/award/continuous",INSERT_CONTINUOUS_AWARD);
        uriMatcher.addURI(AUTHORITY,"settings",SETTINGS);
        uriMatcher.addURI(AUTHORITY,"update/settings",UPDATE_SETTINGS);
        uriMatcher.addURI(AUTHORITY,"insert/settings",INSERT_SETTINGS);
        uriMatcher.addURI(AUTHORITY,"insert/poem",INSERT_POEM);
        uriMatcher.addURI(AUTHORITY,"insert/poem/detail",INSERT_POEM_DETAIL);
        uriMatcher.addURI(AUTHORITY,"insert/packs",INSERT_PACKS);
        uriMatcher.addURI(AUTHORITY,"insert/pack/magnet",INSERT_PACK_MAGNETS);
        uriMatcher.addURI(AUTHORITY,"packs",PACKS);
        uriMatcher.addURI(AUTHORITY,"magnets",MAGNETS);
        uriMatcher.addURI(AUTHORITY,"delete/magnet",DELETE_MAGNET);
        uriMatcher.addURI(AUTHORITY,"insert/currentPoem",INSERT_CURRENT_POEM);
        uriMatcher.addURI(AUTHORITY,"currentPoem",GET_CURRENT_POEM);
        uriMatcher.addURI(AUTHORITY,"delete/currentPoem",DELETE_CURRENT_POEM);
        uriMatcher.addURI(AUTHORITY,"update/poem",UPDATE_POEM);
        uriMatcher.addURI(AUTHORITY,"update/poem/detail",UPDATE_POEM_DETAIL);
        uriMatcher.addURI(AUTHORITY,"delete/poem/detail",DELETE_POEM_DETAIL);
        uriMatcher.addURI(AUTHORITY,"savedPoems",SAVED_POEMS);
        uriMatcher.addURI(AUTHORITY,"savedPoem",SAVED_POEM);
        uriMatcher.addURI(AUTHORITY,"update/currentPoem",UPDATE_CURRENT_POEM);
        uriMatcher.addURI(AUTHORITY,"delete/poem",DELETE_POEM);
        uriMatcher.addURI(AUTHORITY,"insert/statistic/on_save",INSERT_ON_SAVE_STATISTIC);
        uriMatcher.addURI(AUTHORITY,"insert/statistic/continuous",INSERT_CONTINUOUS_STATISTIC);
        uriMatcher.addURI(AUTHORITY,"statistic/continuous",STATISTIC_CONTINUOUS);
        uriMatcher.addURI(AUTHORITY,"statistic/onSave",STATISTIC_ON_SAVE);
        uriMatcher.addURI(AUTHORITY,"update/continuous/statistic",UPDATE_CONTINUOUS_STATISTIC);
        uriMatcher.addURI(AUTHORITY,"update/continuous/award",UPDATE_CONTINUOUS_AWARD);
        uriMatcher.addURI(AUTHORITY,"update/onSave/statistic",UPDATE_ONSAVE_STATISTIC);
        uriMatcher.addURI(AUTHORITY,"update/onSave/award",UPDATE_ONSAVE_AWARD);
        uriMatcher.addURI(AUTHORITY,"statistics/all",ALL_STATISTICS);
        uriMatcher.addURI(AUTHORITY,"update/pack",UPDATE_PACK);
        uriMatcher.addURI(AUTHORITY,"insert/award",INSERT_AWARDS);
        uriMatcher.addURI(AUTHORITY,"insert/award/detail",INSERT_AWARD_DETAILS);
        uriMatcher.addURI(AUTHORITY,"awards",AWARDS);
        uriMatcher.addURI(AUTHORITY,"awards/detail",AWARDS_DETAIL);
        uriMatcher.addURI(AUTHORITY,"update/award",UPDATE_AWARD);
        uriMatcher.addURI(AUTHORITY,"update/award/detail",UPDATE_AWARD_DETAIL);

    }

    public MagnetContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = magnetDatabaseHelper.getWritableDatabase();
        assert database != null;
        int thisDelete;
        switch (uriMatcher.match(uri)) {
            case DELETE_MAGNET:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),null);
                break;
            case DELETE_CURRENT_POEM:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,selection,selectionArgs);
                break;
            case DELETE_POEM_DETAIL:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,selection,selectionArgs);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/savedPoems"),null);
                break;
            case DELETE_POEM:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
       // getContext().getContentResolver().notifyChange(Uri.parse("com.theapp.imapoet.provider.magnetcontentprovider/packs"),null);
        return thisDelete;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = magnetDatabaseHelper.getWritableDatabase();
        assert database != null;
        long thisInsert;
        switch (uriMatcher.match(uri)) {
            case INSERT_CONTINUOUS_AWARD:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_AWARDS_TABLE_NAME, null, values);
                break;
            case INSERT_ON_SAVE_AWARD:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.ON_SAVE_AWARDS_TABLE_NAME, null, values);
                break;
            case INSERT_SETTINGS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.SETTINGS_TABLE_NAME,null,values);
                break;
            case INSERT_POEM:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,null,values);
                break;
            case INSERT_POEM_DETAIL:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,null,values);
                break;
            case INSERT_PACKS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,null,values);
                break;
            case INSERT_PACK_MAGNETS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,null,values);
                break;
            case INSERT_CURRENT_POEM:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,null,values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/currentPoem"),null); // alert the current poem loader
                break;
            case INSERT_CONTINUOUS_STATISTIC:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_STATISTICS_TABLE_NAME,null,values);
                break;
            case INSERT_ON_SAVE_STATISTIC:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.ON_SAVE_STATISTICS_TABLE_NAME,null,values);
                break;
            case INSERT_AWARDS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,null,values);
                break;
            case INSERT_AWARD_DETAILS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,null,values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),null);
        getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/packs"),null);
        return Uri.withAppendedPath(uri,String.valueOf(thisInsert));
    }

    @Override
    public boolean onCreate() {
        magnetDatabaseHelper = new MagnetDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Cursor magnetCursor;
        SQLiteDatabase database = magnetDatabaseHelper.getReadableDatabase();
        if (database == null) throw new AssertionError("the database is null in magnet content provider");
        Context context = getContext();
        if (context == null) throw new AssertionError(" the context is magnet in fish content provider");
        switch (uriMatcher.match(uri)) {
            /*case MAGNET:
                //fishCursor = database.query(MagnetDatabaseContract.MagnetEntry.WORDS_TABLE_NAME, projection, null, null, null, null, sortOrder);
                break;*/

            case AWARD:
                //magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_AWARDS_TABLE_NAME,projection,null,null,null,null,sortOrder);
                magnetCursor = database.rawQuery("SELECT * " +
                        "FROM " + MagnetDatabaseContract.MagnetEntry.ON_SAVE_AWARDS_TABLE_NAME +
                        " UNION ALL " + "SELECT * " + "FROM "+ MagnetDatabaseContract.MagnetEntry.CONTINUOUS_AWARDS_TABLE_NAME, null);
                break;
            case CONTINUOUS_AWARD:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_AWARDS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ON_SAVE_AWARD:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.ON_SAVE_AWARDS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SETTINGS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SETTINGS_TABLE_NAME,projection,null,null,null,null,sortOrder);
                break;
            case PACKS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,projection,null,null,null,null,sortOrder);
                break;
            case MAGNETS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case GET_CURRENT_POEM:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
               // getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),null);
                break;
            case SAVED_POEMS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SAVED_POEM:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case STATISTIC_CONTINUOUS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_STATISTICS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case STATISTIC_ON_SAVE:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.ON_SAVE_STATISTICS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ALL_STATISTICS:
                //magnetCursor = database.rawQuery("SELECT *")
                Cursor continuousStatisticsCursor = database.query(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_STATISTICS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                Cursor onSaveStatisticsCursor = database.query(MagnetDatabaseContract.MagnetEntry.ON_SAVE_STATISTICS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                //magnetCursor =
                magnetCursor = new MergeCursor(new Cursor[]{continuousStatisticsCursor,onSaveStatisticsCursor});
                break;
            case AWARDS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case AWARDS_DETAIL:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        magnetCursor.setNotificationUri(context.getContentResolver(),uri);
        return magnetCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase database = magnetDatabaseHelper.getReadableDatabase();
        if (database == null)
            throw new AssertionError("the database is null in magnet content provider");
        Context context = getContext();
        if (context == null)
            throw new AssertionError(" the context is magnet in fish content provider");
        switch (uriMatcher.match(uri)) {
            case UPDATE_SETTINGS:
                database.update(MagnetDatabaseContract.MagnetEntry.SETTINGS_TABLE_NAME,values,null,null);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings"),null);
                break;
            case UPDATE_POEM:
                database.update(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_POEM_DETAIL:
                database.update(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_CURRENT_POEM:
                database.update(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_CONTINUOUS_STATISTIC:
                database.update(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_STATISTICS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_CONTINUOUS_AWARD:
                database.update(MagnetDatabaseContract.MagnetEntry.CONTINUOUS_AWARDS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_ONSAVE_STATISTIC:
                database.update(MagnetDatabaseContract.MagnetEntry.ON_SAVE_STATISTICS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_ONSAVE_AWARD:
                database.update(MagnetDatabaseContract.MagnetEntry.ON_SAVE_AWARDS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_PACK:
                database.update(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_AWARD:
                database.update(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,values,selection,selectionArgs);
                break;
            case UPDATE_AWARD_DETAIL:
                database.update(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return 0;
    }
}
