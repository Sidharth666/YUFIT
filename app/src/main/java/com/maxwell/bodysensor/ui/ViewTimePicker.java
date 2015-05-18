package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TimePicker;

public class ViewTimePicker extends TimePicker {
    public ViewTimePicker(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initSetup();
    }

    public ViewTimePicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initSetup();
    }

    public ViewTimePicker(Context context)
    {
        super(context);
        initSetup();
    }

    private void initSetup() {
        setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // Prevent parent controls from stealing our events once we've gotten a touch down
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            ViewParent p = getParent();
            if (p != null)
                p.requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }

}
