package com.theapp.imapoet;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.Toast;


/**
 * Created by whitney on 9/19/14.
 */
public class Demo {
    private DemoListener demoListener;
    private boolean demoDrawerCompleted = false;
    private boolean demoPacksCompleted = false;
    private boolean demoWordCompleted = false;
    private boolean demoAwardsCompleted = false;
    private boolean demoDeleteCompleted = false;
    private boolean demoFinished = false;

    public interface DemoListener {
        public void highlightDrawer();
        public void unHighlightDrawer();
        public void highlightPacks();
        public void unHighlightPacks();
        public void highlightTrashCan();
        public void unHighlightTrashCan();
        public void showAward();
        public void addDemoFragment();
        public void demoComplete();
        public void changeTextView(String text);
    }


    public Demo(DemoListener demoListener, Context context){
        this.demoListener = demoListener;
    }


    public void runDemoIntro() {
        //runIntroDemoDialog();
        demoListener.addDemoFragment();
        demoListener.highlightDrawer();
        //if(demoListener.isDrawerOpen()) runOpenSpinnerDialog();
        demoDrawerCompleted = true;
    }

    public void drawerOpened() {
        //runOpenSpinnerDialog();
        if(demoDrawerCompleted) runDemoPacks();
        demoListener.unHighlightDrawer();
        demoListener.highlightPacks();
        if(demoFinished) demoListener.demoComplete();
    }

    private void runDemoPacks() {
        demoListener.changeTextView("Excellent! Your words are separated into packs. To use a new pack, select it from the drop down list above. Select a new pack to continue.");
        //runMoveTileDialog();
        demoPacksCompleted = true;
    }

    public void packsSelected() {
        if(demoPacksCompleted) runDemoWord();
        demoListener.unHighlightPacks();
    }

    private void runDemoWord() {
        demoListener.changeTextView("You can use a word by dragging it to the right. Drag a word to the right to continue. ");
        demoWordCompleted = true;
    }

    public void magnetTilesChanged() {
        if(demoWordCompleted) runDemoAward();
        if(demoFinished) demoListener.demoComplete();
    }

    private void runDemoAward() {
        demoListener.changeTextView("You can win awards by completing certain tasks. When you win an award, the award icon will light up. Click on the award icon to continue.");
        demoListener.showAward();
        demoAwardsCompleted = true;
    }

    public void awardClicked() {
        if(demoAwardsCompleted) runDemoDelete();
    }

    private void runDemoDelete() {
        demoListener.changeTextView("You can delete magnets by dragging them into the trash can. Delete a magnet to continue.");
        demoListener.highlightTrashCan();
        demoDeleteCompleted = true;
    }


    public void magnetDeleted() {
        //if(demoDeleteCompleted) demoListener.changeTextView("That's it! Now you're ready to use I'm a poet!");
        runDemoOtherButtons();
        demoListener.unHighlightTrashCan();
    }

    private void runDemoOtherButtons() {
        if(demoDeleteCompleted) demoListener.changeTextView("There are these other buttons");
        //demoListener.highlightMenuButtons();
        demoFinished = true;

    }
}


