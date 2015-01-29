package com.theapp.imapoet;

import android.content.Context;
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
        return packs;
    }

    public static ArrayList<Pack> loadNewMagnetText(Context context) {
        ArrayList<Pack> packs = new ArrayList<Pack>();
        try {
            ArrayList<String> inAppPurchasePackNames = new ArrayList<String>();
            ArrayList<String> standardPackNames = new ArrayList<String>();
            Collections.addAll(standardPackNames, context.getAssets().list("standardPacks"));
            Collections.addAll(inAppPurchasePackNames, context.getAssets().list("inAppPurchasePacks"));
            for(String packName : standardPackNames) {
                loadPack(context,packs,"standardPacks",packName,true);
            }
            for(String packName : inAppPurchasePackNames) {
                loadPack(context,packs,"inAppPurchasePacks",packName,false);
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
                packs.add(new Pack(packFileName.toLowerCase(), words, isAvailable));
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
            packs.add(new Pack(packFileName.toLowerCase(), words, isAvailable));
            inputStream.close();
            bufferedReader.close();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }


}

