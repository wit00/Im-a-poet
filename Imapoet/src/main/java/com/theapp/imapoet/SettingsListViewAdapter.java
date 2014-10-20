package com.theapp.imapoet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;

/**
 * Created by whitney on 6/30/14.
 */
public class SettingsListViewAdapter extends BaseAdapter {
    private Context context;
    private Boolean music;
    private Boolean soundEffects;

    /*public SettingsListViewAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context,R.layout.fragment_settings_list_header,cursor,from,to,0);
        this.context = context;
        this.layout = R.layout.fragment_awards_grid_item;
        this.inflater = LayoutInflater.from(context);
    }*/



    public SettingsListViewAdapter(Context context, Boolean music, Boolean soundEffects) {
        this.context = context;
        this.music = music;
        this.soundEffects = soundEffects;
    }
    public int getCount() {
        return 8;
    }
    public void setSounds (Boolean music, Boolean soundEffects) {
        this.music = music;
        this.soundEffects = soundEffects;
    }


    public Object getItem(int position) {
        return 0;
    }
    public long getItemId(int position) {
        return 0;
    }


    public View getView(int position, View view, ViewGroup parent) {
        switch (position) {
            case 0:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Sound Effects");
                return view;
            case 1:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                if(soundEffects) {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("sound effects on!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_sound_effects);
                } else {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("sound effects off!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_no_sound_effects);
                }
                return view;
            case 2:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Music");
                return view;
            case 3:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                if(music) {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("music on!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_music);
                } else {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("music off!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_no_music);
                }
                return view;
            case 4:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Review this App!");
                return view;
            case 5:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                ((TextView) (view.findViewById(R.id.settingsRow))).setText("Review this app on google play.");
                ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.rate_us);

                return view;
            case 6:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Contact Us");
                return view;
            case 7:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                ((TextView) (view.findViewById(R.id.settingsRow))).setText("Send us an email!");
                ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.mail_us);
                return view;
            default:
                return view;

        }
       /* if(position == 0 || position == 2) {
            view =  LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
            assert view != null;
            ((TextView) (view.findViewById(R.id.headerTitle))).setText("header " + Integer.toString(position));
            return  view;
        }
        view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
        ((TextView) (view.findViewById(R.id.settingsRow))).setText("settings");
        return view;*/
        //}
    }

}
