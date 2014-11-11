package com.theapp.imapoet;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


public class MediaService extends Service implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnErrorListener, Loader.OnLoadCompleteListener<Cursor> {
    private MediaPlayer backgroundMusicMediaPlayer = null;
    private MediaPlayer soundEffectMediaPlayer = null;
    private Boolean playSoundEffects = true;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public MediaService() {}

    //@Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // The MediaPlayer has moved to the Error state, must be reset!
        backgroundMusicMediaPlayer.reset();
        return false;
    }
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                playBackgroundMusic();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (backgroundMusicMediaPlayer.isPlaying()) backgroundMusicMediaPlayer.stop();
                backgroundMusicMediaPlayer.release();
                backgroundMusicMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                pauseBackgroundMusic();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (backgroundMusicMediaPlayer.isPlaying()) backgroundMusicMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (backgroundMusicMediaPlayer != null) backgroundMusicMediaPlayer.release();
        backgroundMusicMediaPlayer = null;
        if (cursorLoader != null) {
            cursorLoader.unregisterListener(this);
            cursorLoader.cancelLoad();
            cursorLoader.stopLoading();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MediaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaService.this;
        }
    }

    private CursorLoader cursorLoader;
    @Override
    public IBinder onBind(Intent intent) {
        String [] projection = {
                MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC,
                MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS
        };
        cursorLoader = new CursorLoader(this, Uri.parse("content://com.theapp.imapoet.provider.magnetcontentprovider/settings"), projection, null, null,null);
        cursorLoader.registerListener(0, this);
        cursorLoader.startLoading();
        return binder;
    }


    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            setBackgroundMusic(cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_MUSIC)) != 0);
            boolean databaseSoundEffectValue = cursor.getInt(cursor.getColumnIndex(MagnetDatabaseContract.MagnetEntry.COLUMN_SOUND_EFFECTS)) != 0;
            if(!playSoundEffects.equals(databaseSoundEffectValue)) { // don't play sound effect on first load
                playSoundEffects = databaseSoundEffectValue;
                if(playSoundEffects) playSoundEffect();
            }
        } else {
            setBackgroundMusic(true);
        }
    }


    public void pauseBackgroundMusic() {
        if(backgroundMusicMediaPlayer != null) {
            if (backgroundMusicMediaPlayer.isPlaying()) {
                backgroundMusicMediaPlayer.pause();
            }
        }
    }

    private void playBackgroundMusic() {
        if(backgroundMusicMediaPlayer == null) {
            initializeAndStartMediaPlayer();
        } else if (!backgroundMusicMediaPlayer.isPlaying()) {
            startLoopingMediaPlayer();
        }
    }

    private void startLoopingMediaPlayer() {
        backgroundMusicMediaPlayer.start();
        backgroundMusicMediaPlayer.setVolume(1.0f, 1.0f);
        backgroundMusicMediaPlayer.setLooping(true);
    }


    private void initializeAndStartMediaPlayer() {
        backgroundMusicMediaPlayer = MediaPlayer.create(this,R.raw.when_the_wind_blows);
        backgroundMusicMediaPlayer.setOnErrorListener(this);
        startLoopingMediaPlayer();
    }

    public void setBackgroundMusic(boolean playBackgroundMusic) {
        if(playBackgroundMusic) {
            playBackgroundMusic();
        }
        else {
            pauseBackgroundMusic();
        }
    }

    public void playSoundEffect() {
        if(soundEffectMediaPlayer != null) {
            soundEffectMediaPlayer.start();
        } else {
            soundEffectMediaPlayer = MediaPlayer.create(this,R.raw.finger_snapping);
            soundEffectMediaPlayer.start();
        }
    }
}
