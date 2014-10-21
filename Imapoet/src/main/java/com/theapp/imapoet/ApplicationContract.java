package com.theapp.imapoet;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A collection of uris, strings, etc. to clean up other code
 * Created by whitney on 7/18/14.
 */
public class ApplicationContract {
    ApplicationContract(){}
    public static final String PREFERENCES_FILE = "ImAPoetPreferencesFile";
    public static final String FIRST_LAUNCH = "firstLaunch";
    public static final int[] STANDARD_DECKS = {R.raw.basic,R.raw.science,R.raw.love,R.raw.english_letters};


    public enum AwardStatisticNames {
        lovePack
    }
    protected static final String googleMarketWebAddress = "http://play.google.com/store/apps/details?id=";
    protected static final String googleMarketApplication = "market://details?id=";

    /* award uris */
    protected static final Uri getContinuousAward_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/award/continuous");
    protected static final Uri getOnSaveAward_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/award/onSave");
    protected static final Uri getPacks_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/packs");
    protected static final Uri getMagnets_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets");
    protected static final Uri getStatistics_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/statistics/all");
    protected static final Uri getSavedPoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/savedPoem");
    protected static final Uri deleteCurrentPoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/currentPoem");
    protected static final Uri insertNewCurrentPoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/currentPoem");
    protected static final Uri deletePoemDetailBeforeUpdate_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/poem/detail");
    protected static final Uri insertPoemDetail_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/poem/detail");
    protected static final Uri insertPoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/poem");
    protected static final Uri updatePoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/poem");
    protected static final Uri insertPacks_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/packs");
    protected static final Uri updateCurrentPoem_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/currentPoem");
    protected static final Uri insertMagnet_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/pack/magnet");
    protected static final Uri insertOnSaveAwards_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/on_save");
    protected static final Uri insertContinuousAwards_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/continuous");


    //public ArrayList<String> skuList = new ArrayList<String>(Arrays.asList("Shakespeare","Japanese Alphabet"));


    public enum StatisticNames {
        numberPoemsSaved, numberMagnetsFinishedPoem, updateStatistics,
        magnetsUsedSession,numberPacksUsedSession,deleteStatistic,
        EnglishAlphabet,lovePack,demo;


    }

 }
