package com.theapp.imapoet;



import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;


/**
 * A simple {@link Fragment} subclass.
 *
 */
/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends android.support.v4.app.Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SettingsListViewAdapter settingsListViewAdapter;
    private ListView listView;
    private AsyncQueryHandler queryHandler;
    private boolean music = true;
    private boolean soundEffect = true;
    private MediaPlayer mediaPlayerForSoundEffect;
    private MediaPlayer mediaPlayerForMusic;
    private CountDownTimer mediaPlayerForMusicCountdownTimer;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {}



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mediaPlayerForSoundEffect = MediaPlayer.create(getActivity(),R.raw.finger_snapping);
        mediaPlayerForMusic = MediaPlayer.create(getActivity(),R.raw.when_the_wind_blows);
        mediaPlayerForMusicCountdownTimer = new CountDownTimer(30000, 30000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Nothing to do
                if(mediaPlayerForMusic.isPlaying()) {
                    //mediaPlayerForMusic.setVolume(millisUntilFinished/30000, millisUntilFinished/30000);
                }
            }

            @Override
            public void onFinish() {
                if (mediaPlayerForMusic.isPlaying()) {
                    mediaPlayerForMusic.stop();
                    //mediaPlayerForMusic.release();
                }
            }
        };
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mediaPlayerForMusic.isPlaying()) {
            mediaPlayerForMusic.stop();
            mediaPlayerForMusic.release();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu_settings, container, false);
        listView = (ListView) rootView.findViewById(R.id.settingsListView);
        getLoaderManager().initLoader(0,null,this);
        queryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
            @Override
            protected void onUpdateComplete(int soundOn, Object sound, int result) {
                if(soundOn == 1) { // a sound has been turned on
                    if(sound.equals(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) {
                        mediaPlayerForSoundEffect.start();
                    } else if(sound.equals(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) {
                        if(!mediaPlayerForMusic.isPlaying()) {
                            mediaPlayerForMusic = MediaPlayer.create(getActivity(),R.raw.when_the_wind_blows);
                            mediaPlayerForMusic.start();
                            //mediaPlayerForMusic
                            //mediaPlayerForMusicCountdownTimer.start();
                        }
                    }
                } else {
                    if(sound.equals(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) {
                        if(mediaPlayerForMusic.isPlaying()) {
                            mediaPlayerForMusic.stop();
                            //mediaPlayerForMusic.release();
                        }
                    }
                }

            }
            @Override
            protected  void onInsertComplete(int token, Object cookie, Uri uri) {
                settingsListViewAdapter = new SettingsListViewAdapter(getActivity(),true,true);
                restartLoader();
            }
        };
        return rootView;
    }

    private void insertInitialValues() {
        ContentValues initialSettingsValues = new ContentValues();
        initialSettingsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS,1);
        initialSettingsValues.put(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC,1);
        queryHandler.startInsert(0,null,Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/insert/settings"),initialSettingsValues);
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0,null,this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC,
                MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS
        };

        return new CursorLoader(getActivity(),Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings"),
                projection,null,null, MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC + MagnetDatabaseContract.MagnetEntry.ASC);
    }



    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() == 0) {
            insertInitialValues();
        } else {
            cursor.moveToFirst();
            music = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) > 0;
            soundEffect = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) > 0;
            if(settingsListViewAdapter != null) {
                settingsListViewAdapter.setSounds(music, soundEffect);
                settingsListViewAdapter.notifyDataSetChanged();
            } else {
                settingsListViewAdapter = new SettingsListViewAdapter(getActivity(),music,soundEffect);
            }
        }
        listView.setAdapter(settingsListViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        soundEffect = !soundEffect;
                        updateSound(soundEffect, MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS);
                        break;
                    case 3:
                        music = !music;
                        updateSound(music,MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC);

                        break;
                    case 5:
                        String packageName = getActivity().getApplicationContext().getPackageName();
                        Uri googleMarketApplicationUri = Uri.parse(ApplicationContract.googleMarketApplication + packageName);
                        Intent goToGoogleMarketApplication = new Intent(Intent.ACTION_VIEW, googleMarketApplicationUri);
                        try {
                            startActivity(goToGoogleMarketApplication);
                        } catch (ActivityNotFoundException googleMarketApplicationNotFound) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ApplicationContract.googleMarketWebAddress + packageName)));
                        }
                    case 7:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
                        startActivity(Intent.createChooser(intent, "Send Us an Email"));
                        break;
                    default:
                        break;
                }

            }
        });
    }

    // This is called when the last Cursor provided to onLoadFinished()
    // above is about to be closed.
    public void onLoaderReset(Loader<Cursor> loader) {
        listView.setAdapter(null);

    }

    private void updateSound(Boolean sound, String column) {
        ContentValues settingsValues = new ContentValues();
        settingsValues.put(column, (sound) ? 1 : 0);
        queryHandler.startUpdate((sound) ? 1 : 0, column, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/update/settings"), settingsValues, "", new String[]{});
    }




}
