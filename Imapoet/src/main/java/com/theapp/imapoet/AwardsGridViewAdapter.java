package com.theapp.imapoet;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by whitney on 6/30/14.
 */
public class AwardsGridViewAdapter extends SimpleCursorAdapter {
    private Context context;
    private int layout;
    private LayoutInflater inflater;

   public AwardsGridViewAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,R.layout.fragment_awards_grid_item,cursor,from,to,0);
        this.context = context;
        this.layout = R.layout.fragment_awards_grid_item;
        this.inflater = LayoutInflater.from(context);
    }



    //public int getCount() {
    //    return 10;
   // }
    //public Object getItem(int position) {
     //   return 0;
    //}
   // public long getItemId(int position) {
    //    return 0;
   // }

    public boolean gotAward(int position) {
        if(position == 3 || position == 4 || position == 7) {
            return true;
        } else {
           return  false;
        }
    }
    /*public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(context);
            //imageView.setLayoutParams(new GridView.LayoutParams(85,85));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8,8,8,8);
        } else {
            imageView = (ImageView) convertView;
        }
        if(position == 3 || position == 4 || position == 7) {
            imageView.setImageResource(R.drawable.blue_ribbon);
        } else {
            imageView.setImageResource(R.drawable.ribbon_outline);
        }
        return  imageView;
    }*/

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
        //return inflater.inflate(layout, null);
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

    /*@Override
    public void bindView(View view, Context context, Cursor cursor) {
        String pos = cursor.getString(positionColumnIndex);
        String path = cursor.getString(pathColumnIndex);
        ImageView image = (ImageView)view;
        image.setImageDrawable(Drawable.createFromPath(path));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = new ImageView(context);
        bindView(v, context, cursor);
        return v;
    }*/
}

