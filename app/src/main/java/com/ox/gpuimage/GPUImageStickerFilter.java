package com.ox.gpuimage;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author lyzirving
 *         time          2018/10/14
 *         email         lyzirving@sina.com
 *         information
 */

public class GPUImageStickerFilter extends GPUImageFilter {

    private Bitmap mSrcBitmap;
    private int mBitmapTextureId = OpenGlUtils.NO_TEXTURE;
    private float[] mBitmapVertexCoord;
    private FloatBuffer mBitmapVertexBuffer;

    private boolean mIsDrawBackground;

    public GPUImageStickerFilter() {
        super();
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        if (mSrcBitmap == null || mSrcBitmap.isRecycled()) {
            throw new RuntimeException("source bitmap invalid");
        }
        if (mBitmapTextureId == OpenGlUtils.NO_TEXTURE) {
            mBitmapTextureId = OpenGlUtils.loadTexture(mSrcBitmap, mBitmapTextureId, true);
        }
        mBitmapVertexCoord = new float[]{
                -0.5f, -0.5f,
                0.5f, -0.5f,
                -0.5f, 0.5f,
                0.5f, 0.5f,
        };
        mBitmapVertexBuffer = ByteBuffer.allocateDirect(mBitmapVertexCoord.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mBitmapVertexBuffer.put(mBitmapVertexCoord).position(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBitmapTextureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glDeleteTextures(1, new int[]{mBitmapTextureId}, 0);
            mBitmapTextureId = OpenGlUtils.NO_TEXTURE;
        }
        if (mSrcBitmap != null) {
            if (!mSrcBitmap.isRecycled()) {
                mSrcBitmap.recycle();
            }
            mSrcBitmap = null;
        }
    }

    public void onDraw(boolean drawBackground, int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        mIsDrawBackground = drawBackground;
        super.onDraw(textureId, cubeBuffer, textureBuffer);
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        if (mIsDrawBackground) {
            return;
        }
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mBitmapVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mBitmapVertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapTextureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);
    }

    @Override
    protected void onDrawArraysPost() {
        super.onDrawArraysPost();
        if (mIsDrawBackground) {
            return;
        }
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public void setSrcBitmap(Bitmap bitmap) {
        mSrcBitmap = bitmap;
    }

}
