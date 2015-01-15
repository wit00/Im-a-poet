package com.theapp.imapoet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by whitney on 6/12/14.
 */
public class DrawerGridViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> magnets;
    private ArrayList<TextView> magnet_text_views = new ArrayList<TextView>();

    public DrawerGridViewAdapter(Context context, ArrayList<String> magnets) {
        this.context = context;
        this.magnets = magnets;
       /* LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view= inflater.inflate(R.layout.fragment_drawer_gridview_row, false);
        for(String magnet : magnets) {

            TextView new_magnet = (TextView) view.findViewByID(R.id.tile_text);
            new_magnet.setText(magnet);
            magnet_text_views.add(new_magnet);
        }*/
    }

    public void setNewMagnets(ArrayList<String> magnets) {
        this.magnets = magnets;
    }

    public int getCount() {
        return magnets.size();
    }

    public String getMagnetTextAtPosition(int position) {
        return magnets.get(position);
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view= inflater.inflate(R.layout.fragment_drawer_gridview_row,parent, false);
        }
        TextView magnet = (TextView) view.findViewById(R.id.tile_text);
        magnet.setText(magnets.get(position));
        return view;
    }


}