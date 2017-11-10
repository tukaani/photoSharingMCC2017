package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.widget.Toast;

abstract class MenuItem {

    @StringRes
    private int captionId;

    @DrawableRes
    private int thumbId;

    protected MenuItem(@StringRes int captionId, @DrawableRes int thumbId) {
        this.thumbId = thumbId;
        this.captionId = captionId;
    }

    @StringRes
    public int getCaptionId() {
        return this.captionId;
    }

    @DrawableRes
    public int getThumbId() {
        return this.thumbId;
    }

    public abstract void launch(Context context);
}
