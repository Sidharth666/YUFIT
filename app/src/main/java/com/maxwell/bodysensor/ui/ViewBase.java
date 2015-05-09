package com.maxwell.bodysensor.ui;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.maxwell.bodysensor.util.UtilDBG;

public abstract class ViewBase extends View {

    public ViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // force the derived class, to implement the resource releasing function, when it is detached

    protected abstract void releaseResource();

    // some helper function

    public void releaseBitmap(Bitmap b) {
        if (b != null) {
            b.recycle();
            b = null;
        }
    }

}
