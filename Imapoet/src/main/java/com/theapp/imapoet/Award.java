package com.theapp.imapoet;

import android.database.Cursor;

import java.util.ArrayList;

/**
 * Object containing the parts of an award
 * Created by whitney on 11/10/14.
 */
public class Award {
    private int id;
    private String idAsString;
    private int currentValue;
    private String code;
    private ArrayList<AwardDetail> uncompletedAwards = new ArrayList<AwardDetail>();

    public Award(int id, int currentValue, String code) {
        this.id = id;
        idAsString = Integer.toString(id);
        this.currentValue = currentValue;
        this.code = code;
    }

    public void addUncompletedAward(String name, String description, int winCondition, int awardID, int id) {
        uncompletedAwards.add(new AwardDetail(name,description,winCondition,awardID,id));
    }

    public void addUncompletedAward(Cursor cursor) {
        while(cursor.moveToNext()) {
            if(!(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED)) == 1)) {
                addUncompletedAward(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_NAME)),cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION)),cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WIN_CONDITION_VALUE)),cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_ID)),cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
            }
        }
    }

    public int id() { return  id; }
    public String idAsString() { return  idAsString; }
    public String code() { return  code; }
    public ArrayList<AwardDetail> uncompletedAwards() { return  uncompletedAwards; }
    public int currentValue() { return currentValue; }
    public void incrementCurrentValue() { currentValue ++; }
    public void setCurrentValue(int newValue) { currentValue = newValue; }
    public void setCurrentValueToTrue() { currentValue = 1; }


    class AwardDetail {
        private String name;
        private String description;
        private int winCondition;
        private int awardID;
        private int id;
        private String idAsString;

        public AwardDetail(String name, String description, int winCondition, int awardID, int id) {
            this.name = name;
            this.description = description;
            this.winCondition = winCondition;
            this.awardID = awardID;
            this.id = id;
            this.idAsString = Integer.toString(id);
        }

        public String name() { return name; }
        public String description() { return description; }
        public int winCondition() { return winCondition; }
        public String idAsString() { return idAsString; }
        public int id() { return id; }
    }
}

