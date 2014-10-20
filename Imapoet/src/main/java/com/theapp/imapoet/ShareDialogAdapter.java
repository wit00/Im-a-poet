package com.theapp.imapoet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by whitney on 7/10/14.
 */
public class ShareDialogAdapter extends BaseAdapter {
    private Context context;

    public ShareDialogAdapter(Context context) {
        this.context = context;
    }

    public int getCount() {
        return 4;
    }

    public Object getItem(int position) {
        return 0;
    }

    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
        assert view != null;
        switch (position) {
            case 0:
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Email");
                return view;
            case 1:
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Message");
                return view;
            case 2:
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Twitter");
                return view;
            case 3:
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Google+");

                return view;
            default:
                return view;

        }
    }
}