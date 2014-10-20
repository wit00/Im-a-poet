package com.theapp.imapoet;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;


/* The DrawerFragment is the drawer that sits in the MainActivity and can be opened by swiping the left edge or pressing the top left button on the screen. It holds the Packs and Magnets that can be dragged onto the canvas */
public class DrawerFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener,  LoaderManager.LoaderCallbacks<Cursor>  {
    private OnFragmentInteractionListener drawerFragmentListener;
    protected DrawerLayout drawerLayout;
    private GridView gridView;
    private DrawerMagnetsAdapter drawerMagnetsAdapter;
    private DrawerSpinnerAdapter drawerSpinnerAdapter;
    private int spinnerPosition = 0;

    // Required empty public constructor
    public DrawerFragment() {}

    private void setGridViewListener() {
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                view.startDrag(null, new View.DragShadowBuilder(view), null, 0);
                if(drawerLayout != null) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.pager);
                    drawerLayout.closeDrawers();
                }
                Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(position);
                drawerFragmentListener.onDrawerMagnetClicked(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT)),
                        cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID)));
                return true;
            }
        });
    }

    // override the onCreate method to instantiate and run the cursor loader
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0,null,this);
    }

    private void setupMagnetDeckSpinner(View currentView) {
        Spinner spinner = (Spinner) currentView.findViewById(R.id.sets_spinner);
        drawerSpinnerAdapter = new DrawerSpinnerAdapter(getActivity(), R.layout.fragment_drawer_spinner_layout, null, new String[]{}, new int[]{});
        drawerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(drawerSpinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }
    
    private void setupGridView(View currentView) {
        gridView = (GridView) currentView.findViewById(R.id.gridview);
        drawerMagnetsAdapter = new DrawerMagnetsAdapter(getActivity(), null, new String[]{}, new int[]{});
        gridView.setAdapter(drawerMagnetsAdapter);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View currentView = inflater.inflate(R.layout.fragment_drawer, container, false);
        setupMagnetDeckSpinner(currentView);
        setupGridView(currentView);
        setGridViewListener();
        return currentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            drawerFragmentListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        drawerFragmentListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onDrawerMagnetClicked(String clickedMagnetText, int packID);
        public void onSpinnerClicked();
    }

    public void restartMainLoader() {
        Bundle bundle = new Bundle();
        bundle.putInt("id",getCurrentPack());
        getLoaderManager().restartLoader(1, bundle, this);
    }


    /** onItemSelected and onNothingSelected are parts of the interface for the magnet deck spinner **/
    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(position);
        if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE))==0) {
            //toDo
            // do thing for unowned spinner packs
            // make make an in-app purchase here? or go to the store?
            System.out.println("you don't have this pack!");
        }
        Bundle bundle = new Bundle();
        spinnerPosition = position;
        bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
        getLoaderManager().restartLoader(1, bundle, this);
        drawerFragmentListener.onSpinnerClicked();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /* Get the current pack by returning the pack id of the first magnet in the pack. */
    public int getCurrentPack() {
        Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(0);
        return cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID));
    }
    
    /* Get the current pack name by returning the name of the current item in the spinner */
    public String getCurrentPackName() {
        Cursor cursor = (Cursor) drawerSpinnerAdapter.getItem(spinnerPosition);
        return cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
    }

    /* Implements the loader callback methods. Has two loaders with ids 0 and 1. Loader 0 loads the packs from the database and puts them into the drawerSpinnerAdapter. Loader 1 gets the magnets for a particular pack from the database and loads them into the drawerMagnetsAdapter.*/
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==0) {
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE
            };
            return new CursorLoader(getActivity(),ApplicationContract.getPacks_URI,projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + MagnetDatabaseContract.MagnetEntry.DESC);
        }
        else {
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT
            };
          String[] whereArguments = {Integer.toString(args.getInt("id"))};
            return new CursorLoader(getActivity(),ApplicationContract.getMagnets_URI,projection, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID + " =? ", whereArguments , MagnetDatabaseContract.MagnetEntry._ID + MagnetDatabaseContract.MagnetEntry.ASC);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            if(loader.getId() == 0) {
                cursor.moveToFirst();
                Bundle bundle = new Bundle();
                bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
                getLoaderManager().initLoader(1,bundle,this);
                drawerSpinnerAdapter.swapCursor(cursor);
            }
            if(loader.getId() == 1) {
                drawerMagnetsAdapter.swapCursor(cursor);
            }
        }
    }

    // This is called when the last Cursor provided to onLoadFinished() above is about to be closed.  We need to make sure we are no longer using any of the cursors
    public void onLoaderReset(Loader<Cursor> loader) {
        drawerMagnetsAdapter.swapCursor(null);
        drawerSpinnerAdapter.swapCursor(null);
    }
}
