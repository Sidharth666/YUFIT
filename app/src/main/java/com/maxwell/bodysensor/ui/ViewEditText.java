package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.maxwell.bodysensor.R;

public class ViewEditText extends EditText {

    public ViewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

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
