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
    public static final String DEMO = "demo";

    protected static final String googleMarketWebAddress = "http://play.google.com/store/apps/details?id=";
    protected static final String googleMarketApplication = "market://details?id=";
    protected static final String supportEmailAddress = "support@whichfishthisfish.com";

    /* award uris */
    protected static final Uri getPacks_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/packs");
    protected static final Uri getMagnets_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets");
    protected static final Uri getStatistics_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards");
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
    protected static final Uri updateSettings_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/settings");
    protected static final Uri insertSettings_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/settings");
    protected static final Uri getSettings_URI = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings");

 }
