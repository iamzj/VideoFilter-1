package com.ox.gpuimage;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author lyzirving
 *         time        2018/10/9
 *         email       lyzirving@sina.com
 *         information
 */

public class GPUImageScaleFilter extends GPUImageFilter {

    private FloatBuffer mGLScaleVertexCoordBuffer;
    private float[] mScaledVertexCoord = new float[8];

    private ResizeType.Type mResizeType = ResizeType.Type.TYPE_1_1;
    private float mVideoWidth;
    private float mVideoHeight;

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        mGLScaleVertexCoordBuffer.put(mScaledVertexCoord).position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLScaleVertexCoordBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        getResizedVertexCoord();
        mGLScaleVertexCoordBuffer = ByteBuffer.allocateDirect(mScaledVertexCoord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLScaleVertexCoordBuffer.put(mScaledVertexCoord).position(0);
    }

    private void getResizedVertexCoord() {
        float screenRatio = -1f;
        float ratio = -1f;
        switch (mResizeType) {
            case TYPE_1_1:
                screenRatio = 1f;
                break;
            case TYPE_4_5:
                screenRatio = 4f / 5;
                break;
            case TYPE_16_9:
                screenRatio = 16f / 9;
                break;
            case TYPE_9_16:
                screenRatio = 9f / 16;
                break;
        }
        if (mVideoWidth >= mVideoHeight) {
            ratio = mVideoWidth * 1f / mVideoHeight;
            mScaledVertexCoord[0] = -ratio / screenRatio;
            mScaledVertexCoord[1] = -1;
            mScaledVertexCoord[2] = ratio / screenRatio;
            mScaledVertexCoord[3] = -1;
            mScaledVertexCoord[4] = -ratio / screenRatio;
            mScaledVertexCoord[5] = 1;
            mScaledVertexCoord[6] = ratio / screenRatio;
            mScaledVertexCoord[7] = 1;
        } else {
            ratio = mVideoHeight * 1f / mVideoWidth;
            mScaledVertexCoord[0] = -ratio;
            mScaledVertexCoord[1] = -screenRatio;
            mScaledVertexCoord[2] = ratio;
            mScaledVertexCoord[3] = -screenRatio;
            mScaledVertexCoord[4] = -ratio;
            mScaledVertexCoord[5] = screenRatio;
            mScaledVertexCoord[6] = ratio;
            mScaledVertexCoord[7] = screenRatio;
        }
    }

    public void setResize(ResizeType.Type type, int videoWidth, int videoHeight) {
        mResizeType = type;
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    public static class ResizeType {
        public static final int VALUE_1_1 = 1;
        public static final int VALUE_4_5 = 2;
        public static final int VALUE_16_9 = 3;
        public static final int VALUE_9_16 = 4;

        public enum Type {
            TYPE_ORIGINAL, TYPE_1_1, TYPE_4_5, TYPE_16_9, TYPE_9_16;
        }
    }

}
