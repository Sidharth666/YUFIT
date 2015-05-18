package com.maxwell.bodysensor.ui;

import com.maxwell.bodysensor.util.UtilTimeElapse;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class OnSwipeTouchListener implements OnTouchListener {
    private View mView = null;

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        mView = view;
        return gestureDetector.onTouchEvent(motionEvent);
    }

    // when the onXxxxXxx() will be triggered
    // http://charles-android.blogspot.tw/2010/03/blog-post_521.html
    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        // [case 1] press, no move => trigger SimpleOnGestureListener.onLongPress();
        // [case 2] press, slightly move => trigger onScroll()
        // following variables are used for case 2
        private final UtilTimeElapse mElapse = new UtilTimeElapse("SimpleOnGestureListener");
        boolean mTriggerEmuLongPress = true;
        Long mLongPressTimeout = (long) (1000000 * ViewConfiguration.getLongPressTimeout());

        @Override
        public boolean onDown(MotionEvent e) {
            mTriggerEmuLongPress = true;
            mElapse.reset();

            return true; // if return false, onFling() will not be triggered
        }

        // onFling() is triggered when ACTION_UP
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mTriggerEmuLongPress = false;
            OnSwipeTouchListener.this.onLongPress();
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {

            if (mTriggerEmuLongPress) {
                if (mElapse.check(false, false)>mLongPressTimeout) {
                    float x1 = e1.getX();
                    float y1 = e1.getY();
                    float x2 = e2.getX();
                    float y2 = e2.getY();
                    // UtilDBG.i(Float.toString(x1-x2) + " | " + Float.toString(y1-y2));
                    if (Math.abs(x1-x2)<25 && Math.abs(y1-y2)<25) {
                        OnSwipeTouchListener.this.onLongPress();
                        mTriggerEmuLongPress = false;
                    }
                }
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onLongPress() {
    }

    public View getView() {
        return mView;
    }
}
