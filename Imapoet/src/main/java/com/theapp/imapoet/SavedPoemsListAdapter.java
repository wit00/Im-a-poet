package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by whitney on 7/29/14.
 */
public class SavedPoemsListAdapter extends SimpleCursorAdapter {
    private Context context;
    private int layout;
    private LayoutInflater inflater;
    private ArrayList<Integer> ids = new ArrayList<Integer>();
    private ArrayList<String> titles = new ArrayList<String>();


    public SavedPoemsListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,layout,cursor,from,to,0);
        this.context = context;
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);
    }

    public int getIDFromPosition(int position) {
        return ids.get(position);
    }

    public String getNameFromPosition(int position) {
        return titles.get(position);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        //return inflater.inflate(layout, null);
        return inflater.inflate(layout,null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        String title = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_TITLE));
        ((TextView) view.findViewById(R.id.saved_poem_title)).setText(title);
        //((TextView) view.findViewById(R.id.saved_poem_title)).setText("text");

        ((TextView) view.findViewById(R.id.saved_poem_date_value)).setText(cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_DATE_SAVED)));
        (view.findViewById(R.id.delete_poem_button)).setTag(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
        ids.add(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry._ID)));
        titles.add(title);

    }
}
