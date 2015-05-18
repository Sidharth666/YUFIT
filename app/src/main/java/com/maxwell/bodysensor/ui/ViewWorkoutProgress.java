package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwellguider.bluetooth.activitytracker.FitnessType;

/**
 * Created by ryanhsueh on 15/2/5.
 */
public class ViewWorkoutProgress extends ViewBase {

    private static final String GOAL_DEFAULT = "20";
    private static final String GOAL_TREADMILL = "1km";

    private Bitmap mBmpCache;
    private Paint mPaintPorgress;
    private Paint mPaintCheck;
    private Paint mPaintText;

    private int mWidth;
    private int mHeight;
    private int mProgress;
    private final float mStrokeWidth = 3.0f;
    private final float mTextSize = 60f;

    private FitnessType mFitnessType;
    private String mGoal;

    public ViewWorkoutProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs!=null) {
            // must get attributes (attrs) in constructor,
            // it will not be valid anymore after construction finished
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.mxw, 0, 0);

            mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
            mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaintText.setColor(Color.WHITE);
            mPaintText.setTextAlign(Paint.Align.CENTER);
            mPaintText.setTextSize(mTextSize);

            a.recycle();
        } else {
            UtilDBG.e("not expected, the attrs is null in ViewCircleProgress constructor");
        }

        mWidth = 0;
        mHeight = 0;
        mProgress = 0;

        mPaintPorgress = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintPorgress.setStyle(Paint.Style.STROKE);
        mPaintPorgress.setColor(Color.WHITE);
        mPaintPorgress.setStrokeWidth(mStrokeWidth);

        mPaintCheck = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintCheck.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintCheck.setColor(getResources().getColor(R.color.app_orange));
    }

    @Override
    public void releaseResource() {
        releaseBitmap(mBmpCache);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w>0 && h>0) {
            mWidth = w;
            mHeight = h;

            mBmpCache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            updateBmpCache();
        } else if (w==0 || h==0) {
            mBmpCache.recycle();
            mBmpCache = null;
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBmpCache!=null) {
            canvas.drawBitmap(mBmpCache, 0, 0, mPaintPorgress);
        }

        super.onDraw(canvas);
    }

    public void setFitnessType(FitnessType type) {
        mFitnessType = type;
        if (mFitnessType == FitnessType.TREADMILL) {
            mGoal = GOAL_TREADMILL;
        } else {
            mGoal = GOAL_DEFAULT;
        }
    }

    public void setProgress(int progress){
        if (mProgress != progress) {
            mProgress = progress;
            updateBmpCache();
        }
    }

    private void updateBmpCache() {
        if (mWidth<=0 || mHeight<=0)
            return;

        UtilDBG.e("ViewWorkoutProgress > mProgress:" + mProgress);



        float radius = mHeight * 0.2f;
        float line = radius * 0.8f;

        float xCenter = mWidth * 0.5f;
        float yCenter = mHeight * 0.5f;

        float offset = 0;

        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
        float offsetTextY = ((mPaintText.descent() + mPaintText.ascent()) / 2);
        float yCenterText = yCenter - offsetTextY;

        Canvas canvasCache = new Canvas(mBmpCache);
        canvasCache.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // clear canvas

        // draw circle
        offset = 2*radius + line;

        Bitmap bmpChecked = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.checked),
                (int)(2*radius),
                (int)(2*radius),
                false);

        float cx = xCenter;
        float cy = yCenter;

        // circle 1
        cx = xCenter - 2 * offset;
        if (mProgress >= 1) {
            canvasCache.drawBitmap(bmpChecked, cx - radius, cy - radius, mPaintCheck);
        } else {
            canvasCache.drawCircle(cx, cy, radius, mPaintPorgress);
            canvasCache.drawText(mGoal, cx, yCenterText, mPaintText);
        }

        // circle 2
        cx = xCenter - offset;
        if (mProgress >= 2) {
            canvasCache.drawBitmap(bmpChecked, cx - radius, cy - radius, mPaintCheck);
        } else {
            canvasCache.drawCircle(cx, cy, radius, mPaintPorgress);
            canvasCache.drawText(mGoal, cx, yCenterText, mPaintText);
        }

        // circle 3
        cx = xCenter;
        if (mProgress >= 3) {
            canvasCache.drawBitmap(bmpChecked, cx - radius, cy - radius, mPaintCheck);
        } else {
            canvasCache.drawCircle(cx, cy, radius, mPaintPorgress);
            canvasCache.drawText(mGoal, cx, yCenterText, mPaintText);
        }

        // circle 4
        cx = xCenter + offset;
        if (mProgress >= 4) {
            canvasCache.drawBitmap(bmpChecked, cx - radius, cy - radius, mPaintCheck);
        } else {
            canvasCache.drawCircle(cx, cy, radius, mPaintPorgress);
            canvasCache.drawText(mGoal, cx, yCenterText, mPaintText);
        }

        // circle 5
        cx = xCenter + 2 * offset;
        if (mProgress >= 5) {
            canvasCache.drawBitmap(bmpChecked, cx - radius, cy - radius, mPaintCheck);
        } else {
            canvasCache.drawCircle(cx, cy, radius, mPaintPorgress);
            canvasCache.drawText(mGoal, cx, yCenterText, mPaintText);
        }

        bmpChecked.recycle();
        bmpChecked = null;

        // draw line
        offset = radius;
        canvasCache.drawLine(xCenter+offset, yCenter, xCenter+offset+line, yCenter, mPaintPorgress);
        canvasCache.drawLine(xCenter-offset, yCenter, xCenter-offset-line, yCenter, mPaintPorgress);

        offset = offset+line + 2*radius;
        canvasCache.drawLine(xCenter+offset, yCenter, xCenter+offset+line, yCenter, mPaintPorgress);
        canvasCache.drawLine(xCenter-offset, yCenter, xCenter-offset-line, yCenter, mPaintPorgress);



        invalidate();
    }
}
