package com.theapp.imapoet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Dialog box for when a new poem is about to be saved
 * Created by whitney on 8/1/14.
 */
public class NewPoemSaveAlertDialog extends DialogFragment {
    private NewPoemSaveAlertDialogListener newPoemSaveAlertDialogListener;

    public interface NewPoemSaveAlertDialogListener {
        public void newPoemInsertIntoDatabase(String titleInputValue, boolean newPoem);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            newPoemSaveAlertDialogListener = (NewPoemSaveAlertDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        newPoemSaveAlertDialogListener = null;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText titleInput = new EditText(getActivity());
        titleInput.setHint("Give it a title.");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Your Poem")
                .setMessage("Do you want to save this poem?")
                .setView(titleInput)
                .setPositiveButton("okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String titleInputValue = (titleInput.getText()).toString();
                        if (titleInputValue.trim().length() == 0) {
                            titleInputValue = "untitled";
                        } // if empty or made of spaces, set title to untitled
                        //insertPoemIntoDatabase(titleInputValue, drawingPanelFragment.getPoem(),false);
                        newPoemSaveAlertDialogListener.newPoemInsertIntoDatabase(titleInputValue,false);
                    }
                })
                .setNegativeButton("no thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        return builder.create();

    }
}
