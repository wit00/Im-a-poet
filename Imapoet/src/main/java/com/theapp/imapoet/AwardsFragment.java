package com.theapp.imapoet;



import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public  class AwardsFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private AwardsGridViewAdapter awardsGridViewAdapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static AwardsFragment newInstance() {
            return new AwardsFragment();
        }

        public AwardsFragment() {}


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu_awards, container, false);
            GridView gridView = (GridView) rootView.findViewById(R.id.awardGrid);
            awardsGridViewAdapter = new AwardsGridViewAdapter(getActivity(), R.layout.fragment_main_menu_awards, null, new String[]{}, new int[]{});
            gridView.setAdapter(awardsGridViewAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(((TextView) view.findViewById(R.id.description)).getText())
                            .setTitle("About this award");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            return rootView;
        }



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getLoaderManager().initLoader(0,null,this);
        }

        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Uri baseUri = Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/awards/detail");
            return new CursorLoader(getActivity(),baseUri,null,null,null,null);
        }


        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            awardsGridViewAdapter.swapCursor(cursor);
        }

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        public void onLoaderReset(Loader<Cursor> loader) {
            awardsGridViewAdapter.swapCursor(null);
        }
    }
