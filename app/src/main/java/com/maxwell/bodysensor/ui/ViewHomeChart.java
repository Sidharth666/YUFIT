package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.HourlyRecordData;

import java.util.Calendar;
import java.util.List;

public class ViewHomeChart  extends ViewBase {
    int mWidth;
    int mHeight;
    Bitmap mBmpCache;

    private final Paint mPaintLine;
    private final Paint mPaintData;
    private final Paint mPaintDataUnder;
    private final float mDataStrokeWidth = 3.0f;
    private List<HourlyRecordData> mHourlyDatas;

    public ViewHomeChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        mWidth = 0;
        mHeight = 0;

        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(1);
        mPaintLine.setColor(getResources().getColor(R.color.app_greye6));

        int iColor = getResources().getColor(R.color.app_theme_color_2);

        mPaintData = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintData.setColor(iColor);
        mPaintData.setStyle(Paint.Style.STROKE);
        mPaintData.setStrokeWidth(mDataStrokeWidth);

        int iColorUnder = getResources().getColor(R.color.app_orange_a33);
        mPaintDataUnder = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintDataUnder.setColor(iColorUnder);
        mPaintDataUnder.setStyle(Paint.Style.FILL);
    }

    @Override
    public void releaseResource() {
        releaseBitmap(mBmpCache);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w>0 && h>0) {
            // create again if needed
            if (mWidth!=w || mHeight!=h) {
                mBmpCache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            mWidth = w;
            mHeight = h;
            updateBmpCache();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBmpCache!=null) {
            canvas.drawBitmap(mBmpCache, 0, 0, mPaintLine);
        }
        super.onDraw(canvas);
    }

    private void updateBmpCache() {
        if (mWidth<=0 || mHeight<=0)
            return;

        Canvas canvas = new Canvas(mBmpCache);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        final int iPadding = (int)(mDataStrokeWidth / 2) + 1;
        final int xStart = iPadding;
        final int yStart = iPadding;
        // final int xEnd = mWidth - iPadding;
        final int yEnd = mHeight - iPadding;
        final int width = mWidth - 2 * iPadding;
        final int height = mHeight - 2 * iPadding;

        float pts[] = new float [25*2*2];
        int i;
        for (i=0; i<=24; ++i) {
            pts[4*i] = xStart + i * width / 24;
            pts[4*i+1] = yStart;
            pts[4*i+2] = xStart + i * width / 24;
            pts[4*i+3] = yEnd;
        }
        canvas.drawLines(pts, mPaintLine);

        if (mHourlyDatas!=null) {
            double dMaxAppEnergy = -1;
            double dAppEnergy[] = new double [25];
            dAppEnergy[24] = 0;

            for (HourlyRecordData data : mHourlyDatas) {
                if (dMaxAppEnergy < data.mAppEnergy)
                    dMaxAppEnergy = data.mAppEnergy;
                dAppEnergy[data.mTime.get(Calendar.HOUR_OF_DAY)] = data.mAppEnergy;
            }

            float ptsData[] = new float [24*2*2];

            for (i=0; i<24; ++i) {
                ptsData[4*i] = xStart + i * width / 24;
                ptsData[4*i+1] = yEnd - (float) (dAppEnergy[i] / dMaxAppEnergy * height);
                ptsData[4*i+2] = xStart + (i+1) * width / 24;
                ptsData[4*i+3] = yEnd - (float) (dAppEnergy[i+1] / dMaxAppEnergy * height);

                Path path = new Path();
                path.moveTo(ptsData[4*i], yEnd);
                path.lineTo(ptsData[4*i], ptsData[4*i+1]);
                path.lineTo(ptsData[4*i+2], ptsData[4*i+3]);
                path.lineTo(ptsData[4*i+2], yEnd);
                path.close();

                canvas.drawPath(path, mPaintDataUnder);
            }

            canvas.drawLines(ptsData, mPaintData);
        }

        invalidate();
    }

    public void setHourlyDatas(List<HourlyRecordData> HourlyDatas) {
        mHourlyDatas = HourlyDatas;
        updateBmpCache();
    }
}
