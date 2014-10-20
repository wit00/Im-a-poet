package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * Drawer Spinner Adapter is the adapter class for the spinner in the drawer fragment.
 * The closed view of the spinner uses the standard android simple spinner item layout, and
 * the drop down view is my own fragment_drawer_spinner_layout xml.
 * The drop down view turns grey and includes a message if it is a non-downloaded pack.
 * Created by whitney on 7/18/14.
 */
public class DrawerSpinnerAdapter extends SimpleCursorAdapter {
    private LayoutInflater inflater;
    private int layout;

    public DrawerSpinnerAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,layout,cursor,from,to,0);
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        return (inflater).inflate(android.R.layout.simple_spinner_item,null);
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent){
        return (inflater).inflate(layout,null);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        if(view.findViewById(R.id.spinnerOwned) != null) {
            TextView spinnerText = (TextView) view.findViewById(R.id.spinnerTitle);
            TextView ownedText = (TextView) view.findViewById(R.id.spinnerOwned);
            String packFileName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
            String cleanedUpFileName = packFileName.substring(0,packFileName.indexOf("."));
            cleanedUpFileName = cleanedUpFileName.replace("_"," ");
            spinnerText.setText(cleanedUpFileName);
            if(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_IS_AVAILABLE)) == 0) {
                spinnerText.setTextColor(Color.LTGRAY);
                ownedText.setTextColor(Color.LTGRAY);
                ownedText.setVisibility(View.VISIBLE);
            } else {
                spinnerText.setTextColor(Color.BLACK);
                ownedText.setVisibility(View.GONE);
            }
        } else {
            TextView spinnerText = (TextView) view.findViewById(android.R.id.text1);
            String packFileName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_PACK_NAME));
            String cleanedUpFileName = packFileName.substring(0,packFileName.indexOf("."));
            cleanedUpFileName = cleanedUpFileName.replace("_"," ");
            spinnerText.setText(cleanedUpFileName);
            spinnerText.setTextColor(Color.BLACK);
        }
    }
}
