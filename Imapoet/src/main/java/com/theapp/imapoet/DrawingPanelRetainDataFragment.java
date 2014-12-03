package com.theapp.imapoet;


import android.os.Bundle;
import android.app.Fragment;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * A fragment for holding the magnet and saved poem data on orientation change
 */
public class DrawingPanelRetainDataFragment extends Fragment {
    private ArrayList<Magnet> magnets = new ArrayList<Magnet>();
    private boolean previouslySavedPoem = false;
    private String previouslySavedPoemID = null;
    private String previouslySavedPoemName = null;

    public static DrawingPanelRetainDataFragment newInstance() { return new DrawingPanelRetainDataFragment();}

    // Required empty public constructor
    public DrawingPanelRetainDataFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // don't destroy on orientation change
    }

    public void setMagnetData(ArrayList<Magnet> newMagnets, boolean previouslySavedPoem, String previouslySavedPoemID, String previouslySavedPoemName) {

       // for(Magnet newMagnet : newMagnets) {
        /*for(Iterator<Magnet> it = newMagnets.iterator(); it.hasNext();) {
            this.magnets.add(it.next());
        }*/
        this.magnets = newMagnets; //toDo deep copy here

        this.previouslySavedPoem = previouslySavedPoem;
        this.previouslySavedPoemID = previouslySavedPoemID;
        this.previouslySavedPoemName = previouslySavedPoemName;
    }

    public ArrayList<Magnet> getMagnets() {
        for(Magnet magnet : this.magnets) {
            System.out.println("getting magnet: "+magnet.word());
        }
        return magnets;
    }

    public boolean getPreviouslySavedPoem() {
        return previouslySavedPoem;
    }

    public String getPreviouslySavedPoemName() {
        return previouslySavedPoemName;
    }

    public String getPreviouslySavedPoemID() {
        return previouslySavedPoemID;
    }






}
