package com.theapp.imapoet;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple fragment that shows the game statistics, uses a cursor loader
 */
public class StatisticsFragment extends android.support.v4.app.Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>  {

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StatisticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu_statistics, container, false);
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }


    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry._ID,
                MagnetDatabaseContract.MagnetEntry.COLUMN_CODE,
                MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE
        };
        return new CursorLoader(getActivity(), ApplicationContract.getStatistics_URI, projection,null,null,null);
    }

    private boolean isTheSameStatistic(String cursorStatisticName, String statisticName) {
        return cursorStatisticName.equals((statisticName.toString()));
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            String statisticName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE));
            String statisticValue = Integer.toString(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE)));
            if (isTheSameStatistic(statisticName,"SAVED_POEMS")) {
                ((TextView) getView().findViewById(R.id.poems_saved_value)).setText(statisticValue);
            } else if (isTheSameStatistic(statisticName, "SAVED_POEMS_NUMBER_MAGNETS")) {
                ((TextView) getView().findViewById(R.id.number_magnets_finished_poem_value)).setText(statisticValue);
            } else if (isTheSameStatistic(statisticName, "UPDATED_POEM")) {
                ((TextView) getView().findViewById(R.id.poems_update_value)).setText(statisticValue);
            } else if(isTheSameStatistic(statisticName, "MAGNET_ENTERS_CANVAS")) {
                ((TextView) getView().findViewById(R.id.magnets_used_session_value)).setText(statisticValue);
            } else if(isTheSameStatistic(statisticName, "PACK_USED")) {
                ((TextView) getView().findViewById(R.id.number_packs_used_session_value)).setText(statisticValue);
            } else if(isTheSameStatistic(statisticName, "MAGNET_DELETED")) {
                ((TextView) getView().findViewById(R.id.delete_statistic_value)).setText(statisticValue);
            } else if(isTheSameStatistic(statisticName, "SHARED_POEM")) {
                ((TextView) getView().findViewById(R.id.shared_poem_value)).setText(statisticValue);
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {}

}
