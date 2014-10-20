package com.theapp.imapoet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;


public class DrawerFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener,  LoaderManager.LoaderCallbacks<Cursor>  {
    private OnFragmentInteractionListener drawerFragmentListener;
    public DrawerLayout drawerLayout;
    private GridView gridView;
    private DrawerMagnetsAdapter drawerMagnetsAdapter;
    private DrawerSpinnerAdapter spinnerAdapter;

    //private SimpleCursorAdapter drawerSpinnerAdapter;
    private ArrayList<String> packs = new ArrayList<String>();
    private int spinnerPosition = 0;
    private Spinner spinner;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0,null,this);
    }


    public void toggleDrawer() {
        //drawerLayout.openDrawer(8);
        if(drawerLayout != null) {
            drawerLayout.openDrawer(Gravity.LEFT);
        } else {
            drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.pager);
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    public boolean isOpen() {
        if(drawerLayout != null) {
            return drawerLayout.isDrawerOpen(Gravity.LEFT);
        } else {
            drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.pager);
            return drawerLayout.isDrawerOpen(Gravity.LEFT);
        }
    }
    public void openSpinner() {
        if(spinner != null) {
            spinner.performClick();
        } else {
            spinner = (Spinner) getView().findViewById(R.id.sets_spinner);
        }
    }

    private void setupMagnetDeckSpinner(View currentView) {
        spinner = (Spinner) currentView.findViewById(R.id.sets_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
       // spinnerAdapter = new DrawerSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_item, null, new String[]{MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE}, new int[]{android.R.id.text1 });
        spinnerAdapter = new DrawerSpinnerAdapter(getActivity(), R.layout.fragment_drawer_spinner_layout, null, new String[]{}, new int[]{});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }
    
    private void setupGridView(View currentView) {
        gridView = (GridView) currentView.findViewById(R.id.gridview);
        drawerMagnetsAdapter = new DrawerMagnetsAdapter(getActivity(), null, new String[]{}, new int[]{});
        //drawerSpinnerAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_drawer_gridview_row, null, new String[]{MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT}, new int[]{android.R.id.text1},0);
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
        Cursor cursor = (Cursor) spinnerAdapter.getItem(position);
        if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE))==0) {
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


    // TODO:will this break?
    public int getCurrentPack() {
        Cursor cursor = (Cursor) drawerMagnetsAdapter.getItem(0);
        return cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID));
    }

    public String getCurrentPackName() {
        Cursor cursor = (Cursor) spinnerAdapter.getItem(spinnerPosition);
        return cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==0) {
            Uri baseUri = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/packs");
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE
            };
            return new CursorLoader(getActivity(),baseUri,projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE + MagnetDatabaseContract.MagnetEntry.DESC);
        }
        else {
            Uri baseUri = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/magnets");
            String [] projection = {
                    MagnetDatabaseContract.MagnetEntry._ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID,
                    MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT
            };
          String[] whereArguments = {Integer.toString(args.getInt("id"))};
            return new CursorLoader(getActivity(),baseUri,projection, MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_ID + " =? ", whereArguments , MagnetDatabaseContract.MagnetEntry._ID + MagnetDatabaseContract.MagnetEntry.ASC);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            if(loader.getId() == 0) {
                cursor.moveToFirst();
                Bundle bundle = new Bundle();
                bundle.putInt("id",cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
                getLoaderManager().initLoader(1,bundle,this);
                spinnerAdapter.swapCursor(cursor);

            }
        if(loader.getId() == 1) {
            drawerMagnetsAdapter.swapCursor(cursor);
            /*gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                    v.startDrag(null, new View.DragShadowBuilder(v), null, 0);
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
               });*/

            }
        }

    }


    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.  We need to make sure we are no
    // longer using it.
    public void onLoaderReset(Loader<Cursor> loader) {
        drawerMagnetsAdapter.swapCursor(null);
        spinnerAdapter.swapCursor(null);
    }
}
