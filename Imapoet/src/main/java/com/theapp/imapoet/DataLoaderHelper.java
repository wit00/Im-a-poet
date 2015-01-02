package com.theapp.imapoet;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by whitney on 7/24/14.
 */
public class DataLoaderHelper {
    public DataLoaderHelper(){}
    public static ContentValues returnAwardValues(JSONObject jsonObject) {
        ContentValues values = new ContentValues();
        try {
            values.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME,jsonObject.getString("NAME"));
            values.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION,jsonObject.getString("DESCRIPTION"));
            values.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID, jsonObject.getString("COMPLETED_IMAGE_ID"));
            values.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID, jsonObject.getString("UNCOMPLETED_IMAGE_ID"));
            values.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,jsonObject.getString("COMPLETED"));
            return values;
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }
    }


    private static String returnAwardData(Context context, int resource) {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream award_data_file;
        StringBuilder stringBuilder = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        try {
            award_data_file = context.getResources().openRawResource(resource);
            InputStreamReader inputStreamReader = new InputStreamReader(award_data_file);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(lineSeparator);
                    line = bufferedReader.readLine();
                }
                award_data_file.close();
            }catch(IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (Resources.NotFoundException resourcesNotFoundException) {
            resourcesNotFoundException.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Oops! An important file seems to be missing. Try uninstalling and reinstalling the app from the play store, or go to the settings page in this app and send us an email.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //
                }
            });
            (builder.create()).show();
        }
        return stringBuilder.toString();
    }

    public static JSONArray loadAwardData(Context context, int resource) {
        String awardData = returnAwardData(context, resource);
        try {
            JSONObject jsonStatisticsAndAwardsFile = new JSONObject(awardData);
            JSONArray statisticsAndAwards = jsonStatisticsAndAwardsFile.getJSONArray("STATISTICS_AND_AWARDS");
            return statisticsAndAwards;
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }

    }

    private static ContentValues awardsValues(JSONObject award) {
        ContentValues awardsValues = new ContentValues();
        try {
            awardsValues.put("",award.getString("AWARD_NAME"));
            awardsValues.put("",award.getString("DESCRIPTION"));
            awardsValues.put("",award.getString("STATISTIC_VALUE"));
            awardsValues.put("",award.getString("COMPLETED_IMAGE_ID"));
            awardsValues.put("",award.getString("UNCOMPLETED_IMAGE_ID"));
        }  catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }
        return awardsValues;

    }
    public static ContentValues returnStatisticsValues(JSONArray statisticsAndAwards, int index) {
        ContentValues statisticValues = new ContentValues();
        try {
            JSONObject pair = statisticsAndAwards.getJSONObject(index);
            statisticValues.put("",pair.getString("STATISTIC_NAME"));
            statisticValues.put("",pair.getInt("STATISTIC_ID"));
        }  catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
    }
        return statisticValues;
    }



    public static ArrayList<Pack> loadMagnetText(Context context) {
        ArrayList<Pack> packs = new ArrayList<Pack>();
        loadPackDirectory(context,packs,"standardPacks",true);
        //loadPackDirectory(context,packs,"inAppPurchasePacks",false);
        loadPackDirectory(context,packs,"purchasedInAppPurchasePacks",true);
        loadPackDirectory(context,packs,"notPurchasedInAppPurchasePacks",false);
        return packs;
    }

    // copies a file from one directory to another directory; returns a boolean that indicates whether the copy was successful or not
    public static boolean copyFile(String filename, String fromDirectory, String toDirectory, Context context) {
        try {
            InputStream inputStream = context.getAssets().open(fromDirectory + "/" + filename);
            File outputFile = new File(toDirectory + "/" + filename);
           // OutputStream outputStream = new FileOutputStream(outputFile);
           // OutputStream outputStream =(context.getAssets().open(toDirectory ));
            if(!outputFile.exists()) outputFile.createNewFile();
            //copyFile(fromDirectory,toDirectory);
            //OutputStream outputStream = new FileOutputStream(toDirectory + "/" + filename);
            /*byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }*/
            inputStream.close();
            //outputStream.flush();
            //outputStream.close();
            System.out.println("no io exception here");
        } catch (IOException ioException) {
            System.out.println("i have an io exception");
            ioException.printStackTrace();
            return false;
        }

        return true;
    }

    private static void loadPackDirectory(Context context, ArrayList<Pack> packs, String packsDirectoryName, boolean isAvailable) {
        try {
            for (String packFileName : context.getAssets().list(packsDirectoryName)) {
                InputStream inputStream = context.getAssets().open(packsDirectoryName+"/"+packFileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                ArrayList<String> words = new ArrayList<String>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line != null && !line.isEmpty()) words.add(line.trim());
                }
                packs.add(new Pack(packFileName, words, isAvailable));
                inputStream.close();
                bufferedReader.close();
            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }


}

