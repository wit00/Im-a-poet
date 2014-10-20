package com.theapp.imapoet;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by whitney on 9/17/14.
 */
public class DatabaseManager {
    private AsyncQueryHandler queryHandler;
    private Context context;

    public DatabaseManager(Context context) {
        this.context = context;
        createAsyncQueryHandler();
    }

    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {

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
