package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.R;

public class ViewCircleProgress extends ViewBase {
    Bitmap mBmpCache;
    Paint mPaintPorgress;
    Paint mPaintGrey;

    private int mWidth;
    private int mHeight;
    private int mMax;       // Set the range of the progress bar to 0...max.
    private int mProgress;
    private final float mStrokeWidth = 30.0f;

    public ViewCircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs!=null) {
            // must get attributes (attrs) in constructor,
            // it will not be valid anymore after construction finished
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.mxw, 0, 0);
            final int iDefValue = 0xff123456;
            final int iColor = a.getInt(R.styleable.mxw_fill_color, iDefValue);
            if (iColor==iDefValue) {
                UtilDBG.i("check the progressColor of ViewCirclePorgress is set or not");
            }

            mPaintPorgress = new Paint();
            mPaintPorgress.setStyle(Paint.Style.STROKE);
            mPaintPorgress.setColor(iColor);
            mPaintPorgress.setAntiAlias(true);
            mPaintPorgress.setStrokeWidth(mStrokeWidth);

            a.recycle();
        } else {
            UtilDBG.e("not expected, the attrs is null in ViewCircleProgress constructor");
        }

        mWidth = 0;
        mHeight = 0;
        mMax = 100;
        mProgress = 0;

        mPaintGrey = new Paint();
        mPaintGrey.setStyle(Paint.Style.STROKE);
        mPaintGrey.setColor(getResources().getColor(R.color.app_greye6));
        mPaintGrey.setAntiAlias(true);
        mPaintGrey.setStrokeWidth(mStrokeWidth);
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
            canvas.drawBitmap(mBmpCache, 0, 0, mPaintGrey);
        }

        super.onDraw(canvas);
    }

    void setMax(int max) {
        mMax = max;
        updateBmpCache();
    }

    public void setProgress(int progress){
        mProgress = progress;
        updateBmpCache();
    }

    private void updateBmpCache() {
        if (mWidth<=0 || mHeight<=0)
            return;

        int iRectX = 0;
        int iRectY = 0;
        int iRectW = 0;
        int iRectH = 0;
        if (mWidth>mHeight) {
            iRectX = (mWidth-mHeight)/2;
            iRectY = 0;
            iRectW = mHeight;
            iRectH = mHeight;
        } else {
            iRectX = 0;
            iRectY = (mHeight-mWidth)/2;
            iRectW = mWidth;
            iRectH = mWidth;
        }
        iRectX += mStrokeWidth/2;
        iRectY += mStrokeWidth/2;
        iRectW -= mStrokeWidth;
        iRectH -= mStrokeWidth;

        Canvas canvasCache = new Canvas(mBmpCache);
        canvasCache.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        RectF rect = new RectF(iRectX, iRectY, iRectX+iRectW, iRectY+iRectH);
        float fSweepAngle = 360* mProgress  / mMax;
        if (fSweepAngle>360)
            fSweepAngle = 360;
        canvasCache.drawArc(rect, -90, fSweepAngle, false, mPaintPorgress);
        canvasCache.drawArc(rect, -90 + fSweepAngle, 360-fSweepAngle, false, mPaintGrey);

        invalidate();
    }
}
