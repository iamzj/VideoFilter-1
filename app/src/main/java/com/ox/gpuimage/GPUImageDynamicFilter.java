package com.ox.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class GPUImageDynamicFilter
        extends GPUImageFilter
        implements IDynamicFilter {
    private static final String SCREEN_BLEND_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n " +
            "varying highp vec2 textureCoordinate2;\n\n " +
            "uniform sampler2D inputImageTexture;\n " +
            "uniform sampler2D inputImageTexture2;\n \n " +
            "void main()\n {\n     " +
            "mediump vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n     " +
            "mediump vec4 textureColor2 = texture2D(inputImageTexture2, textureCoordinate2);\n     " +
            "gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);" +
            "\n }";
    private static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n \n" +
            "void main()\n{\n    " +
            "gl_Position = position;\n    " +
            "textureCoordinate = inputTextureCoordinate.xy;\n    " +
            "textureCoordinate2 = inputTextureCoordinate2.xy;" +
            "\n}";
    private int mFilterInputTextureUniform2;
    private int mFilterSecondTextureCoordinateAttribute;
    private int mFilterSourceTexture2 = -1;
    private boolean mIsUpdate = true;
    private FloatBuffer mTexture2CoordinatesBuffer;
    private long mTimestamp;

    public GPUImageDynamicFilter() {
        super(VERTEX_SHADER, SCREEN_BLEND_FRAGMENT_SHADER);
    }

    /**
     * 设置当前的时间参数，视频gif需要用到
     */
    public void updateTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * 根据时间戳获取当前位图
     *
     * @param timestamp
     * @return
     */
    protected abstract Bitmap getCurrentBitmap(long timestamp);

    public void updateTextureCoord() {
        float[] buffer = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        Matrix matrix = new Matrix();
        if (mFlipHorizontal) {
            matrix.postScale(-1, 1, 0.5f, 0.5f);
        }
        if (mFlipVertical) {
            matrix.postScale(1, -1, 0.5f, 0.5f);
        }
        if (mRotation.asInt() != 0) {
            matrix.postRotate(mRotation.asInt(), 0.5f, 0.5f);
        }
        matrix.mapPoints(buffer, buffer);
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();
        mTexture2CoordinatesBuffer = fBuffer;
    }

    public boolean isUpdateOn() {
        return mIsUpdate;
    }

    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(1, new int[]{mFilterSourceTexture2}, 0);
        mFilterSourceTexture2 = -1;
    }

    public void onDraw(int paramInt, FloatBuffer paramFloatBuffer1, FloatBuffer paramFloatBuffer2) {
        Bitmap bitmap = getCurrentBitmap(mTimestamp);
        if (bitmap != null) {
            mFilterSourceTexture2 = OpenGlUtils.loadTexture(bitmap, mFilterSourceTexture2, mNeedRecycle);
            super.onDraw(paramInt, paramFloatBuffer1, paramFloatBuffer2);
        }
    }

    private boolean mNeedRecycle;

    public void setNeedRecycle(boolean needRecycle) {
        mNeedRecycle = needRecycle;
    }

    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinateAttribute);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFilterSourceTexture2);
        GLES20.glUniform1i(mFilterInputTextureUniform2, 3);
        mTexture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(mFilterSecondTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, mTexture2CoordinatesBuffer);
    }

    public void onInit() {
        super.onInit();
        mFilterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        mFilterInputTextureUniform2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2");
        GLES20.glEnableVertexAttribArray(mFilterSecondTextureCoordinateAttribute);
    }

    public void onOutputSizeChanged(int paramInt1, int paramInt2) {
        super.onOutputSizeChanged(paramInt1, paramInt2);
        updateTextureCoord();
    }

    public void setFlipHorizontal(boolean paramBoolean) {
        boolean bool = mFlipHorizontal;
        super.setFlipHorizontal(paramBoolean);
        if ((paramBoolean ^ bool)) {
            updateTextureCoord();
        }
    }

    public void setFlipVertical(boolean paramBoolean) {
        boolean bool = mFlipVertical;
        super.setFlipVertical(paramBoolean);
        if ((paramBoolean ^ bool)) {
            updateTextureCoord();
        }
    }

    public void setRotation(Rotation paramRotation) {
        if (mRotation != paramRotation) {
            super.setRotation(paramRotation);
            updateTextureCoord();
        }
    }

    public void setRotation(Rotation paramRotation, boolean paramBoolean1, boolean paramBoolean2) {
        if ((mRotation == paramRotation) && (paramBoolean1 == mFlipHorizontal) && (paramBoolean2 == mFlipVertical)) {
            return;
        }
        super.setRotation(paramRotation, paramBoolean1, paramBoolean2);
        updateTextureCoord();
    }

    public void setUpdateOn(boolean paramBoolean) {
        mIsUpdate = paramBoolean;
    }
}
