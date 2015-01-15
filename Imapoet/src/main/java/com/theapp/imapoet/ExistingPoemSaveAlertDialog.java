package com.theapp.imapoet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import java.util.ArrayList;

/**
 * Dialog box for when an existing poem is about to be saved
 * Created by whitney on 8/1/14.
 */
public class ExistingPoemSaveAlertDialog extends DialogFragment {
    ExistingPoemDialogListener existingPoemDialogListener;
    public interface ExistingPoemDialogListener {
        public void existingPoemInsertIntoDatabase(String titleInputValue, boolean newPoem);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            existingPoemDialogListener = (ExistingPoemDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        existingPoemDialogListener = null;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String loadedPoemName = getArguments().getString("loadedPoemName");
        final ArrayList<Integer> clickedRadioButton = new ArrayList<Integer>(1);
        clickedRadioButton.add(0);
        final EditText titleInput = new EditText(getActivity());
        titleInput.setEnabled(false);
        titleInput.setHint(loadedPoemName);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Your Poem")
                .setView(titleInput)
                .setSingleChoiceItems(new CharSequence[]{"Save to existing saved poem", "Save as a new poem"}, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clickedRadioButton.clear();
                                clickedRadioButton.add(which);
                                if (which == 1) {
                                    titleInput.setEnabled(true);
                                    titleInput.setHint("new poem title");

                                } else {
                                    titleInput.setEnabled(false);
                                    titleInput.setHint(loadedPoemName);

                                }
                            }
                        }
                )

                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String titleInputValue = (titleInput.getText()).toString();
                        if (titleInputValue.trim().length() == 0) {
                            titleInputValue = "untitled";
                        } // if empty or made of spaces, set title to untitled
                        if (clickedRadioButton.get(0) == 0) {
                            //insertPoemIntoDatabase(titleInputValue, drawingPanelFragment.getPoem(), true);
                            existingPoemDialogListener.existingPoemInsertIntoDatabase(titleInputValue,true);

                        } else {
                            existingPoemDialogListener.existingPoemInsertIntoDatabase(titleInputValue,false);

                            // insertPoemIntoDatabase(titleInputValue, drawingPanelFragment.getPoem(), false);
                        }
                    }
                })
                .setNegativeButton("no thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        return builder.create();
    }


    //builder.create();
    //builder.show();
}
