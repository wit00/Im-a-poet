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
 * Created by whitney on 11/13/14.
 */
public class StatisticsFragmentListAdapter extends SimpleCursorAdapter {
    private int layout;
    private LayoutInflater inflater;



    public StatisticsFragmentListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,layout,cursor,from,to,0);
        this.layout = layout;
        this.inflater = LayoutInflater.from(context);
    }



    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        //return inflater.inflate(layout, null);
        return inflater.inflate(layout,null);
    }

    private boolean isTheSameStatistic(String cursorStatisticName, String statisticName) {
        return cursorStatisticName.equals((statisticName.toString()));
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        String statisticName = cursor.getString(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CODE));
        String statisticValue = Integer.toString(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_CURRENT_VALUE)));
        TextView statisticsLabelTextView = ((TextView) view.findViewById(R.id.statistics_label));
        TextView statisticsValueTextView = ((TextView) view.findViewById(R.id.statistics_value));
        if (isTheSameStatistic(statisticName,"SAVED_POEMS")) {
            statisticsLabelTextView.setText(R.string.statistics_total_saves);
            statisticsValueTextView.setText(statisticValue);
        } else if (isTheSameStatistic(statisticName, "SAVED_POEMS_NUMBER_MAGNETS")) {
            statisticsLabelTextView.setText(R.string.statistics_most_words_saved_poem);
            statisticsValueTextView.setText(statisticValue);
        } else if (isTheSameStatistic(statisticName, "UPDATED_POEM")) {
            statisticsLabelTextView.setText(R.string.statistics_total_updates);
            statisticsValueTextView.setText(statisticValue);
        } else if(isTheSameStatistic(statisticName, "MAGNET_ENTERS_CANVAS")) {
            statisticsLabelTextView.setText(R.string.statistics_most_words_used_at_one_time);
            statisticsValueTextView.setText(statisticValue);
        } else if(isTheSameStatistic(statisticName, "PACK_USED")) {
            statisticsLabelTextView.setText(R.string.statistics_most_packs_used_at_one_time);
            statisticsValueTextView.setText(statisticValue);
        } else if(isTheSameStatistic(statisticName, "MAGNET_DELETED")) {
            statisticsLabelTextView.setText(R.string.statistics_magnets_deleted);
            statisticsValueTextView.setText(statisticValue);
        } else if(isTheSameStatistic(statisticName, "SHARED_POEM")) {
            statisticsLabelTextView.setText(R.string.statistics_shared_poems);
            statisticsValueTextView.setText(statisticValue);
        }
    }
}
