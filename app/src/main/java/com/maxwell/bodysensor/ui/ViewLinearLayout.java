package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ViewLinearLayout extends LinearLayout{
    boolean mHideInputNonEditing; // if not selecting a EditText, hide input
    DialogFragment mDF;

    public ViewLinearLayout(Context context) {
        super(context);
    }

    public ViewLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mHideInputNonEditing && ev.getAction()==MotionEvent.ACTION_UP) {
            boolean ret = super.onInterceptHoverEvent(ev);

            View v = null;
            if (mDF.getDialog()!= null) {
                v = mDF.getDialog().getWindow().getCurrentFocus();
            } else {
                v = mDF.getActivity().getCurrentFocus();
            }

            if (v!=null && v instanceof EditText) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);
                if (!rect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    // clear the cursor
                    v.clearFocus();
                    // hide the soft input / soft keyboard
                    InputMethodManager imm = (InputMethodManager) mDF.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return ret;
        }

        return super.onInterceptHoverEvent(ev);
    }

    public void setHideInputNonEditing(boolean b, DialogFragment df) {
        mHideInputNonEditing = b;
        mDF = df;
    }
}
