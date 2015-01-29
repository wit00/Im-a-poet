package com.theapp.imapoet;



import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SavedPoemsFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {
    //private SimpleCursorAdapter simpleCursorAdapter;
    private SavedPoemsListAdapter savedPoemsListAdapter;

    public SavedPoemsFragment() {
        // Required empty public constructor
    }

    public static SavedPoemsFragment newInstance() {
        return new SavedPoemsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_saved_poem, container, false);
        AbsListView listView = (AbsListView) rootView.findViewById(R.id.saved_poems_listview);
         savedPoemsListAdapter = new SavedPoemsListAdapter(getActivity(),R.layout.fragment_saved_poems_list_row,null,
                new String[] {MagnetDatabaseContract.MagnetEntry.COLUMN_TITLE},new int[]{android.R.id.text1});
        listView.setAdapter(savedPoemsListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // load saved poem
                Intent loadMainActivityIntent = new Intent(getActivity(), MainActivity.class);
                loadMainActivityIntent.putExtra("poem_id", savedPoemsListAdapter.getIDFromPosition(position));
                loadMainActivityIntent.putExtra("poem_name", savedPoemsListAdapter.getNameFromPosition(position));
                getActivity().startActivity(loadMainActivityIntent);
            }
        });
        getLoaderManager().initLoader(0,null,this);
        return rootView;
    }


    private void loadNoPoems() {
        (getView().findViewById(R.id.no_poems)).setVisibility(View.VISIBLE);
        (getView().findViewById(R.id.saved_poems_listview)).setVisibility(View.GONE);


    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry._ID,
                MagnetDatabaseContract.MagnetEntry.COLUMN_TITLE,
                MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED
        };

        return new CursorLoader(getActivity(), Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/savedPoems"),
                projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED + MagnetDatabaseContract.MagnetEntry.DESC);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() == 0) loadNoPoems();
        savedPoemsListAdapter.swapCursor(cursor);
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {
        savedPoemsListAdapter.swapCursor(null);
    }

}
