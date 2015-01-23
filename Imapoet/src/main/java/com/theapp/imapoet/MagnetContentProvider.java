package com.theapp.imapoet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.StaleDataException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;


public class MagnetContentProvider extends ContentProvider {
    private MagnetDatabaseHelper magnetDatabaseHelper;
    private static final String AUTHORITY = "com.theapp.imapoet.provider.magnetcontentprovider";
    private static UriMatcher uriMatcher;

    private static final int MAGNET = 1;
    private static final int SETTINGS = 2;
    private static final int UPDATE_SETTINGS = 3;
    private static final int INSERT_SETTINGS = 4;
    private static final int INSERT_POEM = 5;
    private static final int INSERT_POEM_DETAIL = 6;
    private static final int INSERT_PACKS = 7;
    private static final int INSERT_PACK_MAGNETS = 8;
    private static final int PACKS = 9;
    private static final int MAGNETS = 10;
    private static final int DELETE_MAGNET = 11;
    private static final int INSERT_CURRENT_POEM = 12;
    private static final int GET_CURRENT_POEM = 13;
    private static final int DELETE_CURRENT_POEM = 14;
    private static final int UPDATE_POEM = 15;
    private static final int UPDATE_POEM_DETAIL = 16;
    private static final int DELETE_POEM_DETAIL = 17;
    private static final int SAVED_POEMS = 18;
    private static final int SAVED_POEM = 19;
    private static final int UPDATE_CURRENT_POEM = 20;
    private static final int DELETE_POEM = 21;
    private static final int UPDATE_PACK = 22;
    private static final int INSERT_AWARDS = 23;
    private static final int INSERT_AWARD_DETAILS = 24;
    private static final int AWARDS = 25;
    private static final int AWARDS_DETAIL = 26;
    private static final int UPDATE_AWARD = 27;
    private static final int UPDATE_AWARD_DETAIL = 28;
    private static final int DELETE_PACKS = 29;
    private static final int DELETE_MAGNETS = 30;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"magnet",MAGNET);
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
        uriMatcher.addURI(AUTHORITY,"update/pack",UPDATE_PACK);
        uriMatcher.addURI(AUTHORITY,"insert/award",INSERT_AWARDS);
        uriMatcher.addURI(AUTHORITY,"insert/award/detail",INSERT_AWARD_DETAILS);
        uriMatcher.addURI(AUTHORITY,"awards",AWARDS);
        uriMatcher.addURI(AUTHORITY,"awards/detail",AWARDS_DETAIL);
        uriMatcher.addURI(AUTHORITY,"update/award",UPDATE_AWARD);
        uriMatcher.addURI(AUTHORITY,"update/award/detail",UPDATE_AWARD_DETAIL);
        uriMatcher.addURI(AUTHORITY,"delete/magnets",DELETE_MAGNETS);
        uriMatcher.addURI(AUTHORITY,"delete/packs",DELETE_PACKS);
    }

    public MagnetContentProvider() {}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = magnetDatabaseHelper.getWritableDatabase();
        //database.beginTransactionNonExclusive();
        assert database != null;
        int thisDelete;
        switch (uriMatcher.match(uri)) {
            case DELETE_MAGNET:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,selection,selectionArgs);
                //getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),null);
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
            case DELETE_PACKS:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,selection,selectionArgs);
                break;
            case DELETE_MAGNETS:
                thisDelete = database.delete(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
       // getContext().getContentResolver().notifyChange(Uri.parse("com.theapp.imapoet.provider.magnetcontentprovider/packs"),null);
        //database.setTransactionSuccessful();
        //database.endTransaction();
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
        //database.enableWriteAheadLogging();

        assert database != null;
        long thisInsert;

        switch (uriMatcher.match(uri)) {
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
                database.beginTransactionNonExclusive();

                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,null,values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/packs"),null);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case INSERT_PACK_MAGNETS:
                database.beginTransactionNonExclusive();

                //database.enableWriteAheadLogging();
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,null,values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),null);
                database.setTransactionSuccessful();
                database.endTransaction();
                break;
            case INSERT_CURRENT_POEM:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,null,values);
                //getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/currentPoem"),null); // alert the current poem loader
                break;
            case INSERT_AWARDS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,null,values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"),null);
                break;
            case INSERT_AWARD_DETAILS:
                thisInsert = database.insert(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,null,values);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards/detail"),null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

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
        //database.enableWriteAheadLogging();
        //database.beginTransactionNonExclusive();
        Context context = getContext();
        switch (uriMatcher.match(uri)) {
            case SETTINGS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SETTINGS_TABLE_NAME,projection,null,null,null,null,sortOrder);
                break;
            case PACKS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,projection,null,null,null,null,sortOrder);
                magnetCursor.setNotificationUri(context.getContentResolver(),uri);
                break;
            case MAGNETS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.MAGNETS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                magnetCursor.setNotificationUri(context.getContentResolver(),uri);
                break;
            case GET_CURRENT_POEM:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SAVED_POEMS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case SAVED_POEM:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case AWARDS:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                magnetCursor.setNotificationUri(context.getContentResolver(),uri);
                break;
            case AWARDS_DETAIL:
                magnetCursor = database.query(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                magnetCursor.setNotificationUri(context.getContentResolver(),uri);
                break;
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        //database.setTransactionSuccessful();
        //database.endTransaction();
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
                int updateResult = database.update(MagnetDatabaseContract.MagnetEntry.SETTINGS_TABLE_NAME,values,null,null);
                getContext().getContentResolver().notifyChange(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings"),null);
                return updateResult;
            case UPDATE_POEM:
                return database.update(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_TABLE_NAME,values,selection,selectionArgs);
            case UPDATE_POEM_DETAIL:
                return database.update(MagnetDatabaseContract.MagnetEntry.SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME,values,selection,selectionArgs);
            case UPDATE_CURRENT_POEM:
                return database.update(MagnetDatabaseContract.MagnetEntry.LAST_POEM_TABLE_NAME,values,selection,selectionArgs);
            case UPDATE_PACK:
                return database.update(MagnetDatabaseContract.MagnetEntry.PACKS_TABLE_NAME,values,selection,selectionArgs);
            case UPDATE_AWARD:
                return database.update(MagnetDatabaseContract.MagnetEntry.AWARDS_TABLE_NAME,values,selection,selectionArgs);
            case UPDATE_AWARD_DETAIL:
                return database.update(MagnetDatabaseContract.MagnetEntry.AWARDS_DETAIL_TABLE_NAME,values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
