package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.util.UtilDBG;

public class ViewDotProgress extends View{
    Bitmap mBmpCache;
    Paint mPaintFill = new Paint();
    Paint mPaintProgress = new Paint();
    Paint mPaintNonProgress = new Paint();
    int mSize = 7;
    int mProgress = 1;
    int mViewW = -1;
    int mViewH = -1;

    public ViewDotProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewDotProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w>0 && h>0) {
            mViewW = w;
            mViewH = h;
//            int [] colors = {
//                    getResources().getColor(R.color.darkgrey),
//                    getResources().getColor(R.color.black),
//                    getResources().getColor(R.color.darkgrey),
//            };
//            LinearGradient gradient = new LinearGradient(0, 0, 0, mViewH, colors, null, Shader.TileMode.CLAMP);
            mPaintFill.setDither(true);
            mPaintFill.setColor(Color.WHITE);

            mPaintProgress.setAntiAlias(true);
            mPaintProgress.setStyle(Paint.Style.FILL);
            mPaintProgress.setColor(getResources().getColor(R.color.app_theme));

            mPaintNonProgress.setAntiAlias(true);
            mPaintNonProgress.setStyle(Paint.Style.FILL);
            mPaintNonProgress.setColor(getResources().getColor(R.color.app_greyba));

            mBmpCache = Bitmap.createBitmap(mViewW, mViewH, Bitmap.Config.ARGB_8888);
            drawBmpCache();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBmpCache!=null) {
            canvas.drawBitmap(mBmpCache, 0, 0, mPaintFill);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = resolveSize(120, widthMeasureSpec);
        int h = resolveSize(120, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    // Set the range of the progress bar to 0... iSize-1
    public void setSize(int iSize) {
        if (iSize>0) {
            mSize = iSize;
        } else {
            UtilDBG.logMethod();
            UtilDBG.e("iSize, unexpected value: " + Integer.toString(iSize));
        }

        if (!validProgress(mSize, mProgress)) {
            mProgress = 0;
        }

        drawBmpCache();
    }

    // set the current progress, it must in the range 0, mSize-1
    public void setProgress(int iProgress) {
        if (validProgress(mSize, iProgress)) {
            mProgress = iProgress;
        }

        drawBmpCache();
    }

    private boolean validProgress(int size, int progress) {
        return (progress>=0 && progress<size);
    }

    private void drawBmpCache() {
        if (mViewW<=0 || mViewH<=0)
            return;

        Canvas canvasCache = new Canvas(mBmpCache);
        RectF rect = new RectF(0, 0, mViewW, mViewH);
        canvasCache.drawRect(rect, mPaintFill);

        float fRadius = mViewH / 5;
        float fSpace = fRadius * 2;
        float cx = mViewW / 2 - (2*fRadius + fSpace) * (mSize-1) / 2;
        float cy = mViewH / 2;

        for (int i=0; i<mSize; ++i) {
            canvasCache.drawCircle(cx, cy, fRadius, mProgress==i ? mPaintProgress : mPaintNonProgress);
            cx += (fSpace + fRadius*2);
        }

        invalidate();
    }
}
