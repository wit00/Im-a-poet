package com.theapp.imapoet;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

/**
 * Created by whitney on 9/22/14.
 */
public class MagnetDrawerListener implements DrawerLayout.DrawerListener {
    MainActivity test;


    public MagnetDrawerListener(MainActivity mainActivity) {
        test = mainActivity;
    }

    public void onDrawerClosed (View drawerView) {
        //
    }

    public void onDrawerOpened (View drawerView) {
        //System.out.println("oink drawer open");
        test.drawerOpened();
    }



    public void onDrawerSlide (View drawerView, float slideOffset) {
        //
    }

    public void onDrawerStateChanged (int newState) {
        //
    }
}
