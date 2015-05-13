package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.maxwell.bodysensor.R;

public class ViewRadioButton extends RadioButton {

    public ViewRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewRadioButton(Context context, AttributeSet attrs, int defStyle) {
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

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // Ref. http://stackoverflow.com/questions/4504024/android-localization-problem-not-all-items-in-the-layout-update-properly-when-s
        final CharSequence text = getText();
        super.onRestoreInstanceState(state);
        setText(text);
    }
}