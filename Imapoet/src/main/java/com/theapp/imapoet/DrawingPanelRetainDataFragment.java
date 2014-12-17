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
    private float scaleFactor = 1.0f;
    private float scalePivotX = 0.0f;
    private float scalePivotY = 0.0f;
    private float scrollXOffset = 0.0f;
    private float scrollYOffset = 0.0f;

    public static DrawingPanelRetainDataFragment newInstance() { return new DrawingPanelRetainDataFragment();}

    // Required empty public constructor
    public DrawingPanelRetainDataFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // don't destroy on orientation change
    }

    public void setMagnetData(ArrayList<Magnet> newMagnets, boolean previouslySavedPoem, String previouslySavedPoemID, String previouslySavedPoemName, float scaleFactor, float scalePivotX, float scalePivotY, float scrollXOffset, float scrollYOffset) {

       // for(Magnet newMagnet : newMagnets) {
        /*for(Iterator<Magnet> it = newMagnets.iterator(); it.hasNext();) {
            this.magnets.add(it.next());
        }*/
        this.magnets = newMagnets; //toDo deep copy here

        this.previouslySavedPoem = previouslySavedPoem;
        this.previouslySavedPoemID = previouslySavedPoemID;
        this.previouslySavedPoemName = previouslySavedPoemName;
        this.scaleFactor = scaleFactor;
        this.scalePivotX = scalePivotX;
        this.scalePivotY = scalePivotY;
        this.scrollXOffset = scrollXOffset;
        this.scrollYOffset = scrollYOffset;
    }

    public ArrayList<Magnet> getMagnets() { return magnets; }

    public boolean getPreviouslySavedPoem() {
        return previouslySavedPoem;
    }

    public String getPreviouslySavedPoemName() {
        return previouslySavedPoemName;
    }

    public String getPreviouslySavedPoemID() { return previouslySavedPoemID;}

    public float getScaleFactor() { return scaleFactor; }
    public float getScalePivotX () { return scalePivotX; }
    public float getScalePivotY () { return scalePivotY; }

    public float getScrollXOffset() { return scrollXOffset; }
    public float getScrollYOffset() { return scrollYOffset; }






}
