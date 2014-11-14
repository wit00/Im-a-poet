package com.theapp.imapoet;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


/**
 * A simple fragment that shows the game statistics, uses a cursor loader
 */
public class StatisticsFragment extends android.support.v4.app.Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>  {
    private ListView statisticsListView;
    private StatisticsFragmentListAdapter statisticsFragmentListAdapter;


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
        statisticsListView = (ListView) rootView.findViewById(R.id.statistics_list);
        statisticsFragmentListAdapter = new StatisticsFragmentListAdapter(getActivity(),R.layout.fragment_main_menu_statistics_row,null,
                new String[] {MagnetDatabaseContract.MagnetEntry._ID, MagnetDatabaseContract.MagnetEntry.COLUMN_CODE, MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE},new int[]{android.R.id.text1});
        statisticsListView.setAdapter(statisticsFragmentListAdapter);
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry._ID,
                MagnetDatabaseContract.MagnetEntry.COLUMN_CODE,
                MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE
        };
        String selection = MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "SAVED_POEMS' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "SAVED_POEMS_NUMBER_MAGNETS' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "UPDATED_POEM' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "MAGNET_ENTERS_CANVAS' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "PACK_USED' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "MAGNET_DELETED' OR " + MagnetDatabaseContract.MagnetEntry.COLUMN_CODE + " = '" + "SHARED_POEM'";
        return new CursorLoader(getActivity(), ApplicationContract.getStatistics_URI, projection, selection ,null,null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        statisticsFragmentListAdapter.swapCursor(cursor);
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {
        statisticsFragmentListAdapter.swapCursor(null);
    }
}
