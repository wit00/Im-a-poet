package com.theapp.imapoet;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Convenience class for using the awards system more easily
 * Created by whitney on 11/10/14.
 */
public class AwardHandler {
    private Context context;
    private int rawFile;
    private AwardHandlerAsyncQueryHandler asyncQueryHandler;
    private ArrayList<Award> awards = new ArrayList<Award>();
    private AwardManagerListener awardManagerListener;


    public AwardHandler(Context context, int rawFile, AwardHandler.AwardManagerListener awardManagerListener) {
        this.context = context;
        this.rawFile = rawFile;
        asyncQueryHandler = new AwardHandlerAsyncQueryHandler(context.getContentResolver(),this);
        this.awardManagerListener = awardManagerListener;

    }
    public interface AwardManagerListener {
        public void loadAward(String name, String description, int id);
    }

    public void setUpDatabaseAndAttachAwardTypes() {
        (new SetupAwardsBackgroundTask()).execute();
    }

    public void attachAwardTypes() {
        queryAwardsDatabase();
    }

    private void queryAwardsDatabase() {
        asyncQueryHandler.startQuery(1, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"), null, null, null, null);
    }

    private class SetupAwardsBackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<String> awardCodes = new ArrayList<String>();
            ArrayList<Integer> currentValuesForCodes = new ArrayList<Integer>();
            ArrayList<String> awardNames = new ArrayList<String>();
            ArrayList<Integer> currentValuesForNames = new ArrayList<Integer>();
            Cursor awardsCursor = context.getContentResolver().query(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"),null,null,null,null);
            while(awardsCursor.moveToNext()) {
                awardCodes.add(awardsCursor.getString(awardsCursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE)));
                currentValuesForCodes.add(awardsCursor.getInt(awardsCursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE)));
            }
            Cursor awardsDetailCursor = context.getContentResolver().query(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards/detail"),null,null,null,null);
            while(awardsDetailCursor.moveToNext()) {
                awardNames.add(awardsDetailCursor.getString(awardsDetailCursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_NAME)));
                currentValuesForNames.add(awardsDetailCursor.getInt(awardsDetailCursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED)));
            }
            // delete old stuff
            context.getContentResolver().delete(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/awards"),null,null);
            context.getContentResolver().delete(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/award/details"),null,null);
            if(!awardsCursor.isClosed()) awardsCursor.close();
            if(!awardsDetailCursor.isClosed()) awardsDetailCursor.close();
            // get award data
            JSONArray statisticsAndAwards = loadAwardData(context, rawFile);
            try {
                for (int i = 0; i < statisticsAndAwards.length(); i++) {
                    JSONObject pair = statisticsAndAwards.getJSONObject(i);
                    ContentValues statisticValues = new ContentValues();
                    statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE, pair.getString("AWARD_CODE"));
                    int awardValue = 0;
                    if(awardCodes.contains(pair.getString("AWARD_CODE"))) { awardValue = currentValuesForCodes.get(awardCodes.indexOf(pair.getString("AWARD_CODE")));}
                    statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE, awardValue);
                    JSONArray awardList = pair.getJSONArray("AWARD_WIN_CONDITIONS");
                    Uri uri = context.getContentResolver().insert(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award"), statisticValues);
                    int statisticID = Integer.parseInt(uri.getLastPathSegment());
                    for (int j = 0; j < awardList.length(); j++) {
                        JSONObject award = awardList.getJSONObject(j);
                        ContentValues awardsValues = new ContentValues();
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_ID, statisticID);
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_NAME, award.getString("AWARD_NAME"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION, award.getString("DESCRIPTION"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WIN_CONDITION_VALUE, award.getString("WIN_CONDITION"));
                        int completedValue = 0;
                        if(awardNames.contains(award.getString("AWARD_NAME"))) { completedValue = currentValuesForNames.get(awardNames.indexOf(award.getString("AWARD_NAME")));}
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED, completedValue);
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID, award.getString("COMPLETED_IMAGE_ID"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID, award.getString("UNCOMPLETED_IMAGE_ID"));
                        context.getContentResolver().insert(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/detail"),awardsValues);
                    }
                }
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            asyncQueryHandler.startQuery(1, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"), null, null, null, null);
            return null;
        }
        @Override
        protected void onPostExecute(Void nothing) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putInt("awardsVersion",ApplicationContract.AWARDS_VERSION);
            sharedPreferencesEditor.apply();
        }
    }

    /*private class SetupAwardsBackgroundTask2 extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            // delete old stuff
            context.getContentResolver().delete(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/awards"),null,null);
            context.getContentResolver().delete(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/award/details"),null,null);
            // get award data
            JSONArray statisticsAndAwards = loadAwardData(context, rawFile);
            //ContentValues[] allStatisticsAndAwardsContentValues = new ContentValues[statisticsAndAwards.length()];
            try {
                for (int i = 0; i < statisticsAndAwards.length(); i++) {
                    JSONObject pair = statisticsAndAwards.getJSONObject(i);
                    ContentValues statisticValues = new ContentValues();
                    statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE, pair.getString("AWARD_CODE"));
                    statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE, 0);
                    JSONArray awardList = pair.getJSONArray("AWARD_WIN_CONDITIONS");
                    System.out.println("award list length " + Integer.toString(awardList.length()));
                    Uri uri = context.getContentResolver().insert(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award"), statisticValues);
                    int statisticID = Integer.parseInt(uri.getLastPathSegment());
                    for (int j = 0; j < awardList.length(); j++) {
                        JSONObject award = awardList.getJSONObject(j);
                        ContentValues awardsValues = new ContentValues();
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_ID, statisticID);
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_NAME, award.getString("AWARD_NAME"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION, award.getString("DESCRIPTION"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WIN_CONDITION_VALUE, award.getString("WIN_CONDITION"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED, 0);
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID, award.getString("COMPLETED_IMAGE_ID"));
                        awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID, award.getString("UNCOMPLETED_IMAGE_ID"));
                        context.getContentResolver().insert(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/detail"),awardsValues);
                    }
                }
            } catch (JSONException jsonException) {
                System.out.println("json exception");
                jsonException.printStackTrace();
            }
            asyncQueryHandler.startQuery(1, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"), null, null, null, null);
            return null;
        }
        @Override
        protected void onPostExecute(JSONArray statisticsAndAwards) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(ApplicationContract.PREFERENCES_FILE,0);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putInt("awardsVersion",ApplicationContract.AWARDS_VERSION);
            sharedPreferencesEditor.apply();
            System.out.println("shared preferences updated to: " + Integer.toString(sharedPreferences.getInt("awardsVersion",-1)));

            //awardManagerListener.
        }
    }*/



    private void updateAwardCurrentValue(String id, int currentValue) {
        ContentValues updateAwardContentValues = new ContentValues();
        updateAwardContentValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE,currentValue);
        asyncQueryHandler.startUpdate(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/award"),updateAwardContentValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + id,null);
    }

    private void updateAwardDetailValues(String id) {
        ContentValues updateAwardDetailContentValues = new ContentValues();
        updateAwardDetailContentValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,1);
        asyncQueryHandler.startUpdate(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/award/detail"),updateAwardDetailContentValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + id,null);
    }

    private void iterateThroughUncompletedAwards(Award award) {
        for(Iterator<Award.AwardDetail> iterator = award.uncompletedAwards().iterator(); iterator.hasNext();) {
            Award.AwardDetail thisAwardDetail = iterator.next();
            if(thisAwardDetail.winCondition() == award.currentValue()) {
                updateAwardDetailValues(thisAwardDetail.idAsString());
                awardManagerListener.loadAward(thisAwardDetail.name(), thisAwardDetail.description(), thisAwardDetail.id());
                iterator.remove();
            }
        }
    }

    public void newIncrementAction(String awardType) {
        for(Award award : awards) {
            if (award.code().equals(awardType)) {
                award.incrementCurrentValue();
                updateAwardCurrentValue(award.idAsString(), award.currentValue());
                iterateThroughUncompletedAwards(award);
            }
        }
    }

    public void newSetToTrueAction (String awardType) {
        for(Award award : awards) {
            if (award.code().equals(awardType)) {
                award.setCurrentValueToTrue();
                updateAwardCurrentValue(award.idAsString(),award.currentValue());
                iterateThroughUncompletedAwards(award);
            }
        }
    }

    public void newSetValueAction(String awardType, int value) {
        for(Award award : awards) {
            if (award.code().equals(awardType)) {
                award.setCurrentValue(value);
                updateAwardCurrentValue(award.idAsString(), award.currentValue());
                iterateThroughUncompletedAwards(award);
            }
        }
    }

    private String returnAwardData(Context context, int resource) {
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


    public JSONArray loadAwardData(Context context, int resource) {
        String awardData = returnAwardData(context, resource);
        try {
            JSONObject jsonStatisticsAndAwardsFile = new JSONObject(awardData);
            return jsonStatisticsAndAwardsFile.getJSONArray("AWARDS");
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return null;
        }
    }


    private static class AwardHandlerAsyncQueryHandler extends AsyncQueryHandler {
        private final WeakReference<AwardHandler> awardHandlerWeakReference;

        public AwardHandlerAsyncQueryHandler(ContentResolver cr, AwardHandler awardHandler) {
            super(cr);
            awardHandlerWeakReference = new WeakReference<AwardHandler>(awardHandler);
        }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case 1: //
                        while(cursor.moveToNext()) {
                            int id = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID));
                            Award award = new Award(id, cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE)), cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE)));
                            if(awardHandlerWeakReference.get() != null) awardHandlerWeakReference.get().awards.add(award);
                            startQuery(2, award, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards/detail"),null, MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_ID + " = " + Integer.toString(id),null,null);
                            //asyncQueryHandler.startQuery(2, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards/detail"),null, null,null,null);
                        }
                        break;
                    case 2: //
                        ((Award) cookie).addUncompletedAward(cursor);
                        break;
                }
            }
            @Override
            protected  void onInsertComplete(int token, Object cookie, Uri uri) {
                switch (token) {
                    case 1:
                        int statisticID = Integer.parseInt(uri.getLastPathSegment());
                        JSONArray awardList = (JSONArray)cookie;
                        try {
                            for (int j = 0; j < awardList.length(); j++) {
                                JSONObject award = awardList.getJSONObject(j);
                                ContentValues awardsValues = new ContentValues();
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_ID,statisticID);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_NAME,award.getString("AWARD_NAME"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION,award.getString("DESCRIPTION"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WIN_CONDITION_VALUE,award.getString("WIN_CONDITION"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,0);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID,award.getString("COMPLETED_IMAGE_ID"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID,award.getString("UNCOMPLETED_IMAGE_ID"));
                                startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/detail"),awardsValues);
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        startQuery(1, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards"), null, null, null, null);
                        break;
                }
            }
        }
}
