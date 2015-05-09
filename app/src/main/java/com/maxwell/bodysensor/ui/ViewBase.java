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

    public static Typeface getTypeface(Context context, String fontName) {
        if (fontName != null) {
            try {
                // first, check the font file exist or not
                AssetManager mng = context.getAssets();
                InputStream is = mng.open("font/" + fontName);
                is.close();

                return Typeface.createFromAsset(context.getAssets(), "font/" + fontName);
            } catch (IOException ex) {
                UtilDBG.e("the font file not exist: " + fontName);
                ex.printStackTrace();
            }
        }

        return null;
    }
}
