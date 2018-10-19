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
                mSticker.setTranslationX(mShiftX);
                mSticker.setTranslationY(mShiftY);
                break;
            case MotionEvent.ACTION_UP:
              break;
        }
        return result;
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
