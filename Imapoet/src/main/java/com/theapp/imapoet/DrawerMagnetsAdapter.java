package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by whitney on 10/9/14.
 */
public class DrawerMagnetsAdapter extends SimpleCursorAdapter{
    private LayoutInflater inflater;

    public DrawerMagnetsAdapter(Context context, Cursor cursor, String[] from, int[] to) {
        super(context,R.layout.fragment_awards_grid_item,cursor,from,to,0);
        //this.context = context;
        //this.layout = R.layout.fragment_awards_grid_item;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        //return inflater.inflate(layout, null);
        return (inflater).inflate(R.layout.fragment_drawer_gridview_row,null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView magnet = (TextView) view.findViewById(R.id.tile_text);
        magnet.setText(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_WORD_TEXT)));


    }
}
