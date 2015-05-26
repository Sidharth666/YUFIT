package com.maxwell.bodysensor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.maxwell.bodysensor.R;
import com.maxwell.bodysensor.data.SleepMoveData;
import com.maxwell.bodysensor.data.SleepScore;
import com.maxwell.bodysensor.data.sleep.SleepLevel;
import com.maxwell.bodysensor.util.UtilCalendar;
import com.maxwell.bodysensor.util.UtilDBG;
import com.maxwell.bodysensor.util.UtilLocale;
import com.maxwell.bodysensor.util.UtilLocale.DateFmt;


public class ViewChart extends ViewBase {
    public static final int AD = 10;   // Activity, Day
    public static final int AW = 11;   // Activity, Week
    public static final int AM = 12;   // Activity, Month
    public static final int AY = 13;   // Activity, Year
    public static final int SD = 20;   // Sleep, Day
    public static final int SW = 21;   // Sleep, Week;
    public static final int SM = 22;   // Sleep, Month;
    public static final int SY = 23;   // Sleep, Year;

    private static final float TRESHOLD_DSLEEP_VERY_GOOD = 85f;
    private static final float TRESHOLD_DSLEEP_GOOD = 60f;

    int mWidth;
    int mHeight;
    float mXStart;
    float mYStart;
    float mXEnd;
    float mYEnd;
    float mInnerWidth;
    float mInnerHeight;
    float mBestHeight;
    float mTimeHintHeight;

    Bitmap mBmpCache;
    Bitmap mBmpCacheTimeHint;
    Bitmap mBmpBest;
    Bitmap mBmpTimeHint;

    int mCurrentMode = 0;
    boolean mHasTimeHint = false;
    float mHintPercentage = -1.0f;
    ViewChartData mChartData;
    SleepScore mSleepScore;

    private final Paint mPaintLineDivider;
    private final Paint mPaintLineGoal;
    private final Paint mPaintText;

    private final Paint [] mPaintBars;
    final int BAR_ACTIVITY_DAY = 0;
    final int BAR_ACTIVITY_WEEK = 1;
    final int BAR_SLEEP_VERY_GOOD = 2;
    final int BAR_SLEEP_GOOD = 3;
    final int BAR_SLEEP_POOR = 4;
    final int BAR_DEEP_SLEEP = 5;
    final int BAR_LIGHT_SLEEP = 6;
    final int BAR_AWAKE = 7;
    private final float BAR_RADIUS = 6.0f;

    // image: original size of ^
    // space: same height
    public ViewChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        mWidth = 0;
        mHeight = 0;

        mCurrentMode = 0;
        mHasTimeHint = false;
        mHintPercentage = -1.0f;

        mPaintLineDivider = createPaintLine(2, R.color.app_greye6);
        mPaintLineGoal = createPaintLine(2, R.color.app_grey58);
        mPaintLineGoal.setPathEffect(new DashPathEffect(new float[] {2,4}, 0));

        mPaintBars = new Paint [] {
                createPaintBar(R.color.app_activity_day), // activity (day/year)
                createPaintBar(R.color.app_activity_week), // activity (week/month)
                createPaintBar(R.color.app_sleep_very_good), // sleep very good
                createPaintBar(R.color.app_sleep_good), // sleep good
                createPaintBar(R.color.app_sleep_poor), // sleep poor
                createPaintBar(R.color.app_deep_sleep), // deep sleep
                createPaintBar(R.color.app_light_sleep), // light sleep
                createPaintBar(R.color.app_awake), // awake
        };

        mBmpBest = BitmapFactory.decodeResource(getResources(), R.drawable.activity_best);

        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mPaintText.setTextAlign(Align.CENTER);
        mPaintText.setColor(getResources().getColor(R.color.app_white));

        if (isInEditMode()) {
            return;
        }
    }

    @Override
    public void releaseResource() {
        releaseBitmap(mBmpCache);
        releaseBitmap(mBmpCacheTimeHint);
        releaseBitmap(mBmpBest);
        releaseBitmap(mBmpTimeHint);
    }

    private Paint createPaintLine(float strokeWidth, int iColorID) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        p.setColor(getResources().getColor(iColorID));

        return p;
    }

    private Paint createPaintBar(int iColorID) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(getResources().getColor(iColorID));

        return p;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w>0 && h>0) {
            // create again if needed
            if (mWidth!=w || mHeight!=h) {
                mBmpCache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                if (mHasTimeHint) {
                    mBmpCacheTimeHint = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                }
            }
            mWidth = w;
            mHeight = h;

            final int iPadding = 2;
            mXStart = iPadding;
            mYStart = iPadding;
            mXEnd = mWidth - iPadding;
            mYEnd = mHeight - iPadding;
            mInnerWidth = mWidth - 2 * iPadding;
            mInnerHeight = mHeight - 2 * iPadding;
            mBestHeight = mInnerHeight / 25.0f;
            mTimeHintHeight = mInnerHeight / 6.0f;
            mPaintText.setTextSize(mTimeHintHeight * 2/4);

            updateBmpCache();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBmpCache!=null) {
            canvas.drawBitmap(mBmpCache, 0, 0, mPaintLineDivider);
        }
        if (mBmpCacheTimeHint!=null) {
            canvas.drawBitmap(mBmpCacheTimeHint, 0, 0, mPaintLineDivider);
        }
        super.onDraw(canvas);
    }

    private void updateBmpCache() {
        if (mWidth<=0 || mHeight<=0)
            return;

        Canvas canvas = new Canvas(mBmpCache);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        switch (mCurrentMode) {
        case ViewChart.AD:
        case ViewChart.AW:
        case ViewChart.AM:
        case ViewChart.AY:
        case ViewChart.SW:
        case ViewChart.SM:
        case ViewChart.SY:
            subBmpCacheNormal(canvas);
            break;
        case ViewChart.SD:
            subBmpCacheSD(canvas);
            break;
        default:
            UtilDBG.e("unexpected, check below");
            UtilDBG.logMethod();
            break;
        }

        invalidate();
    }

    private void updateBmpCacheTimeHint() {
        if (mWidth<=0 || mHeight<=0)
            return;

        Canvas canvas = new Canvas(mBmpCacheTimeHint);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (mHintPercentage>=0.0 && mHintPercentage<=1.0) {
            float fLeft = mXStart + mInnerWidth * mHintPercentage;
            canvas.drawLine(fLeft, mYEnd, fLeft, mYStart + mTimeHintHeight, mPaintLineGoal);

            float fTimeHintWidth = mTimeHintHeight * mBmpTimeHint.getWidth() / mBmpTimeHint.getHeight();
            fLeft -= fTimeHintWidth / 2;
            if (fLeft < 0.0f)
                fLeft = 0.0f;
            if (fLeft > mInnerWidth - fTimeHintWidth)
                fLeft = mInnerWidth - fTimeHintWidth;
            RectF rectDst = new RectF(fLeft, 0, fLeft + fTimeHintWidth, mTimeHintHeight);
            canvas.drawBitmap(mBmpTimeHint, null, rectDst, mPaintLineDivider);

            long lStart = mSleepScore.mStartTime.getUnixTime();
            long lStop = mSleepScore.mStopTime.getUnixTime();
            long lTime = lStart + (long)((lStop-lStart) * mHintPercentage);
            UtilCalendar calTime = new UtilCalendar(lTime, mSleepScore.mStartTime.getTimeZone());
            fLeft += fTimeHintWidth/2;
            canvas.drawText(UtilLocale.calToString(calTime, DateFmt.HMa), fLeft, mYStart + mTimeHintHeight * 2 / 3, mPaintText);
        }

        invalidate();
    }

    private void subBmpCacheNormal(Canvas canvas) {
        int i = 0;
        int iBarCount = 1;
        boolean bHasBest = false;

        switch (mCurrentMode) {
        case ViewChart.AD:
            iBarCount = 24;
            bHasBest = true;
            break;
        case ViewChart.AW:
            iBarCount = 7;
            bHasBest = true;
            break;
        case ViewChart.AM:
            iBarCount = 31;
            bHasBest = true;
            break;
        case ViewChart.AY:
            iBarCount = 12;
            bHasBest = true;
            break;
        case ViewChart.SW:
            iBarCount = 7;
            break;
        case ViewChart.SM:
            iBarCount = 31;
            break;
        case ViewChart.SY:
            iBarCount = 12;
            break;
        }

        // draw vertical divider lines

        float [] fDividerPts = new float[(iBarCount+1)*4];
        for (i=0; i<=iBarCount; ++i) {
            fDividerPts[4*i] = mXStart + i * mInnerWidth / iBarCount;
            fDividerPts[4*i+1] = mYStart;
            fDividerPts[4*i+2] = mXStart + i * mInnerWidth / iBarCount;
            fDividerPts[4*i+3] = mYEnd;
        }
        canvas.drawLines(fDividerPts, mPaintLineDivider);

        // only draw data if it is valid

        if (mChartData==null || !mChartData.mIsValid) {
            return;
        }

        if (iBarCount < mChartData.mSize) {
            UtilDBG.e("unexpected  subBmpCacheNormal, bar count < bar size");
        }

        for (i=0; i<mChartData.mSize; ++i) {
            switch(mCurrentMode) {
            case ViewChart.AD:
                mChartData.mColorIndexes[i] = BAR_ACTIVITY_DAY;
                break;
            case ViewChart.AW:
            case ViewChart.AM:
                mChartData.mColorIndexes[i] = ((mChartData.mValues[i] >= mChartData.mGoals[i]) ? BAR_ACTIVITY_DAY : BAR_ACTIVITY_WEEK);
                break;
            case ViewChart.AY:
                mChartData.mColorIndexes[i] = BAR_ACTIVITY_DAY;
                break;
            case ViewChart.SW:
            case ViewChart.SM:
            case ViewChart.SY:
                double score = mChartData.mValues[i];
                if (score >= TRESHOLD_DSLEEP_VERY_GOOD) {
                    mChartData.mColorIndexes[i] = BAR_SLEEP_VERY_GOOD;
                } else if (score < TRESHOLD_DSLEEP_VERY_GOOD && score >= TRESHOLD_DSLEEP_GOOD) {
                    mChartData.mColorIndexes[i] = BAR_SLEEP_GOOD;
                } else {
                    mChartData.mColorIndexes[i] = BAR_SLEEP_POOR;
                }
                break;
            default:
                UtilDBG.e("not expected, when drawing ViewChart");
            }
        }

        float yBarBottom = mYEnd;
        float yBarTopBase = mYEnd - 2 * BAR_RADIUS;
        float yBarYExtend = mInnerHeight - 2 * BAR_RADIUS - (bHasBest ? mBestHeight * 2 : 0);

        RectF rect = new RectF();

        double dMaxValue = 100.0f;
        boolean bValidMaxValue = false;

        if ((mCurrentMode == AD || mCurrentMode == AW || mCurrentMode == AM || mCurrentMode == AY)) {
            if (mChartData.mMaxValueIndex>=0 && mChartData.mMaxValueIndex < mChartData.mSize) {
                dMaxValue = mChartData.mValues[mChartData.mMaxValueIndex];
                bValidMaxValue = true;
            } else {
                bValidMaxValue = false;
            }
        }

        for (i=0; i<mChartData.mSize; ++i) {
            if (mChartData.mValues[i]==-1) {
                continue;
            }
            rect.set(mXStart + i * mInnerWidth / iBarCount + mInnerWidth / 3 / iBarCount,
                    (float)(yBarTopBase - yBarYExtend * mChartData.mValues[i] / dMaxValue),
                    mXStart + i * mInnerWidth / iBarCount + mInnerWidth *2 / 3 / iBarCount,
                    yBarBottom);
            canvas.drawRoundRect(rect, BAR_RADIUS, BAR_RADIUS, mPaintBars[mChartData.mColorIndexes[i]]);
        }

        if (bHasBest && bValidMaxValue) {
            float fBestWidth = mBestHeight * mBmpBest.getWidth() / mBmpBest.getHeight();
            float fBestLeft = mXStart + mChartData.mMaxValueIndex * mInnerWidth / iBarCount + mInnerWidth / 2 / iBarCount - fBestWidth/2;
            RectF rectDst = new RectF(fBestLeft, mYStart, fBestLeft + fBestWidth, mYStart + mBestHeight);
            canvas.drawBitmap(mBmpBest, null, rectDst, mPaintLineDivider);
        }

        if (mCurrentMode == AW || mCurrentMode==AM) {
            int [] iDrawGoals = new int [mChartData.mSize];
            int iPreviousGoal = 0;

            // ex. original: 0,     4000,   5000,   0,      5000,   3000,   0
            // change to:    4000,  4000,   5000,   5000,   5000,   3000,   3000

            // find first non-zero
            for (i=0; i<mChartData.mSize; ++i) {
                if (mChartData.mGoals[i] != 0) {
                    iPreviousGoal = mChartData.mGoals[i];
                    break;
                }
            }

            // change the zero values
            for (i=0; i<mChartData.mSize; ++i) {
                if (mChartData.mGoals[i] != 0) {
                    iDrawGoals[i] = mChartData.mGoals[i];
                    iPreviousGoal = mChartData.mGoals[i];
                } else {
                    iDrawGoals[i] = iPreviousGoal;
                }
            }

            // start drawing it
            int iBegin = 0, iEnd = 0;
            int iGoalDraw;
            for (i=0; i<mChartData.mSize; i=iEnd) {
                iBegin = i;
                iGoalDraw = iDrawGoals[iBegin];
                for (iEnd = iBegin; iEnd<mChartData.mSize; ++iEnd) {
                    if (iGoalDraw != iDrawGoals[iEnd]) {
                        break;
                    }
                }

                if (iGoalDraw >=0 && iGoalDraw < dMaxValue) {
                    canvas.drawLine(
                            mXStart + iBegin * mInnerWidth / iBarCount,
                            (float)(yBarTopBase - yBarYExtend * iGoalDraw / dMaxValue),
                            mXStart + iEnd * mInnerWidth / iBarCount,
                            (float)(yBarTopBase - yBarYExtend * iGoalDraw / dMaxValue),
                            mPaintLineGoal);
                    if (iEnd>=0 && iEnd<mChartData.mSize) {
                        canvas.drawLine(
                                mXStart + iEnd * mInnerWidth / iBarCount,
                                (float)(yBarTopBase - yBarYExtend * iGoalDraw / dMaxValue),
                                mXStart + iEnd * mInnerWidth / iBarCount,
                                (float)(yBarTopBase - yBarYExtend * iDrawGoals[iEnd] / dMaxValue),
                                mPaintLineGoal);
                    }
                }
            }
        }

        if (mCurrentMode == SW || mCurrentMode == SM || mCurrentMode == SY) {
            canvas.drawLine(
                    mXStart,
                    (float)(yBarTopBase - yBarYExtend / 3.0),
                    mXEnd,
                    (float)(yBarTopBase - yBarYExtend / 3.0),
                    mPaintLineGoal);
            canvas.drawLine(
                    mXStart,
                    (float)(yBarTopBase - yBarYExtend * 2.0 / 3.0),
                    mXEnd,
                    (float)(yBarTopBase - yBarYExtend * 2.0 / 3.0),
                    mPaintLineGoal);
        }
    }

    private void subBmpCacheSD(Canvas canvas) {
        if (mCurrentMode!=ViewChart.SD) {
            return;
        }

        if (mSleepScore==null) {
            return;
        }

        long lStartSecond = mSleepScore.mStartTime.getUnixTime();
        long lTotalSecond = (mSleepScore.mStopTime.getUnixTime()-lStartSecond);
        float fStart;
        float fEnd;
        float fSectionTop = 0;
        float yBarBottom = mYEnd;
        float yBarYExtend = mInnerHeight - mTimeHintHeight * 3 / 2;
        int iColorIndex = 0;
        RectF rect = new RectF();

        int i, j;
        SleepMoveData dataStart;
        int size = mSleepScore.mListSleepMove.size();
        for (i=0; i<size; ++i) {
            for (j=i+1; j<size; ++j) {
                if (mSleepScore.mListSleepMove.get(i).mLevel!=
                        mSleepScore.mListSleepMove.get(j).mLevel) {
                    break;
                }
            }
            --j;

            dataStart = mSleepScore.mListSleepMove.get(i);
            fStart = mXStart + mInnerWidth * (dataStart.mTime.getUnixTime() - lStartSecond) / lTotalSecond;
            fEnd = mXStart + mInnerWidth * (mSleepScore.mListSleepMove.get(j).mTime.getUnixTime() + 15*60 - lStartSecond) / lTotalSecond;
            if (fStart < mXStart)
                fStart = mXStart;
            if (fEnd > mXEnd)
                fEnd = mXEnd;

            if (dataStart.mLevel == SleepLevel.DEEP) {
                fSectionTop = yBarBottom - yBarYExtend / 3;
                iColorIndex = BAR_DEEP_SLEEP;
            } else if (dataStart.mLevel == SleepLevel.LIGHT) {
                fSectionTop = yBarBottom - yBarYExtend * 2 / 3;
                iColorIndex = BAR_LIGHT_SLEEP;
            } else if (dataStart.mLevel == SleepLevel.AWAKE) {
                fSectionTop = yBarBottom - yBarYExtend;
                iColorIndex = BAR_AWAKE;
            }

            rect.set(fStart,
                    fSectionTop,
                    fEnd,
                    yBarBottom);
            canvas.drawRect(rect, mPaintBars[iColorIndex]);
        }

        if (mHintPercentage>=0.0f) {

        }
    }

    public void setChartData(int mode, ViewChartData chartData) {
        mCurrentMode = mode;
        mChartData = chartData;

        updateBmpCache();
    }

    public void setSleepScore(int mode, SleepScore score) {
        mCurrentMode = mode;
        mSleepScore = score;

        updateBmpCache();
    }

    public static class ViewChartData {
        public ViewChartData(int size) {
            mIsValid = false;
            mSize = size;
            mMaxValueIndex = -1;
            mValues = new double [mSize];
            mGoals = new int [mSize];
            mColorIndexes = new int [mSize];
            for (int i=0; i<mSize; ++i) {
                mValues[i] = -1;
                mGoals[i] = 0;
                mColorIndexes[i] = 0;
            }
        }

        void initValues(double value) {
            for (int i=0; i<mSize; ++i) {
                mValues[i] = value;
            }
        }

        void initGoals(int value) {
            for (int i=0; i<mSize; ++i) {
                mGoals[i] = value;
            }
        }

        public boolean mIsValid;
        public int mSize;
        public int mMaxValueIndex;
        public double [] mValues; // 0: means has data, and value is 0 | -1 means no data
        public int [] mGoals;
        public int [] mColorIndexes;
    }

    public void setHasTimeHint(boolean b) {
        mHasTimeHint = b;
        if (mHasTimeHint) {
            if (mBmpCacheTimeHint == null) {
                if (mWidth>0 && mHeight>0) {
                    mBmpCacheTimeHint = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                }
            } else if (mBmpCacheTimeHint.getWidth()!=mWidth ||
                    mBmpCacheTimeHint.getHeight()!=mHeight) {
                mBmpCacheTimeHint.recycle();
                mBmpCacheTimeHint = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            }

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    if (mCurrentMode!=ViewChart.SD) {
                        return false;
                    }
                    if (mSleepScore==null) {
                        return false;
                    }

                    final int x = (int) event.getX();
                    int iMask = event.getAction() & MotionEvent.ACTION_MASK;
                    boolean bUp = (iMask & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP;
                    // boolean bMove = (iMask & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE;

                    float fPercentage = -1.0f;
                    if (bUp) {
                        fPercentage = -1.0f;
                    } else {
                        fPercentage = (x - mXStart) / mInnerWidth;
                        if (fPercentage > 1.0f) {
                            fPercentage = 1.0f;
                        } else if (fPercentage < 0.0f) {
                            fPercentage = 0.0f;
                        }
                    }

                    if (mHintPercentage != fPercentage) {
                        mHintPercentage = fPercentage;
                        updateBmpCacheTimeHint();
                    }
                    return true;
                }
            });
        }
    }
}
