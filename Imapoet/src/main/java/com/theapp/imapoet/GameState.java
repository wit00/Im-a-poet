package com.theapp.imapoet;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import java.util.ArrayList;

/**
 * Game State Class holds the async query handler used by the Main Activity.
 * It also holds the DrawingPanelListener that MainActivity implements.
 * Created by whitney on 8/1/14.
 */
public class GameState {
    private AsyncQueryHandler queryHandler;
    private ArrayList<Magnet> currentPoem;
    private Context context;
    private DrawingPanelListener drawingPanelListener;
    private AwardHandler awardHandler;

    public GameState(Context context, DrawingPanelListener drawingPanelListener,Boolean firstLaunch) {
        this.context = context;
        createAsyncQueryHandler();
        this.drawingPanelListener = drawingPanelListener;
        awardHandler = new AwardHandler(context,R.raw.award_data,(AwardHandler.AwardManagerListener) drawingPanelListener);
        if(!firstLaunch) {
            awardHandler.attachAwardTypes();
        } else {
            awardHandler.setUpDatabaseAndAttachAwardTypes();
        }
    }
    // Called by MainActivity, tells the award manager that the demo has been completed
    public void demoComplete() { awardHandler.newSetToTrueAction("DEMO_FINISHED"); }

    // Called by MainActivity when the screen is cleared; sets the autoSave poem state to false
    public void setSavedPoemState(boolean loaded) { drawingPanelListener.setSavedPoemState(loaded);}

    public interface DrawingPanelListener {
        public String loadSavedMagnets(Cursor cursor, String packIDColumn, String textColumn, String xColumn, String yColumn, String idColumn);
        public boolean getSavedPoemState();
        public String getSavedPoemId();
        public String getSavedPoemName();
        public void setSavedPoemId(String savedPoemId);
        public void setSavedPoemState(boolean loaded,String title);
        public void setSavedPoemState(boolean loaded);
    }

    public void magnetDeleted() {
        awardHandler.newIncrementAction("MAGNET_DELETED");
    }
    public void updatePackSize(int numberPacksUsed, int currentPackID, String currentPackName) {
        awardHandler.newSetValueAction("PACK_USED",numberPacksUsed);
        if(currentPackName.equals("english_letters.txt")) awardHandler.newSetToTrueAction("ENGLISH_ALPHABET_PACK_USED");
        if(currentPackName.equals("love.txt")) awardHandler.newSetToTrueAction("LOVE_PACK_USED");
    }

    public void magnetTilesChanged(int numberMagnetTiles) {
        awardHandler.newSetValueAction("MAGNET_ENTERS_CANVAS",numberMagnetTiles);
    }

    /* Loads a saved poem from the database when the user loads a saved poem from the saved poems fragment in MainMenu.*/
    public void loadSavedMagnets(Bundle extras) {
        int id = extras.getInt("poem_id");
        drawingPanelListener.setSavedPoemState(true,extras.getString("poem_name"));
        drawingPanelListener.setSavedPoemId(Integer.toString(id));
        String[] projection = {MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT, MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION, MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION, MagnetDatabaseContract.MagnetEntry.COLUMN_TOP, MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM, MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT, MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT, MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID};
       queryHandler.startQuery(LoaderCodes.savedMagnets,extras,ApplicationContract.getSavedPoem_URI,projection,MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID + " = " + Integer.toString(id),null,null);
    }

    /* set the last poem to be loaded on onResume (autosave) */
    public void setCurrentPoemForAutoSave(ArrayList<Magnet> theCurrentPoem) {
        queryHandler.startDelete(LoaderCodes.deleteOldAutoSavedPoem,theCurrentPoem,ApplicationContract.deleteCurrentPoem_URI,null,null);
    }

    public void insertANewPoem(String title, String date, ArrayList<Magnet> magnets) {
        drawingPanelListener.setSavedPoemState(true,title);
        ContentValues poemValues = new ContentValues();
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TITLE,title);
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED,date);
        queryHandler.startInsert(LoaderCodes.insertNewPoem,currentPoem,ApplicationContract.insertPoem_URI,poemValues);
        currentPoem = magnets;
        awardHandler.newIncrementAction("SAVED_POEMS");
        awardHandler.newSetValueAction("SAVED_POEMS_NUMBER_MAGNETS",magnets.size());
    }

    public void updateAnExistingPoem(String date, ArrayList<Magnet> magnets) {
        ContentValues poemValues = new ContentValues();
        poemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED,date);
        queryHandler.startUpdate(LoaderCodes.updatePoem,null,ApplicationContract.updatePoem_URI,poemValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + drawingPanelListener.getSavedPoemId(),null);
        currentPoem = magnets;
        awardHandler.newIncrementAction("UPDATED_POEM");
        awardHandler.newSetValueAction("SAVED_POEMS_NUMBER_MAGNETS",magnets.size());
    }

    // during first use or when the application is updated, this function inserts the packs (and later magnets) from the text files
    public void insertPacksFromTextFiles(Pack pack) {
        ContentValues packValues = new ContentValues();
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,pack.title);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED,0);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET,"");
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET_VALUE,0);
        packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE, pack.isAvailable());
        queryHandler.startInsert(LoaderCodes.insertPacks, pack, ApplicationContract.insertPacks_URI, packValues);
    }


    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case LoaderCodes.savedMagnets: // load saved magnets (in on resume)
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
                if(token == LoaderCodes.updatePoem) { // update a poem that is in the db
                    queryHandler.startDelete(LoaderCodes.updateSavedPoemDetail,currentPoem,ApplicationContract.deletePoemDetailBeforeUpdate_URI,MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID + " = " + drawingPanelListener.getSavedPoemId(),null);
                }
            }
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                switch (token) {
                    case LoaderCodes.deleteOldAutoSavedPoem:
                        for(Magnet magnet : (ArrayList<Magnet>)cookie) {
                            ContentValues updateCurrentPoemValues = new ContentValues();
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_X, magnet.x());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_Y, magnet.y());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_COLOR, magnet.magnetColor());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_TEXT, magnet.word());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID, magnet.packID());
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TOP, magnet.getConnectedMagnetsString(magnet.topSideConnectedMagnet()));
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM, magnet.getConnectedMagnetsString(magnet.bottomSideConnectedMagnet()));
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT, magnet.getConnectedMagnetsString(magnet.leftSideConnectedMagnet()));
                            updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT, magnet.getConnectedMagnetsString(magnet.rightSideConnectedMagnet()));
                            if(drawingPanelListener.getSavedPoemState()) {
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,drawingPanelListener.getSavedPoemName());
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,drawingPanelListener.getSavedPoemId());
                            } else {
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,-1);
                                updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,-1);
                            }
                            queryHandler.startInsert(0,null,ApplicationContract.insertNewCurrentPoem_URI,updateCurrentPoemValues);
                        }
                        break;
                    case LoaderCodes.updateSavedPoemDetail: // insert saved poem detail
                        for(Magnet magnet : currentPoem) {
                            ContentValues poemDetailValues = new ContentValues();
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,drawingPanelListener.getSavedPoemId());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT, magnet.word());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION, magnet.x());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION, magnet.y());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TOP, magnet.getConnectedMagnetsString(magnet.topSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM, magnet.getConnectedMagnetsString(magnet.bottomSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT, magnet.getConnectedMagnetsString(magnet.leftSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT, magnet.getConnectedMagnetsString(magnet.rightSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID, magnet.packID());
                            queryHandler.startInsert(0,null,ApplicationContract.insertPoemDetail_URI,poemDetailValues);
                        }
                        break;
                }
            }
            @Override
            protected  void onInsertComplete(int token, Object cookie, Uri uri) {
                switch (token) {
                    case LoaderCodes.insertNewPoem: // insert saved poem detail values
                        String poemID = Integer.toString(Integer.parseInt(uri.getLastPathSegment()));
                        drawingPanelListener.setSavedPoemId(poemID);
                        ContentValues updateCurrentPoemValues = new ContentValues();
                        updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID,poemID);
                        updateCurrentPoemValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE,drawingPanelListener.getSavedPoemName());
                        queryHandler.startUpdate(0, null, ApplicationContract.updateCurrentPoem_URI, updateCurrentPoemValues,
                                null, null);

                        for(Magnet magnet : currentPoem) {
                            ContentValues poemDetailValues = new ContentValues();
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_POEM_ID,poemID);
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT, magnet.word());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_X_LOCATION, magnet.x());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_Y_LOCATION, magnet.y());
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TOP, magnet.getConnectedMagnetsString(magnet.topSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM, magnet.getConnectedMagnetsString(magnet.bottomSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT, magnet.getConnectedMagnetsString(magnet.leftSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT, magnet.getConnectedMagnetsString(magnet.rightSideConnectedMagnet()));
                            poemDetailValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID, magnet.packID());
                            queryHandler.startInsert(0,null,ApplicationContract.insertPoemDetail_URI,poemDetailValues);
                        }
                        break;
                    case LoaderCodes.insertPacks:
                        int packID = Integer.parseInt(uri.getLastPathSegment());
                        for(String magnet : ((Pack) cookie).magnets) { // fix this so that the pack is passed along in a bundle
                            ContentValues magnetValues = new ContentValues();
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,packID);
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,magnet);
                            magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED_SAVED_OR_SHARED,0);
                            queryHandler.startInsert(0,null,ApplicationContract.insertMagnet_URI,magnetValues);
                        }
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

class LoaderCodes {
    static final int savedMagnets = 1;
    static final int deleteOldAutoSavedPoem = 2;
    static final int updateSavedPoemDetail = 3;
    static final int updatePoem = 4;
    static final int insertNewPoem = 5;
    static final int insertPacks = 6;
}