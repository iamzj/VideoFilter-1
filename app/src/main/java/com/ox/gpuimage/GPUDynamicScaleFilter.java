package com.ox.gpuimage;

import android.opengl.GLES20;

import com.ox.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author lyzirving
 *         time        2018/10/22
 *         email       lyzirving@sina.com
 *         information
 */

public class GPUDynamicScaleFilter extends GPUImageFilter {
    private static final float INVALID_RATIO = -1;

    private FloatBuffer mScaledTextureBuffer;
    private float[] mScaleTextureCoord;
    private int mTimeCount;

    private int mFrameRate;
    //变化开始的时间，单位秒
    private int mStartTime;
    //变化持续的时间，单位秒
    private int mDuration;
    private float mRatio;
    private float mDRatio;
    private boolean mIsEnlarge;

    private boolean mIsRecover;

    public GPUDynamicScaleFilter() {
        super();
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        float[] flipTexture = TextureRotationUtil.getRotation(Rotation.NORMAL, false, true);
        mScaledTextureBuffer = ByteBuffer.allocateDirect(flipTexture.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mScaledTextureBuffer.put(flipTexture).position(0);
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        updateScaleTextureCoord();
        mTimeCount++;
        mScaledTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                mScaledTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
    }

    private void updateScaleTextureCoord() {
        if (mTimeCount >= mStartTime * mFrameRate && mTimeCount <= (mStartTime + mDuration) * mFrameRate) {
            float curRatio = mIsEnlarge ? 1 + (mTimeCount - mStartTime * mFrameRate) * mDRatio :
                    1 - (mTimeCount - mStartTime * mFrameRate) * mDRatio;
            if (mIsEnlarge && curRatio > mRatio) {
                curRatio = mRatio;
            } else if (!mIsEnlarge && curRatio < mRatio) {
                curRatio = mRatio;
            }
            float[] texture = new float[8];
            texture[0] = (1 - 1 / curRatio) / 2;
            texture[1] = (1 - 1 / curRatio) / 2 + 1 / curRatio;
            texture[2] = 1 - (1 - 1 / curRatio) / 2;
            texture[3] = (1 - 1 / curRatio) / 2 + 1 / curRatio;
            texture[4] = (1 - 1 / curRatio) / 2;
            texture[5] = (1 - 1 / curRatio) / 2;
            texture[6] = 1 - (1 - 1 / curRatio) / 2;
            texture[7] = (1 - 1 / curRatio) / 2;
            mScaleTextureCoord = TextureRotationUtil.flipVerticle(texture);
            mScaledTextureBuffer.put(mScaleTextureCoord);
        }
    }

    public void setScaleInfo(int frameRate, int startTime, int duration, float ratio) {
        mFrameRate = frameRate;
        mStartTime = startTime;
        mDuration = duration;
        mRatio = ratio;
        if (mRatio > 1) {
            mIsEnlarge = true;
            mDRatio = (mRatio - 1) * 1f / (mDuration * mFrameRate);
        } else {
            mDRatio =  (1 - mRatio) * 1f / (mDuration * mFrameRate);
        }
    }

}
