package com.theapp.imapoet;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by whitney on 9/17/14.
 */
public class AwardManager {
    private AsyncQueryHandler queryHandler;
    private Context context;
    private AwardManagerListener awardManagerListener;

    public interface AwardManagerListener {
        public void loadAward(String name, String description, int id);
        public int getInitialNumberMagnets();
    }


    public AwardManager(Context context, AwardManagerListener awardManagerListener) {
        this.context = context;
        createAsyncQueryHandler();
        this.awardManagerListener = awardManagerListener;
    }
    /* On save statistic values*/
    private ArrayList<award> onSaveAwards = new ArrayList<award>(); // the awards available for magnets used in a session
    private int currentNumberPoemsSavedStatisticValue; // the statistic value of the number of poems saved
    private String numberPoemsSavedStatistic = "'numberPoemsSaved'"; // the name of the onSave number statistic

    /* number words used finished poem */
    private ArrayList<award> numberMagnetsFinishedPoemAwards = new ArrayList<award>(); // the awards available for the number of magnets used in a finished poem
    private String numberMagnetsFinishedPoemStatistic = "'numberMagnetsFinishedPoem'";
    private int currentNumberMagnetsFinishedPoem;

    /* continuous statistic values */
    /* Number of Magnets Used */
    int currentMagnetsUsedStatisticValue; // the statistic value of greatest number of magnets used in a session
    private int currentMagnetsUsedSessionValue; // number of magnets used in this session
    private ArrayList<award> numberMagnetsUsedAwards = new ArrayList<award>(); // the awards available for magnets used in a session
    private String magnetsUsedStatistic = "'magnetsUsedSession'"; // the name of the magnets used statistic

    /* numberPacksUsed size */
    private int currentPackUsedValues;
    private ArrayList<award> packsUsedAwards = new ArrayList<award>();
    private String numberPacksUsedSession = "'numberPacksUsedSession'";

    /* Pack awards */
    private ArrayList<award> alphabetAwards = new ArrayList<award>(1);
    private String alphabetAwardStatistic = "'EnglishAlphabet'";
    private ArrayList<award> lovePackAward = new ArrayList<award>(1);
    private String lovePackAwardStatistic = "'love'";


    /* Delete awards */
    private ArrayList<award> deletionAwards = new ArrayList<award>();
    private String deletionAwardStatistic = "'deleteStatistic'";
    private int currentDeletions;

    /* Update awards */
    private ArrayList<award> updateAwards = new ArrayList<award>();
    private String updateAwardsStatistic = "'updateStatistics'";
    private int currentUpdates;

    /* demo awards */
    private ArrayList<award> demoAward = new ArrayList<award>(1);





    public void demoComplete() {
       if(demoAward.size() > 0) { // if the demo hasn't already been completed
           updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/statistic",1,"'demo'");
           ContentValues statisticValues = new ContentValues();
           statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,1);
           queryHandler.startUpdate(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/award"),statisticValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + demoAward.get(0).id(),null);
       }
    }

    private void loadAwardAndUpdateOnSave(ArrayList<award> awardArrayList, int statisticValue, String statisticName) {
        if(!updateAwards.isEmpty() && currentUpdates >= updateAwards.get(0).awardValue()) {
            awardManagerListener.loadAward(awardArrayList.get(0).name(), awardArrayList.get(0).description(), awardArrayList.get(0).id());
            updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/award",awardArrayList);
        }
        updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/statistic",statisticValue,statisticName);
    }

    private void loadAwardAndUpdateContinuous(ArrayList<award> awardArrayList, int statisticValue, String statisticName) {
        awardManagerListener.loadAward(awardArrayList.get(0).name(), awardArrayList.get(0).description(), awardArrayList.get(0).id());
        updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/statistic",statisticValue,statisticName);
        updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/award",awardArrayList);
    }

    public void updatePoem(int numberMagnets) {
        currentUpdates ++;
        checkNumberMagnetsFinishedPoemAwards(numberMagnets);
        loadAwardAndUpdateOnSave(updateAwards,currentUpdates,updateAwardsStatistic);
    }

    private boolean thereIsADeleteAward() {
        return ((!deletionAwards.isEmpty()) && (currentDeletions >= deletionAwards.get(0).awardValue()));
    }

    public void magnetDeleted() {
        currentDeletions ++;
        if(thereIsADeleteAward()) {
            loadAwardAndUpdateContinuous(deletionAwards,currentDeletions,deletionAwardStatistic);
        } else updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/statistic",currentDeletions,deletionAwardStatistic);
    }

    private void updateAwardAndStatisticForSeenPack(ArrayList<award> packAward, String packAwardStatistic) {
        if(!packAward.isEmpty() && packAward.get(0).awardValue() == 0) {
            loadAwardAndUpdateContinuous(packAward,1,packAwardStatistic);
        }
    }


    private void checkForParticularPackAwards(String currentPackName) {
        if(currentPackName.equals("English letters")) {
            updateAwardAndStatisticForSeenPack(alphabetAwards,alphabetAwardStatistic);
        }
        else if(currentPackName.equals("love")) {
            updateAwardAndStatisticForSeenPack(lovePackAward,lovePackAwardStatistic);
        }
    }

    public void updatePackSize(int numberPacksUsed,int currentPackID, String currentPackName) {
        if(numberPacksUsed > currentPackUsedValues) {
            currentPackUsedValues = numberPacksUsed;
            // update statistics
            updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/statistic",numberPacksUsed,numberPacksUsedSession);
            if(!packsUsedAwards.isEmpty() && numberPacksUsed >= packsUsedAwards.get(0).awardValue()) {
                // update awards
                awardManagerListener.loadAward(packsUsedAwards.get(0).name(), packsUsedAwards.get(0).description(), packsUsedAwards.get(0).id());
                updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/award",packsUsedAwards);
            }
        }
        checkForParticularPackAwards(currentPackName);

    }

    protected void setLocalCopiesOfContinuousStatistics() {
        queryHandler.startQuery(1,null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/statistic/continuous"),
                new String[]{MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME, MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE},null,null, null);
    }

    protected void setLocalCopiesOfOnSaveStatistics() {
        queryHandler.startQuery(1,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/statistic/onSave"),
                new String[]{MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME, MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE},null,null, null);

    }

    private void setLocalCopiesOfStatistics(String statisticUri) {
        queryHandler.startQuery(1,null,Uri.parse(statisticUri),
                new String[]{MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME, MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE},null,null, null);
    }

    private void updateStatistic(String uri, int value, String statisticName) {
        ContentValues statisticValues = new ContentValues();
        statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE,value);
        queryHandler.startUpdate(0,null,Uri.parse(uri), statisticValues, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME + " = " + statisticName, null);
    }

    /* todo needs to attach to an award object, then update award, then update the nextAwardMagnetUsedSessionValue to the next award*/
    private void checkMagnetsUsedAwards() {
        if(!numberMagnetsUsedAwards.isEmpty() && (currentMagnetsUsedSessionValue == numberMagnetsUsedAwards.get(0).awardValue())) {
            awardManagerListener.loadAward(numberMagnetsUsedAwards.get(0).name(), numberMagnetsUsedAwards.get(0).description(), numberMagnetsUsedAwards.get(0).id());
            updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/award", numberMagnetsUsedAwards);
        }

    }
    public void magnetTilesChanged(int numberMagnetTiles) {
        currentMagnetsUsedSessionValue = numberMagnetTiles;
        if(numberMagnetTiles > currentMagnetsUsedStatisticValue) {
            updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/continuous/statistic",numberMagnetTiles,magnetsUsedStatistic);
            currentMagnetsUsedStatisticValue = numberMagnetTiles;
            checkMagnetsUsedAwards();
        }
    }

    private void updateAndRemoveCurrentAward(String uri, ArrayList<award> awardArrayList) {
        ContentValues statisticValues = new ContentValues();
        //statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE,value);
        statisticValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED,1);
        queryHandler.startUpdate(0,null,Uri.parse(uri),statisticValues, MagnetDatabaseContract.MagnetEntry._ID + " = " + awardArrayList.get(0).id(),null);
        awardArrayList.remove(0);
    }
    private void checkSaveNumberAwards() {
        if(!onSaveAwards.isEmpty() && (currentNumberPoemsSavedStatisticValue >= onSaveAwards.get(0).awardValue())) {
            awardManagerListener.loadAward(onSaveAwards.get(0).name(), onSaveAwards.get(0).description(), onSaveAwards.get(0).id());
            //awardManagerListener.loadAward("","ba", 1);
            updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/award",onSaveAwards);
        }
    }

    private void checkNumberMagnetsFinishedPoemAwards(int numberMagnets) {
        updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/statistic",numberMagnets,numberMagnetsFinishedPoemStatistic);

        if(!numberMagnetsFinishedPoemAwards.isEmpty() && (numberMagnets == numberMagnetsFinishedPoemAwards.get(0).awardValue())) {
            awardManagerListener.loadAward(numberMagnetsFinishedPoemAwards.get(0).name(), numberMagnetsFinishedPoemAwards.get(0).description(), numberMagnetsFinishedPoemAwards.get(0).id());
            updateAndRemoveCurrentAward("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/award", numberMagnetsFinishedPoemAwards);
        }
    }

    public void newPoemInsert(int numberMagnets) {
        currentNumberPoemsSavedStatisticValue ++;
        // System.out.println("statistic value!" + Integer.toString(currentNumberPoemsSavedStatisticValue));
        updateStatistic("content://com.theapp.imapoet.provider.magnetcontentprovider/update/onSave/statistic",currentNumberPoemsSavedStatisticValue,numberPoemsSavedStatistic);
        checkSaveAwards(numberMagnets);
    }
    public void checkSaveAwards(int numberMagnets) {
        checkSaveNumberAwards();
        checkNumberMagnetsFinishedPoemAwards(numberMagnets);
    }

    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case 1:
                        int currentStatisticID;
                        while(cursor.moveToNext()) {
                            String statisticName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME));
                            currentStatisticID = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID));
                            String[] awardValues = new String[]{MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME, MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION,
                                    MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE,
                                    MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_ID};
                            String whereClause = MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_ID + " = " + Integer.toString(currentStatisticID);
                            String orderClause = MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE + MagnetDatabaseContract.MagnetEntry.ASC;
                            if(statisticName.equals("magnetsUsedSession")) {
                                currentMagnetsUsedStatisticValue = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE));
                                queryHandler.startQuery(2,numberMagnetsUsedAwards,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("numberPoemsSaved")) {
                                currentNumberPoemsSavedStatisticValue = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE));
                                queryHandler.startQuery(2,onSaveAwards,ApplicationContract.getOnSaveAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("numberPacksUsedSession")) {
                                currentPackUsedValues = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE));
                                queryHandler.startQuery(2,packsUsedAwards,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("numberMagnetsFinishedPoem")) {
                                queryHandler.startQuery(2,numberMagnetsFinishedPoemAwards,ApplicationContract.getOnSaveAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("EnglishAlphabet")) {
                                queryHandler.startQuery(2,alphabetAwards,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("lovePack")) {
                                queryHandler.startQuery(2,lovePackAward,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("deleteStatistic")) {
                                currentDeletions = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE));
                                queryHandler.startQuery(2,deletionAwards,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("updateStatistics")) {
                                currentUpdates = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE));
                                queryHandler.startQuery(2,updateAwards,ApplicationContract.getOnSaveAward_URI,awardValues, whereClause,null, orderClause);
                            } else if(statisticName.equals("demo")) {
                                queryHandler.startQuery(2,demoAward,ApplicationContract.getContinuousAward_URI,awardValues, whereClause,null, orderClause);

                            }
                        }
                        magnetTilesChanged(awardManagerListener.getInitialNumberMagnets());
                        break;
                    case 2:
                        //System.out.println("cursor poems size: "+Integer.toString(cursor.getCount()));
                        //todo what if cursor is null?
                        while(cursor.moveToNext()) {
                            if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED)) != 1) { // the award is not completed
                                ((ArrayList)cookie).add(new award(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME)),
                                        cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION)),
                                        cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)),
                                        cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_VALUE))));                            }
                        }
                        break;
                }


            }
            @Override
            protected void onUpdateComplete(int token, Object cookie, int result) {

            }
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {

            }
            @Override
            protected  void onInsertComplete(int token, Object cookie, Uri uri) {
                switch (token) {


                }
            }
        };
    }
}
