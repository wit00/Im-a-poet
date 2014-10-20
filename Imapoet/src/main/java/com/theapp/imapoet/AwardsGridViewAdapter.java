package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Adapter for the gridView that holds the award images in the AwardsFragment
 * Created by whitney on 6/30/14.
 */
public class AwardsGridViewAdapter extends SimpleCursorAdapter {
    private int layout;
    private LayoutInflater inflater;

   public AwardsGridViewAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,R.layout.fragment_awards_grid_item,cursor,from,to,0);
        this.layout = R.layout.fragment_awards_grid_item;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
       return inflater.inflate(layout,null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED)) == 0) {
            String imageName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_UNCOMPLETED_IMAGE_ID));
            ((ImageView) view.findViewById(R.id.gridImage)).setImageResource(view.getResources().getIdentifier("com.theapp.imapoet:drawable/"+imageName,null,null));

        } else {
            String imageName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_COMPLETED_IMAGE_ID));
            ((ImageView) view.findViewById(R.id.gridImage)).setImageResource(view.getResources().getIdentifier("com.theapp.imapoet:drawable/"+imageName,null,null));

        }
        ((TextView) view.findViewById(R.id.gridText)).setText(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_AWARD_NAME)));
        ((TextView) view.findViewById(R.id.description)).setText(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_DESCRIPTION)));
    }

}

