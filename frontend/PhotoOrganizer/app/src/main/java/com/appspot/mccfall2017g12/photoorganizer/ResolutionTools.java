package com.appspot.mccfall2017g12.photoorganizer;

import android.graphics.BitmapFactory;

public class ResolutionTools {

    public final static int RESOLUTION_LOW = 1;
    public final static int RESOLUTION_HIGH = 2;

    public final static int SIZE_LOW = 640;
    public final static int SIZE_HIGH = 1280;

    public static final String LEVEL_LOW = "low";
    public static final String LEVEL_HIGH = "high";
    public static final String LEVEL_FULL = "full";

    /**
     * Calculates the resolution of an image file stored locally.
     * See doc/resolution.txt for further information.
     *
     * @param filePath the image file full path
     * @return Resolution
     */
    public static int calculateResolution(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int w = options.outWidth;
        if (w <= SIZE_LOW)
            return 1;
        else if (w <= SIZE_HIGH)
            return 2;
        else
            return 3;
    }

    public static int getResolution(String resolutionLevel) {
        switch (resolutionLevel) {
            case LEVEL_LOW:
                return RESOLUTION_LOW;
            case LEVEL_HIGH:
                return RESOLUTION_HIGH;
            case LEVEL_FULL:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int getSize(String resolutionLevel) {
        switch (resolutionLevel) {
            case LEVEL_LOW:
                return SIZE_LOW;
            case LEVEL_HIGH:
                return SIZE_HIGH;
            case LEVEL_FULL:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static int getResolution(String resolutionLevel, int fullResolution) {
        return Math.min(getResolution(resolutionLevel), fullResolution);
    }
}
