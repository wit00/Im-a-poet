package com.theapp.imapoet;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by whitney on 9/8/14.
 * SD_CardHelper is a helper class for saving to the sd card on android devices.
 */
public class SD_CardHelper {

    public SD_CardHelper(){}

    public static boolean sdCardIsMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getAlbumStorageDirectory(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("", "Directory not created");
        }
        return file;
    }

    // save a bitmap to a file
    public static boolean saveBitmap(File saveToFile, Bitmap bitmapToSave) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(saveToFile);
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
            return false;
        } catch (IOException IOException) {
            IOException.printStackTrace();
            return false;
        }
        return true;
    }

    // If the filename exists, adds a number in parenthesis to the filename, so that it can be saved without overriding the file. For example, if "untitled" was an existing file, it would be saved as "untitled (1)". If "untitled (1)" existed, the new file would be titled "untitled (2)", etc.
    public static File appendNumberToFilenameIfExists(String fileName, File directory) {
        File newFile = new File(directory,fileName);
        int nextFile = 0;
        while (newFile.exists()) {
            nextFile ++;
            newFile = new File(directory,fileName + " (" + Integer.toString(nextFile) + ")");
        }
        return newFile;
    }
}
