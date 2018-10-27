package com.ox.gpuimage;

import android.opengl.GLES20;
import android.util.Log;

import com.ox.gpuimage.util.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.ox.gpuimage.GPUImageRenderer.CUBE;

/**
 * @author lyzirving
 *         time        2018/10/22
 *         email       lyzirving@sina.com
 *         information
 */

public class GPUDynamicScaleFilter extends GPUImageFilter {
    public static final float NONE_RATIO = 1;
    private FloatBuffer mScaledVertexBuffer;
    private float[] mScaledVetex;
    private int mTimeCount;

    private int mFrameRate;
    //变化开始的时间，单位秒
    private int mStartTime;
    //变化持续的时间，单位秒
    private int mDuration;
    private float mDstRatio;
    private float mLastRatio = NONE_RATIO;
    private float mCurRatio = NONE_RATIO;
    private float mDRatio;
    private boolean mIsEnlarge;

    public GPUDynamicScaleFilter() {
        super();
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        mScaledVetex = new float[8];
        mScaledVertexBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mScaledVertexBuffer.put(CUBE).position(0);
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        updateScaleVertex();
        mTimeCount++;
        mScaledVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mScaledVertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
    }

    private void updateScaleVertex() {
        if (mTimeCount >= mStartTime * mFrameRate && mTimeCount <= (mStartTime + mDuration) * mFrameRate) {
            mCurRatio = mIsEnlarge ? mLastRatio + (mTimeCount - mStartTime * mFrameRate) * mDRatio :
                    mLastRatio - (mTimeCount - mStartTime * mFrameRate) * mDRatio;
            if (mIsEnlarge && mCurRatio > mDstRatio) {
                mCurRatio = mDstRatio;
            } else if (!mIsEnlarge && mCurRatio < mDstRatio) {
                mCurRatio = mDstRatio;
            }
            float[] scaleVertex = new float[8];
            scaleVertex[0] = -mCurRatio;
            scaleVertex[1] = -mCurRatio;
            scaleVertex[2] = mCurRatio;
            scaleVertex[3] = -mCurRatio;
            scaleVertex[4] = -mCurRatio;
            scaleVertex[5] = mCurRatio;
            scaleVertex[6] = mCurRatio;
            scaleVertex[7] = mCurRatio;
            mScaledVertexBuffer.put(scaleVertex);
            Log.d("test", "current ratio = " + mCurRatio + " ,dst ratio = " + mDstRatio + " ,last ratio = " + mLastRatio);
        }

    }

    public void setScaleInfo(int frameRate, int startTime, int duration, float ratio) {
        mFrameRate = frameRate;
        mStartTime = startTime;
        mDuration = duration;
        mDstRatio = mLastRatio * ratio;
        if (mDstRatio - mLastRatio > 0) {
            mIsEnlarge = true;
            mDRatio = (mDstRatio - mLastRatio) * 1f / (mDuration * mFrameRate);
        } else if (mDstRatio - mLastRatio < 0){
            mDRatio =  (mLastRatio - mDstRatio) * 1f / (mDuration * mFrameRate);
        } else if (mDstRatio == mLastRatio) {

        }
    }

    public void setLastRatio(float lastRatio) {
        mLastRatio = lastRatio;
    }

    public float getDstRatio() {
        return mDstRatio;
    }

}
