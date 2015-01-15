package com.theapp.imapoet;

import android.provider.BaseColumns;

/**
 * Static class that holds the strings used for accessing the database
 * Created by whitney on 7/1/14.
 */
public class MagnetDatabaseContract {
    public MagnetDatabaseContract() {
    }

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ImAPoetDatabase.db";

    public static abstract class MagnetEntry implements BaseColumns {
        public static final String PACKS_TABLE_NAME = "PacksTable";
        public static final String COLUMN_PACK_NAME = "PackName";
        public static final String COLUMN_MOST_USED_MAGNET = "PacksMostUsedMagnet";
        public static final String COLUMN_MOST_USED_MAGNET_VALUE = "PacksMostUsedMagnetValue";
        public static final String COLUMN_TIMES_USED = "PacksTimeUsed";
        public static final String COLUMN_IS_AVAILABLE = "IsPackAvailable";


        public static final String MAGNETS_TABLE_NAME = "MagnetsTable";
        public static final String COLUMN_WORD_TEXT = "wordText";
        public static final String COLUMN_PACK_ID = "pack";
        public static final String COLUMN_TIMES_USED_SAVED_OR_SHARED = "timesUsedInASavedOrSharedPoem";

        public static final String SAVED_POEMS_TABLE_NAME = "SavedPoemsTable";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE_SAVED = "dateSaved";

        public static final String SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME = "SavedPoemsMagnetDetail";
        public static final String COLUMN_POEM_ID = "PoemID";
        public static final String COLUMN_X_LOCATION = "xLocation";
        public static final String COLUMN_Y_LOCATION = "yLocation";
        public static final String COLUMN_TOP = "TopConnectedMagnet";
        public static final String COLUMN_BOTTOM = "BottomConnectedMagnet";
        public static final String COLUMN_LEFT = "LeftConnectedMagnet";
        public static final String COLUMN_RIGHT = "RightConnectedMagnet";

        //public static final String COLUMN_AWARD_NAME = "awardName";
        public static final String COLUMN_COMPLETED = "completed";
        public static final String COLUMN_COMPLETED_IMAGE_ID = "completedImageID";
        public static final String COLUMN_UNCOMPLETED_IMAGE_ID = "uncompletedImageID";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String SETTINGS_TABLE_NAME = "SettingsTable";
        public static final String COLUMN_MUSIC = "music";
        public static final String COLUMN_SOUND_EFFECTS = "soundEffects";

        public static final String LAST_POEM_TABLE_NAME = "LastPoem";
        public static final String COLUMN_MAGNET_PACK_ID = "PackID";
        public static final String COLUMN_MAGNET_X = "MagnetX";
        public static final String COLUMN_MAGNET_Y = "MagnetY";
        public static final String COLUMN_MAGNET_COLOR = "MagnetColor";
        public static final String COLUMN_MAGNET_TEXT = "MagnetText";
        public static final String COLUMN_IF_SAVED_ID = "ifSavedPoemID";
        public static final String COLUMN_IF_SAVED_TITLE = "ifSavedPoemTitle";

        public static final String AWARDS_TABLE_NAME = "AwardsTable";
        public static final String COLUMN_CURRENT_VALUE = "CurrentValue";
        public static final String COLUMN_CODE = "Code";

        public static final String AWARDS_DETAIL_TABLE_NAME = "AwardsDetail";
        public static final String COLUMN_AWARD_ID = "AwardId";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_WIN_CONDITION_VALUE = "WinConditionValue";


        private static final String TEXT_TYPE = " TEXT";
        private static final String INTEGER_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";
        //public static final String COLLATE_NOCASE_ASC = " COLLATE NOCASE ASC";
        //public static final String COLLATE_NOCASE_DESC = " COLLATE NOCASE DESC";
        public static final String ASC = " ASC";
        public static final String DESC = " DESC";

        public static final String CREATE_PACKS_TABLE =
                "CREATE TABLE " + PACKS_TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_PACK_NAME + TEXT_TYPE + COMMA_SEP +
                        COLUMN_TIMES_USED + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_MOST_USED_MAGNET + TEXT_TYPE + COMMA_SEP +
                        COLUMN_MOST_USED_MAGNET_VALUE + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_IS_AVAILABLE + INTEGER_TYPE +
                        ");";

        public static final String CREATE_LAST_SAVED_TABLE =
                "CREATE TABLE " + LAST_POEM_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_MAGNET_X + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_MAGNET_Y + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_MAGNET_COLOR + TEXT_TYPE + COMMA_SEP +
                        COLUMN_MAGNET_TEXT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_MAGNET_PACK_ID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_TOP + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_BOTTOM + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_LEFT + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_RIGHT + TEXT_TYPE + COMMA_SEP  +
                        COLUMN_IF_SAVED_ID + TEXT_TYPE + COMMA_SEP +
                        COLUMN_IF_SAVED_TITLE + TEXT_TYPE +
                        ");";

        public static final String CREATE_MAGNETS_TABLE =
                "CREATE TABLE " + MAGNETS_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_PACK_ID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_WORD_TEXT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_TIMES_USED_SAVED_OR_SHARED + INTEGER_TYPE +
        ");";
        public static final String CREATE_SAVED_POEMS_TABLE =
                "CREATE TABLE " + SAVED_POEMS_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TITLE + TEXT_TYPE + COMMA_SEP +
                        COLUMN_DATE_SAVED + TEXT_TYPE +
                        ");";
        public static final String CREATE_SAVED_POEMS_MAGNET_DETAIL_TABLE =
                "CREATE TABLE " + SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_POEM_ID + TEXT_TYPE + COMMA_SEP +
                        COLUMN_MAGNET_PACK_ID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_WORD_TEXT + TEXT_TYPE + COMMA_SEP +
                        COLUMN_X_LOCATION + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_Y_LOCATION + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_TOP + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_BOTTOM + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_LEFT + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_RIGHT + TEXT_TYPE +
                        ");";

        public static final String CREATE_AWARDS_TABLE =
                "CREATE TABLE " + AWARDS_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_CURRENT_VALUE + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_CODE + TEXT_TYPE +
                        ");";

        public static final String CREATE_AWARDS_DETAIL_TABLE =
                "CREATE TABLE " + AWARDS_DETAIL_TABLE_NAME+ " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_AWARD_ID + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                        COLUMN_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                        COLUMN_WIN_CONDITION_VALUE + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_COMPLETED_IMAGE_ID + TEXT_TYPE + COMMA_SEP +
                        COLUMN_UNCOMPLETED_IMAGE_ID + TEXT_TYPE + COMMA_SEP +
                        COLUMN_COMPLETED + INTEGER_TYPE +
                        ");";

        public static final String CREATE_SETTINGS_TABLE =
                "CREATE TABLE " + SETTINGS_TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_MUSIC + INTEGER_TYPE + COMMA_SEP +
                        COLUMN_SOUND_EFFECTS + INTEGER_TYPE +
                        ");";


        public static final String SQL_DELETE_PACKS = "DROP TABLE IF EXISTS " + PACKS_TABLE_NAME;
        public static final String SQL_DELETE_MAGNETS = "DROP TABLE IF EXISTS " + MAGNETS_TABLE_NAME;
        public static final String SQL_DELETE_SAVED_POEMS = "DROP TABLE IF EXISTS " + SAVED_POEMS_TABLE_NAME;
        public static final String SQL_DELETE_SAVED_POEMS_DETAIL = "DROP TABLE IF EXISTS " + SAVED_POEMS_MAGNET_DETAIL_TABLE_NAME;
        public static final String SQL_DELETE_SETTINGS = "DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME;
        public static final String SQL_DELETE_LAST_POEM = "DROP TABLE IF EXISTS " + LAST_POEM_TABLE_NAME;
        public static final String SQL_DELETE_AWARDS = "DROP TABLE IF EXISTS " + AWARDS_TABLE_NAME;
        public static final String SQL_DELETE_AWARDS_DETAIL = "DROP TABLE IF EXISTS " + AWARDS_DETAIL_TABLE_NAME;



    }
}
