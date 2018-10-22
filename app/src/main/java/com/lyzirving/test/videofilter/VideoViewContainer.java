package com.lyzirving.test.videofilter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * @author lyzirving
 *         time        2018/10/15
 *         email       lyzirving@sina.com
 *         information
 */

public class VideoViewContainer extends FrameLayout {

    private RectF mRenderRect;
    private ImageView mSticker;
    private float mStartX, mStartY;
    private float mShiftX, mShiftY;
    private float mStickerOriginalLeft, mStickerOriginalTop;
    private int mStickerW, mStickerH;
    private RectF mStickerRect;

    public VideoViewContainer(@NonNull Context context) {
        this(context, null);
    }

    public VideoViewContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRenderRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.clipRect(mRenderRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!hasSticker()) {
            return super.onTouchEvent(event);
        }
        return dealWithSticker(event);
    }

    public float getXTranslateRatio() {
        float result = 2f * mShiftX / mRenderRect.width();
        return result;
    }

    public float getYTranslateRatio() {
        float result = 2f * mShiftY / mRenderRect.height();
        return result;
    }

    public float[] calculateStickerSizeRatio(int degree) {
        float[] result = new float[4];
        float left = mStickerRect.left;
        float right = mStickerRect.right;
        float top = mStickerRect.top;
        float bottom = mStickerRect.bottom;
        float width = mRenderRect.width();
        float height = mRenderRect.height();
        switch (degree) {//0为left，1为top，2为right，3为bottom
            case 0:
                result[0] = (left - width / 2f) / (width / 2f);
                result[2] = (right - width / 2f) / (width / 2f);
                result[1] = (height / 2f - top) / (height / 2f);
                result[3] = (height / 2f - bottom) / (height / 2f);
                break;
            case 90:
                result[0] = (top - height / 2f) / (height / 2f);
                result[2] = (bottom - height / 2f) / (height / 2f);
                result[1] = (right - width / 2f) / (width / 2f);
                result[3] = (left - width / 2f) / (width / 2f);
                break;
        }
        return result;
    }

    private boolean hasSticker() {
        boolean result = false;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                result = child.getVisibility() == VISIBLE ? true : false;
                break;
            }
        }
        return result;
    }

    private boolean dealWithSticker(MotionEvent e) {
        if (mSticker == null) {
            mSticker = getSticker();
            mStickerW = mSticker.getMeasuredWidth();
            mStickerH = mSticker.getMeasuredHeight();
            mStickerOriginalLeft = mSticker.getLeft();
            mStickerOriginalTop = mSticker.getTop();
            mStickerRect = new RectF(mStickerOriginalLeft, mStickerOriginalTop,
                    mStickerOriginalLeft + mStickerW, mStickerOriginalTop + mStickerH);
        }
        boolean result = true;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = e.getX();
                mStartY = e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mShiftX += e.getX() - mStartX;
                mShiftY += e.getY() - mStartY;
                mStartX = e.getX();
                mStartY = e.getY();
                limitTranslation();
                updateStickerRect();
                mSticker.setTranslationX(mShiftX);
                mSticker.setTranslationY(mShiftY);
                break;
            case MotionEvent.ACTION_UP:
              break;
        }
        return result;
    }

    private void updateStickerRect() {
        mStickerRect.set(mStickerOriginalLeft + mShiftX, mStickerOriginalTop + mShiftY,
                mStickerOriginalLeft + mShiftX + mStickerW, mStickerOriginalTop + mShiftY + mStickerH);
    }

    private void limitTranslation() {
        float currentLeft = mStickerOriginalLeft + mShiftX;
        float currentTop = mStickerOriginalTop + mShiftY;
        float currentRight = currentLeft + mStickerW;
        float currentBottom = currentTop + mStickerH;
        if (currentRight < mRenderRect.left + mStickerW / 2) {
            mShiftX = -mRenderRect.width() / 2;
        } else if (currentLeft > mRenderRect.right - mStickerW / 2) {
            mShiftX = mRenderRect.width() / 2;
        }
        if (currentBottom < mRenderRect.top + mStickerH / 2) {
            mShiftY = -mRenderRect.height() / 2;
        } else if (currentTop > mRenderRect.bottom - mStickerH / 2) {
            mShiftY = mRenderRect.height() / 2;
        }
    }

    private ImageView getSticker() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                return (ImageView) child;
            }
        }
        return null;
    }

}
