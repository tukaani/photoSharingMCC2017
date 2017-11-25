package com.appspot.mccfall2017g12.photoorganizer;

import android.graphics.BitmapFactory;

public class ResolutionTools {

    public final static int REFERENCE_HEIGHT = 3;
    public final static int REFERENCE_WIDTH = 4;

    public final static int RESOLUTION_LOW = 480;
    public final static int RESOLUTION_HIGH = 960;

    public static final String LEVEL_LOW = "low";
    public static final String LEVEL_HIGH = "high";
    public static final String LEVEL_FULL = "full";

    /**
     * Calculates the resolution of an image file stored locally.
     * Resolution here is defined as follows:
     * Resolution is the height of a rectangle whose aspect ratio is
     * {@literal REFERENCE_WIDTH} / {@literal REFERENCE_HEIGHT} and
     * which totally covers the image.
     *
     * @param filePath the image file full path
     * @return Resolution
     */
    public static int calculateResolution(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if (REFERENCE_HEIGHT * options.outWidth > REFERENCE_WIDTH * options.outHeight)
            return ceiledDivision(REFERENCE_HEIGHT * options.outWidth, REFERENCE_WIDTH);
        else
            return options.outHeight;
    }

    /**
     * Returns correctly ceiled division of two integers
     *
     * @param a Dividend
     * @param b Divisor
     * @return ceil(a divided by b)
     */
    private static int ceiledDivision(int a, int b) {
        return (a + b - 1) / b;
    }

    public static int getResolution(String resolutionLevel, int fullResolution) {
        int resolution;

        switch (resolutionLevel) {
            case LEVEL_LOW:
                resolution = RESOLUTION_LOW;
                break;
            case LEVEL_HIGH:
                resolution = RESOLUTION_HIGH;
                break;
            case LEVEL_FULL:
                resolution = fullResolution;
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (resolution > fullResolution)
            resolution = fullResolution;

        return resolution;
    }
}
