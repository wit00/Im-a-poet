package com.theapp.imapoet;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

/**
 * Listener for the drawer in the main activity. Lets the main activity know that the drawer has been opened (so the information can be passed on to the demo if it is running.
 * Created by whitney on 9/22/14.
 */
public class MagnetDrawerListener implements DrawerLayout.DrawerListener {
    MainActivity mainActivity;


    public MagnetDrawerListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void onDrawerClosed (View drawerView) {
        //
    }

    public void onDrawerOpened (View drawerView) {
        mainActivity.drawerOpened();
    }



    public void onDrawerSlide (View drawerView, float slideOffset) {
        //
    }

    public void onDrawerStateChanged (int newState) {
        //
    }
}
