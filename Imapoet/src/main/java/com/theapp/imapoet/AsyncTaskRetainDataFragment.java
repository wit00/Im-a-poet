package com.theapp.imapoet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.theapp.imapoet.AsyncTaskRetainDataFragment.AsyncTaskListener} interface
 * to handle interaction events.
 * Use the {@link AsyncTaskRetainDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AsyncTaskRetainDataFragment extends Fragment {
    private AsyncTaskListener asyncTaskListener;
    private Context applicationContext;
    private boolean runFirstRun = false;
    private boolean runUpdate = false;
    private boolean updateComplete = false;
    private boolean firstRunComplete = false;

    public interface AsyncTaskListener {
        public void updateAsyncLoadingComplete();
        public void firstRunAsyncLoadingComplete();
    }

    public static AsyncTaskRetainDataFragment newInstance() {
        return new AsyncTaskRetainDataFragment();
    }

    public AsyncTaskRetainDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            asyncTaskListener = (AsyncTaskListener) activity;
            applicationContext = activity.getApplicationContext();
            if(runFirstRun) {
                new loadTextFilesIntoPacksAsyncTask().execute();
                runFirstRun = false;
            }
            if(runUpdate) {
                new loadNewTextFilesIntoPacksAsyncTask().execute();
                runUpdate = false;
            }
            if(updateComplete) asyncTaskListener.updateAsyncLoadingComplete();
            if(firstRunComplete) asyncTaskListener.firstRunAsyncLoadingComplete();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        asyncTaskListener = null;
    }

    public void performFirstRunAsyncTask() {
        if(applicationContext != null) {
            new loadTextFilesIntoPacksAsyncTask().execute();
            runFirstRun = false;
        }
        else runFirstRun = true;
    }

    public void performUpdateAsyncTask() {
        if(applicationContext != null) {
            new loadNewTextFilesIntoPacksAsyncTask().execute();
            runUpdate = false;
        } else runUpdate = true;
    }


    private class loadTextFilesIntoPacksAsyncTask extends AsyncTask<Void, Void, ArrayList<Pack>> {
        protected ArrayList<Pack> doInBackground(Void... params) {
            return DataLoaderHelper.loadMagnetText(applicationContext);
        }
        protected void onPostExecute(ArrayList<Pack> packs) {
            (new FirstInsertAsyncTask()).execute(packs);
        }
    }

    private class FirstInsertAsyncTask extends AsyncTask<ArrayList<Pack>, Integer, Void> {
        protected void onPreExecute() {
            firstRunComplete = false;
        }
        protected Void doInBackground(ArrayList<Pack>... packs) {
            ContentResolver contentResolver = applicationContext.getContentResolver();
            contentResolver.delete(ApplicationContract.deleteMagnets_URI,null,null);
            contentResolver.delete(ApplicationContract.deletePacks_URI,null,null);
            ArrayList<ContentValues> magnetContentValues = new ArrayList<ContentValues>();
            for(Pack pack : packs[0]) {
                ContentValues packValues = new ContentValues();
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,pack.title);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED,0);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET,"");
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET_VALUE,0);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE, pack.isAvailable());
                Uri uri = contentResolver.insert(ApplicationContract.insertPacks_URI,packValues);
                int packID = Integer.parseInt(uri.getLastPathSegment());
                for(String magnetString : pack.magnets) {
                    ContentValues magnetValues = new ContentValues();
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,packID);
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,magnetString);
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED_SAVED_OR_SHARED,0);
                    magnetContentValues.add(magnetValues);
                }
            }
            ContentValues[] magnetContentValuesArray = magnetContentValues.toArray(new ContentValues[magnetContentValues.size()]);
            contentResolver.bulkInsert(ApplicationContract.insertMagnet_URI,magnetContentValuesArray);
            return null;
        }
        protected void onPostExecute(Void nothing) {
            if(asyncTaskListener != null) asyncTaskListener.firstRunAsyncLoadingComplete();
            else firstRunComplete = true;
        }
    }
    private class loadNewTextFilesIntoPacksAsyncTask extends AsyncTask<Void, Void, ArrayList<Pack>> {
        protected ArrayList<Pack> doInBackground(Void... params) {
            return DataLoaderHelper.loadNewMagnetText(applicationContext);
        }
        protected void onPostExecute(ArrayList<Pack> packs) {
            (new UpdateAsyncTask()).execute(packs);
        }
    }

    public void signUpForUpdateCompletionNotification(Activity activity) {
        asyncTaskListener = (AsyncTaskListener) activity;
        if(updateComplete) {
            asyncTaskListener.updateAsyncLoadingComplete();
        }
    }

    public void signUpForFirstRunCompletionNotification(Activity activity) {
        asyncTaskListener = (AsyncTaskListener) activity;
        if(firstRunComplete) {
            asyncTaskListener.updateAsyncLoadingComplete();
        }
    }

    private class UpdateAsyncTask extends AsyncTask<ArrayList<Pack>, Integer, Void> {
        protected void onPreExecute() {
            updateComplete = false;
        }
        protected Void doInBackground(ArrayList<Pack>... packs) {
            ContentResolver contentResolver = applicationContext.getContentResolver();
            contentResolver.delete(ApplicationContract.deleteMagnets_URI,null,null);
            contentResolver.delete(ApplicationContract.deletePacks_URI,null,null);
            ArrayList<ContentValues> magnetContentValues = new ArrayList<ContentValues>();
            for(Pack pack : packs[0]) {
                ContentValues packValues = new ContentValues();
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,pack.title);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED,0);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET,"");
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MOST_USED_MAGNET_VALUE,0);
                packValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE, pack.isAvailable());
                Uri uri = contentResolver.insert(ApplicationContract.insertPacks_URI,packValues);
                int packID = Integer.parseInt(uri.getLastPathSegment());
                for(String magnetString : pack.magnets) {
                    ContentValues magnetValues = new ContentValues();
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,packID);
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT,magnetString);
                    magnetValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_TIMES_USED_SAVED_OR_SHARED,0);
                    magnetContentValues.add(magnetValues);
                }
            }
            ContentValues[] magnetContentValuesArray = magnetContentValues.toArray(new ContentValues[magnetContentValues.size()]);
            contentResolver.bulkInsert(ApplicationContract.insertMagnet_URI,magnetContentValuesArray);
            return null;
        }
        protected void onPostExecute(Void nothing) {
            if(asyncTaskListener != null) asyncTaskListener.updateAsyncLoadingComplete();
            else updateComplete = true;
        }
    }

}
