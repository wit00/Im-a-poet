package com.theapp.imapoet;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;

/**
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


    //public ArrayList<String> skuList = new ArrayList<String>(Arrays.asList("Shakespeare","Japanese Alphabet"));


    public enum StatisticNames {
        numberPoemsSaved, numberMagnetsFinishedPoem, updateStatistics,
        magnetsUsedSession,numberPacksUsedSession,deleteStatistic,
        EnglishAlphabet,lovePack,demo;


    }
 }
