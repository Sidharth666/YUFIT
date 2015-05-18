package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.maxwell.bodysensor.R;

public class ViewTextView extends TextView {
    public ViewTextView(Context context) {
        super(context);
    }

    public ViewTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Typeface.createFromAsset doesn't work in the layout editor. Skipping...
        // this will make sure the "Graphical Layout" in "Eclipse" look correct
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.mxw);
        String fontName = styledAttrs.getString(R.styleable.mxw_typeface);
        styledAttrs.recycle();

    }
}