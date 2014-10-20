package com.theapp.imapoet;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.theapp.imapoet.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link //Callbacks}
 * interface.
 */
public class StatisticsFragment extends android.support.v4.app.Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>  {
    private ListView listView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StatisticsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        //setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu_statistics, container, false);
        getLoaderManager().initLoader(0, null, this);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }




    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME,
                MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE
        };

        return new CursorLoader(getActivity(), Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/statistics/all"),
                projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME + MagnetDatabaseContract.MagnetEntry.ASC);
    }


    private boolean isTheSameStatistic(String cursorStatisticName, ApplicationContract.StatisticNames statisticName) {
        return cursorStatisticName.equals((statisticName.toString()));
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            //System.out.println("statistic name: "+cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME)));
            String statisticName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_STATISTIC_NAME));
            String statisticValue = Integer.toString(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_VALUE)));
            if (isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.numberPoemsSaved)) {
                ((TextView) getView().findViewById(R.id.poems_saved_value)).setText(statisticValue);
            } else if (isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.numberMagnetsFinishedPoem)) {
                ((TextView) getView().findViewById(R.id.number_magnets_finished_poem_value)).setText(statisticValue);
            } else if (isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.updateStatistics)) {
                ((TextView) getView().findViewById(R.id.poems_update_value)).setText(statisticValue);
            } else if(isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.magnetsUsedSession)) {
                ((TextView) getView().findViewById(R.id.magnets_used_session_value)).setText(statisticValue);

            } else if(isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.numberPacksUsedSession)) {
                ((TextView) getView().findViewById(R.id.number_packs_used_session_value)).setText(statisticValue);

            } else if(isTheSameStatistic(statisticName, ApplicationContract.StatisticNames.deleteStatistic)) {
                ((TextView) getView().findViewById(R.id.delete_statistic_value)).setText(statisticValue);

            }

        }
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
