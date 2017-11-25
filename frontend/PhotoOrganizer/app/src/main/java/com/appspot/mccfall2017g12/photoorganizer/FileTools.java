package com.appspot.mccfall2017g12.photoorganizer;

import android.os.Environment;

import java.io.File;

public class FileTools {

    private static File getDir() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PhotoOrganizer");
        dir.mkdirs();
        return dir;
    }

    public static File get(String filename) {
        return new File(getDir(), filename);
    }
}
