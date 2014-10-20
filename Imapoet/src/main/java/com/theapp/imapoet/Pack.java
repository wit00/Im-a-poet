package com.theapp.imapoet;

import java.util.ArrayList;

/**
 * The pack objects are used in the drawer fragment spinner
 * Created by whitney on 7/24/14.
 */
public class Pack {
    String title;
    int id;
    ArrayList<String> magnets;
    private boolean isAvailable;

    public int isAvailable() {
        if(isAvailable) return 1;
        else return 0;
    }

    Pack(String title, ArrayList<String> words, boolean isAvailable) {
        this.title = title;
        magnets = words;
        this.isAvailable = isAvailable;
    }
}
