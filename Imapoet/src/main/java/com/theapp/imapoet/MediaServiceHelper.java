package com.theapp.imapoet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * A helper class to make using the MediaService (streaming background music) service more DRY. Instantiated and used in MainActivity and MainMenu, this class binds the class' contexts to the MediaService. The using class must call bindtoMediaMusicService and unbindFromMediaMusicService, preferably on they're onStart and onStop methods.
 * Created by whitney on 11/6/14.
 */
public class MediaServiceHelper {
    private Context context;
    private MediaService mediaService = null;
    private boolean boundToMediaService = false;

    public MediaServiceHelper(Context context) {
        this.context = context;
    }

    protected MediaService getMediaService() { return mediaService; }

    protected void bindToMediaMusicService() {
        Intent intent = new Intent(context, MediaService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void unbindFromMediaMusicService() {
        if (boundToMediaService) {
            context.unbindService(mConnection);
            boundToMediaService = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaService.LocalBinder binder = (MediaService.LocalBinder) service;
            mediaService = binder.getService();
            boundToMediaService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToMediaService = false;
            mediaService = null;

        }
    };
}
