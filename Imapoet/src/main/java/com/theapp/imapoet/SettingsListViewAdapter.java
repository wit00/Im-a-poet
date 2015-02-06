package com.theapp.imapoet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Custom adapter for the settings fragment in the main menu
 * Created by whitney on 6/30/14.
 */
public class SettingsListViewAdapter extends BaseAdapter {
    private Context context;
    private Boolean music;
    private Boolean soundEffects;

    public SettingsListViewAdapter(Context context, Boolean music, Boolean soundEffects) {
        this.context = context;
        this.music = music;
        this.soundEffects = soundEffects;
    }
    public int getCount() {
        return 12;
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
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("Sound effects on!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_sound_effects);
                } else {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("Sound effects off!");
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
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("Music on!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_music);
                } else {
                    ((TextView) (view.findViewById(R.id.settingsRow))).setText("Music off!");
                    ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.settings_no_music);
                }
                return view;
            case 4:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Review this App!");
                return view;
            case 5:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                ((TextView) (view.findViewById(R.id.settingsRow))).setText("Click here to review this app on google play.");
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
            case 8:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Restore Purchases");
                return view;
            case 9:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                ((TextView) (view.findViewById(R.id.settingsRow))).setText("If something is wrong with your in-app purchases, you can restore them here.");
                ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.restore_in_app);
                return view;
            case 10:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_header, parent, false);
                ((TextView) (view.findViewById(R.id.headerTitle))).setText("Reload Packs and Words");
                return view;
            case 11:
                view = LayoutInflater.from(context).inflate(R.layout.fragment_settings_list_row, parent, false);
                ((TextView) (view.findViewById(R.id.settingsRow))).setText("If something is wrong with your packs and/or words, you can restore them here. If you have made in app purchases, you should return to this page and press the 'restore purchases' button above.");
                ((ImageView) (view.findViewById(R.id.settingsRowImage))).setImageResource(R.drawable.restore);
                return view;
            default:
                return view;
        }
    }

}
