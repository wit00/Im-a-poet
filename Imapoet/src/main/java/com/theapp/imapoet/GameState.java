package com.theapp.imapoet;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Game State Class holds the async query handler used by the Main Activity.
 * It also holds the DrawingPanelListener that MainActivity implements.
 * Created by whitney on 8/1/14.
 */
public class GameState {
    //private boolean savedPoem = false;
    //private String savedPoemID = null;
    //private String savedPoemName = null;
    private AsyncQueryHandler queryHandler;
    private ArrayList<MagnetTile> currentPoem;
    private Context context;
    private AwardManager awardManager;

    public boolean isLoaded() { return drawingPanelListener.getSavedPoemState(); }
    public String loadedName() { return drawingPanelListener.getSavedPoemName(); }

    private DrawingPanelListener drawingPanelListener;



    public void demoComplete() {
        awardManager.demoComplete();
    }
    public void setSavedPoemState(boolean loaded) {
       // this.savedPoem = loaded;
        drawingPanelListener.setSavedPoemState(loaded);
    }

    public interface DrawingPanelListener {
        public String loadMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn);
        public String loadSavedMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn);
        public void setSettings(boolean soundEffects, boolean music);
        public boolean getSavedPoemState();
        public String getSavedPoemId();
        public String getSavedPoemName();
        public void setSavedPoemId(String savedPoemId);
        public void setSavedPoemState(boolean loaded,String title);
        public void setSavedPoemState(boolean loaded);
    }

    public void magnetDeleted() {
        awardManager.magnetDeleted();
    }
    public void updatePackSize(int numberPacksUsed, int currentPackID, String currentPackName) {
        awardManager.updatePackSize(numberPacksUsed,currentPackID,currentPackName);
    }

    public void magnetTilesChanged(int numberMagnetTiles) {
        awardManager.magnetTilesChanged(numberMagnetTiles);
    }

    public GameState(Context context, DrawingPanelListener drawingPanelListener,Boolean firstLaunch) {
        this.context = context;
        createAsyncQueryHandler();
        this.drawingPanelListener = drawingPanelListener;
        awardManager = new AwardManager(context,(AwardManager.AwardManagerListener) drawingPanelListener);
        if(!firstLaunch) {
            awardManager.setLocalCopiesOfContinuousStatistics();
            awardManager.setLocalCopiesOfOnSaveStatistics();
        }
        //savedPoem = drawingPanelListener.getSavedPoemState();
    }

    /* todo, move this to drawingpanelfragment? */
    public void loadSavedMagnets(Bundle extras) {
        int id = extras.getInt("poem_id");
        drawingPanelListener.setSavedPoemState(true,extras.getString("poem_name"));
        drawingPanelListener.setSavedPoemId(Integer.toString(id));
        String[] projection = {MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT, MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION,
                MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION, MagnetDatabaseContract.MagnetEntry.COLUMN_COLOR, MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID};
       queryHandler.startQuery(4,extras,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/savedPoem"),projection,
                        MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID + " = " + Integer.toString(id),null,null);
    }

    public void loadLastPoem() {
        String[] projection = {MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_X, MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_Y, MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_COLOR,
                MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_TEXT, MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE, MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID, MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID};
        queryHandler.startQuery(1,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/currentPoem"),projection,null,null,null);
    }


    public void deleteMagnet(String currentPack) {
        queryHandler.startQuery(3,this,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets"),
                new String[] {MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,
                        MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT},null,null,
                        MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT + MagnetDatabaseContract.MagnetEntry.ASC);
    }

    /* set the last poem to be loaded on onResume (autosave) */
    public void deleteCurrentPoem(ArrayList<MagnetTile> theCurrentPoem) {
        queryHandler.startDelete(1,theCurrentPoem,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/currentPoem"),null,null);

    }

    public void insertANewPoem(String title, String date, ArrayList<MagnetTile> magnetTiles) {
        drawingPanelListener.setSavedPoemState(true,title);
        ContentValues poemValues = new ContentValues();
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TITLE,title);
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED,date);
        queryHandler.startInsert(1,currentPoem,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/poem"),poemValues);
        currentPoem = magnetTiles;
        awardManager.newPoemInsert(magnetTiles.size());
    }

    public void insertANewMagnet(String newMagnetText, int currentPack) {
        ContentValues magnetValue = new ContentValues();
        magnetValue.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,newMagnetText);
        magnetValue.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,currentPack);
        magnetValue.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED_SAVED_OR_SHARED,0);
        queryHandler.startInsert(3, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/pack/magnet"), magnetValue);
    }
    public void updateAnExistingPoem(String date, ArrayList<MagnetTile> magnetTiles) {
        ContentValues poemValues = new ContentValues();
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED,date);
        queryHandler.startUpdate(1,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/poem"),poemValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + drawingPanelListener.getSavedPoemId(),null);
        currentPoem = magnetTiles;
        awardManager.updatePoem(magnetTiles.size());
    }

    public void insertANewPoemFromText(Pack pack) {
        ContentValues packValues = new ContentValues();
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,pack.title);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED,0);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET,"");
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET_VALUE,0);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE, pack.isAvailable());
        queryHandler.startInsert(2, pack, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/packs"), packValues);

    }





    public void insertStatisticsAndAwardsData(JSONArray statisticsAndAwards, Uri uri, int token) {
        try {
            for (int i = 0; i < statisticsAndAwards.length(); i++) {
                JSONObject pair = statisticsAndAwards.getJSONObject(i);
                ContentValues statisticValues = new ContentValues();
                statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME,pair.getString("STATISTIC_NAME"));
                statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE,0);
                JSONArray awardList = pair.getJSONArray("AWARDS");
                queryHandler.startInsert(token,awardList,uri,statisticValues);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }

    }



    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case 1: //todo, delete?

                        break;
                    case 2:
                        drawingPanelListener.setSettings((cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) != 0),
                                (cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) != 0));
                        break;
                    case 3: // todo work on this one
                        final ArrayList<String> mSelectedItems = new ArrayList<String>();  // Where we track the selected items
                        CharSequence[] wordNames = new CharSequence[cursor.getCount()];
                        final String[] id = new String[cursor.getCount()];
                        int i = 0;
                        while(cursor.moveToNext()) {
                            wordNames[i] = (cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT)));
                            id[i] = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID));
                            i++;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder((Context) cookie);
                        builder.setTitle("Delete")
                                .setMultiChoiceItems(wordNames,null,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which,
                                                                boolean isChecked) {
                                                if (isChecked) {
                                                    // If the user checked the item, add it to the selected items
                                                    mSelectedItems.add(id[which]);
                                                } else if (mSelectedItems.contains(which)) {
                                                    // Else, if the item is already in the array, remove it
                                                        mSelectedItems.remove(Integer.valueOf(which));
                                                }
                                            }
                                        })
                                        // Set the action buttons
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        String[] test = new String[mSelectedItems.size()];
                                        int d = 0;
                                        for(String newID : mSelectedItems) {
                                            test[d] = newID;
                                            d++;
                                        }
                                        queryHandler.startDelete(0, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/magnet"), MagnetDatabaseContract.MagnetEntry._ID + " =? ",test);
                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });

                        (builder.create()).show();
                        break;
                    case 4: // load saved magnets (in on resume)
                        //cursor.moveToFirst();
                        drawingPanelListener.loadSavedMagnets(cursor,
                            MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID,
                            MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,
                            MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION,
                            MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION,
                            MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID);

                        break;
                }


            }
            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {
                if(token == 1) { // update a poem that is in the db
                    queryHandler.startDelete(2,currentPoem,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/delete/poem/detail"),
                            MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID + " = " + drawingPanelListener.getSavedPoemId() /*+ " AND "*/
                                        /*+ MagnetDatabaseContract.MagnetEntry._ID + " = " + Integer.toString(magnetTile.id())*/,null);
                    //}
                }
            }
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                switch (token) {
                    case 1:
                        for(MagnetTile magnetTile : (ArrayList<MagnetTile>)cookie) {
                            ContentValues updateCurrentPoemValues = new ContentValues();
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_X,magnetTile.x());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_Y,magnetTile.y());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_COLOR,magnetTile.magnetColor());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_TEXT,magnetTile.word());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID,magnetTile.packID());
                            if(drawingPanelListener.getSavedPoemState()) {
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,drawingPanelListener.getSavedPoemName());
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,drawingPanelListener.getSavedPoemId());
                            } else {
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,-1);
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,-1);
                            }
                            queryHandler.startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/currentPoem"),updateCurrentPoemValues);
                        }
                        break;
                    case 2: // insert saved poem detail
                        for(MagnetTile magnetTile : currentPoem) {
                            ContentValues poemDetailValues = new ContentValues();
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,drawingPanelListener.getSavedPoemId());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT, magnetTile.word());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION, magnetTile.x());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION, magnetTile.y());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COLOR, magnetTile.magnetColor());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID, magnetTile.packID());
                            queryHandler.startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/poem/detail"),poemDetailValues);
                        }
                        break;
                }

            }
            @Override
            protected  void onInsertComplete(int token, Object cookie, Uri uri) {
                switch (token) {
                    case 1: // insert saved poem detail values
                        String poemID = Integer.toString(Integer.parseInt(uri.getLastPathSegment()));
                        drawingPanelListener.setSavedPoemId(poemID);
                        ContentValues updateCurrentPoemValues = new ContentValues();
                        updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,poemID);
                        updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,drawingPanelListener.getSavedPoemName());
                        queryHandler.startUpdate(0, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/currentPoem"), updateCurrentPoemValues,
                                null, null);

                        for(MagnetTile magnetTile : currentPoem) {
                            ContentValues poemDetailValues = new ContentValues();
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,poemID);
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,magnetTile.word());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION,magnetTile.x());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION,magnetTile.y());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COLOR,magnetTile.magnetColor());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID,magnetTile.packID());
                            queryHandler.startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/poem/detail"),poemDetailValues);
                        }
                        break;
                    case 2:
                        int packID = Integer.parseInt(uri.getLastPathSegment());
                        for(String magnet : ((Pack) cookie).magnets) { // fix this so that the pack is passed along in a bundle
                            ContentValues magnetValues = new ContentValues();
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,packID);
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,magnet);
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED_SAVED_OR_SHARED,0);
                            queryHandler.startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/pack/magnet"),magnetValues);
                        }
                        break;
                    case 3:
                        //DrawerFragment drawerFragment = (DrawerFragment)getSupportFragmentManager().findFragmentById(R.id.drawer_layout);
                        //drawerFragment.restartMainLoader();
                        break;
                    case 4:
                        int statisticID = Integer.parseInt(uri.getLastPathSegment());
                        JSONArray awardList = (JSONArray)cookie;
                        try {
                            for (int j = 0; j < awardList.length(); j++) {
                                JSONObject award = awardList.getJSONObject(j);
                                ContentValues awardsValues = new ContentValues();
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_ID,statisticID);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME,award.getString("AWARD_NAME"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION,award.getString("DESCRIPTION"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE,award.getString("STATISTIC_VALUE"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,0);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID,award.getString("COMPLETED_IMAGE_ID"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID,award.getString("UNCOMPLETED_IMAGE_ID"));
                                queryHandler.startInsert(6, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/on_save"), awardsValues);
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        break;
                    case 5:
                        int statisticsID = Integer.parseInt(uri.getLastPathSegment());
                        JSONArray awardListJSONArray = (JSONArray)cookie;
                        try {
                            for (int j = 0; j < awardListJSONArray.length(); j++) {
                                JSONObject award = awardListJSONArray.getJSONObject(j);
                                ContentValues awardsValues = new ContentValues();
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,0);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_ID,statisticsID);
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME,award.getString("AWARD_NAME"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION,award.getString("DESCRIPTION"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE,award.getString("STATISTIC_VALUE"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID,award.getString("COMPLETED_IMAGE_ID"));
                                awardsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID,award.getString("UNCOMPLETED_IMAGE_ID"));
                                //  contentResolver.insert(Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award"),statisticValues(pair));
                                queryHandler.startInsert(7, null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/award/continuous"), awardsValues);
                            }
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        break;
                    case 6:
                        awardManager.setLocalCopiesOfOnSaveStatistics();
                        break;
                    case 7:
                        awardManager.setLocalCopiesOfContinuousStatistics();
                        break;
                    case 8: // share bitmap
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "this is my text");
                        sendIntent.setType("image/*");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.hello_world)));
                        break;
                }

            }
        };
    }


}

class award {
    private String name;
    private String description;
    private int id;
    private int awardValue;

    public award(String name, String description, int id, int awardValue) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.awardValue = awardValue;
    }

    public String name() { return  name; }
    public String description() { return  description; }
    public int id() { return  id; }
    public int awardValue() { return  awardValue; }
}