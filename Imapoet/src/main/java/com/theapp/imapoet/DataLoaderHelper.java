package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Convenience class for handling data loading operations
 * Created by whitney on 7/24/14.
 */
public class DataLoaderHelper {
    public DataLoaderHelper(){}

    public static ArrayList<Pack> loadMagnetText(Context context) {
        ArrayList<Pack> packs = new ArrayList<Pack>();
        loadPackDirectory(context,packs,"standardPacks",true);
        loadPackDirectory(context,packs,"inAppPurchasePacks",false);
        //loadPackDirectory(context,packs,"purchasedInAppPurchasePacks",true);
        //loadPackDirectory(context,packs,"notPurchasedInAppPurchasePacks",false);
        return packs;
    }

    public static ArrayList<Pack> loadNewMagnetText(Context context) {
        ArrayList<Pack> packs = new ArrayList<Pack>();
        //Cursor cursor = context.getContentResolver().query(ApplicationContract.getPacks_URI, new String[]{MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME}, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + " = 1", null, null);
        try {
            ArrayList<String> inAppPurchasePackNames = new ArrayList<String>();
            ArrayList<String> standardPackNames = new ArrayList<String>();
            ArrayList<String> cursorPackNames = new ArrayList<String>();
            ArrayList<Pack> cursorPacks = new ArrayList<Pack>();
            Collections.addAll(standardPackNames, context.getAssets().list("standardPacks"));
            Collections.addAll(inAppPurchasePackNames, context.getAssets().list("inAppPurchasePacks"));
           // cursor.moveToFirst();
            //if(cursor.getCount() > 0) Collections.addAll(cursorPackNames, cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME)));
            for(String packName : standardPackNames) {
                loadPack(context,packs,"standardPacks",packName,true);
            }
            for(String packName : inAppPurchasePackNames) {
                loadPack(context,packs,"inAppPurchasePacks",packName,false);

               /* if(cursorPacks.contains(packName)) {
                    loadPack(context,packs,"inAppPurchasePacks",packName,true);
                } else {
                    loadPack(context,packs,"inAppPurchasePacks",packName,false);
                }*/
            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        return packs;
    }


    private static void loadPackDirectory(Context context, ArrayList<Pack> packs, String packsDirectoryName, boolean isAvailable) {
        try {
            for (String packFileName : context.getAssets().list(packsDirectoryName)) {
                InputStream inputStream = context.getAssets().open(packsDirectoryName+"/"+packFileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                ArrayList<String> words = new ArrayList<String>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isEmpty()) words.add(line.trim());
                }
                packs.add(new Pack(packFileName, words, isAvailable));
                inputStream.close();
                bufferedReader.close();
            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private static void loadPack(Context context, ArrayList<Pack> packs, String packsDirectoryName, String packFileName, boolean isAvailable) {
        try {
            InputStream inputStream = context.getAssets().open(packsDirectoryName+"/"+packFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            ArrayList<String> words = new ArrayList<String>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) words.add(line.trim());
            }
            packs.add(new Pack(packFileName, words, isAvailable));
            inputStream.close();
            bufferedReader.close();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }


}

