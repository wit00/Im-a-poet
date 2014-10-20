package com.theapp.imapoet;

import java.util.ArrayList;

/**
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


    Pack(String title, int id) {
        this.title = title;
        this.id = id;
        //this.magnets = magnets;
    }
   /* Pack(ArrayList<String> words) {
        this.title = words.get(0);
        words.remove(0);
        magnets = words;
    }*/

    Pack(String title, ArrayList<String> words, boolean isAvailable) {
        this.title = title;
        magnets = words;
        this.isAvailable = isAvailable;
    }
}
