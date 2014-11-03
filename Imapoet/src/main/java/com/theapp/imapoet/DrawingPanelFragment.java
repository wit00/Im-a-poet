package com.theapp.imapoet;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * The drawing panel fragment is an android fragment element that holds the surface view (canvas) where the magnet drawing takes place.
 * Created by whitney on 8/1/14.
 */
public class DrawingPanelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private AsyncQueryHandler queryHandler;
    private DrawingPanel.CanvasListener canvasListener;
    private DrawingPanel drawingPanel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        createAsyncQueryHandler();
        drawingPanel = new DrawingPanel(getActivity(),canvasListener);
        return drawingPanel;
    }

    public void loadMagnets(){
        getLoaderManager().initLoader(0,null,this);
    }

    public DrawingPanel drawingPanel() { return drawingPanel; }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            canvasListener = (DrawingPanel.CanvasListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    public void setSettings(boolean soundEffects, boolean music) {
        drawingPanel.setSettings(soundEffects, music);
    }
    public int setWord(String word, int packID){
        return drawingPanel.setWord(word,packID);
    }


    public ArrayList<Magnet> getPoem() {
        return drawingPanel.getPoem();
    }

    public void clearMagnets() {
        drawingPanel.clearMagnets();
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadSettings();
    }


    private void loadSettings() {
        queryHandler.startQuery(1,null, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings"),
                new String[] {MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS, MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC},
                null, null, null);
    }
    private void createAsyncQueryHandler() {
        queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                switch (token) {
                    case 1:
                        if(cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            setSettings((cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) != 0),
                                    (cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) != 0));
                        } else {
                            setSettings(true,true);
                        }
                        break;
                }
            }
        };
    }
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_X, MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_Y,   MagnetDatabaseContract.MagnetEntry.COLUMN_TOP,
                MagnetDatabaseContract.MagnetEntry.COLUMN_BOTTOM,
                MagnetDatabaseContract.MagnetEntry.COLUMN_LEFT,
                MagnetDatabaseContract.MagnetEntry.COLUMN_RIGHT,
                MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_TEXT, MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_TITLE, MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID, MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID};
        //queryHandler.startQuery(1,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/currentPoem"),projection,null,null,null);

        return new CursorLoader(getActivity(),Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/currentPoem"),
                projection,null,null, null);
    }



    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
       if(cursor.getCount() > 0) {
           cursor.moveToFirst();
           drawingPanel.loadMagnets(cursor,
                   MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_PACK_ID,
                   MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_TEXT,
                   MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_X,
                   MagnetDatabaseContract.MagnetEntry.COLUMN_MAGNET_Y,
                   MagnetDatabaseContract.MagnetEntry.COLUMN_IF_SAVED_ID);
       }
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {
        //listView.setAdapter(null);

    }
}


