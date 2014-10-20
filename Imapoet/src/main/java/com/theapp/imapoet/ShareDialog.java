package com.theapp.imapoet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by whitney on 8/1/14.
 */
public class ShareDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_save_to_sd,null);
        ((TextView) view.findViewById(R.id.message)).setText("");
        final ShareDialogAdapter shareDialogAdapter = new ShareDialogAdapter(getActivity());
        //((ImageView) view.findViewById(R.id.screenshot)).setImageBitmap(screenshot);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because it's going in the dialog layout
        builder.setTitle("Share your poem")
                .setView(view)
                .setAdapter(shareDialogAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        switch (id) {
                            case 0: //Email
                                Bitmap screenshot = ScreenshotCreator.createScreenshotWithWatermark(getActivity().getFragmentManager().findFragmentById(R.id.the_canvas).getView(), getActivity(), R.drawable.watermark);

                                File file;
                                file = new File(getActivity().getCacheDir(), "tempPoemBitmap3.jpg");


                                try {
                                    file.createNewFile();
                                    file.setReadable(true, false);
                                    //file = file.createNewFile();
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                } catch (FileNotFoundException fileNotFoundException) {
                                    fileNotFoundException.printStackTrace();
                                } catch (java.io.IOException IOException) {
                                    IOException.printStackTrace();
                                }
                                //Uri.fromFile(file);
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.setType("image/*");
                                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "I made a poem with I'm a poet!");
                                sendIntent.putExtra("sms_body", "I made a poem with I'm a poet!");
                                //startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.hello_world)));
                                startActivity(sendIntent);
                                //getCacheDir().delete();

                                //file.delete();
                                break;
                        }
                    }
                });
        return builder.create();
    }
}


