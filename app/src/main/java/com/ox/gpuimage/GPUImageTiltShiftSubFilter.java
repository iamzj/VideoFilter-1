package com.ox.gpuimage;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.opengl.GLES20;

import com.ox.gpuimage.util.FilterTools;

/**
 * 移轴镜滤镜
 */
@SuppressLint("WrongCall")
public class GPUImageTiltShiftSubFilter extends GPUImageTwoInputFilter {

    private static final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate2 = inputTextureCoordinate2.xy;\n" +
            "}";

    /*" varying highp vec2 textureCoordinate;\n" +
            " varying highp vec2 textureCoordinate2;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2; \n" +
            " \n" +
            " uniform lowp float excludeCircleRadius;\n" +
            " uniform lowp vec2 excludeCirclePoint;\n" +
            " uniform lowp float excludeBlurSize;\n" +
            " uniform highp float aspectRatio;\n" +
            " uniform int pressed;\n" +
            "\n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 sharpImageColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     lowp vec4 blurredImageColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
            "     if (pressed == 0) {\n" +
            "       blurredImageColor = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     }\n" +
            "     \n" +
            "     highp vec2 textureCoordinateToUse = vec2(textureCoordinate2.x, (textureCoordinate2.y * aspectRatio + 0.5 - 0.5 * aspectRatio));\n" +
            "     highp float distanceFromCenter = distance(excludeCirclePoint, textureCoordinateToUse);\n" +
            "     lowp float blurIntensity = smoothstep(excludeCircleRadius - excludeBlurSize, excludeCircleRadius, distanceFromCenter);\n" +
            "     if (pressed != 0) {\n" +
            "       blurIntensity *= 0.85;\n" +
            "     }\n" +
            "     \n" +
            "     gl_FragColor = mix(sharpImageColor, blurredImageColor, blurIntensity);\n" +
            " }\n";*/


    private static final String ENCRYPT_SHADER = "KnxreHNjZG0qYmNtYnoqfG9pOCp+b3J+f3hvSWVleG5jZGt+bzEAKnxreHNjZG0qYmNtYnoqfG9pOCp+b3J+f3hvSWVleG5jZGt+bzgxACoAKn9kY2xleGcqeWtnemZveDhOKmNken9+Q2drbW9eb3J+f3hvMQAqf2RjbGV4Zyp5a2d6Zm94OE4qY2R6f35DZ2ttb15vcn5/eG84MSoAKn9kY2xleGcqYmNtYnoqZ2t+OSpna354Y3IxACoAKn9kY2xleGcqYmNtYnoqbGZla34qbGVpf3ldY25+YjEAKn9kY2xleGcqZmV9eip8b2k4KmxlaX95WmVjZH4xACp/ZGNsZXhnKmJjbWJ6KmxmZWt+KmxlaX95TGtmZkVsbFhrfm8xACp/ZGNsZXhnKmNkfip6eG95eW9uMQAqACp8ZWNuKmdrY2QiIwAqcQAqKioqKmZlfXoqfG9pPip5Ymt4ekNna21vSWVmZXgqNyp+b3J+f3hvOE4iY2R6f35DZ2ttb15vcn5/eG8mKn5vcn5/eG9JZWV4bmNka35vIzEAKioqKipmZX16KnxvaT4qaGZ/eHhvbkNna21vSWVmZXgqNyp8b2k+IjskOiYqOyQ6Jio7JDomKjskOiMxACoqKioqY2wqInp4b3l5b24qNzcqOiMqcQAqKioqKioqaGZ/eHhvbkNna21vSWVmZXgqNyp+b3J+f3hvOE4iY2R6f35DZ2ttb15vcn5/eG84Jip+b3J+f3hvSWVleG5jZGt+bzgjMQAqKioqKncAKioqKipiY21ieip8b2k5KmllZXhuY2Rrfm8qNyp8b2k5In5vcn5/eG9JZWV4bmNka35vOCYqOiQ6IyogKmdrfnhjcjEAKioqKipiY21ieip8b2k5KmxlaX95WmVjZH5HKjcqfG9pOSJsZWl/eVplY2R+Jio6JDojKiAqZ2t+eGNyMQAqKioqKgAqKioqKmJjbWJ6KmxmZWt+Kn5lekxlaX95Rm98b2YqNypsZWl/eVplY2R+RyRzKicqbGVpf3ldY25+YiogKjokPzEAKioqKipiY21ieipsZmVrfipoZX5+ZWdMZWl/eUZvfG9mKjcqbGVpf3laZWNkfkckcyohKmxlaX95XWNufmIqICo6JD8xACoqKioqZmV9eipsZmVrfipoZn94Q2R+b2R5Y35zKjcqOyQ6KicqeWdlZX5ieX5veiJ+ZXpMZWl/eUZvfG9mKicqbGVpf3lMa2ZmRWxsWGt+byYqfmV6TGVpf3lGb3xvZiYqaWVleG5jZGt+byRzIzEAKioqKipoZn94Q2R+b2R5Y35zKiE3KnlnZWV+Ynl+b3oiaGV+fmVnTGVpf3lGb3xvZiYqaGV+fmVnTGVpf3lGb3xvZiohKmxlaX95TGtmZkVsbFhrfm8mKmllZXhuY2Rrfm8kcyMxACoqKioqY2wqInp4b3l5b24qKzcqOiMqcQAqKioqKioqaGZ/eENkfm9keWN+cyogNyo6JDIxACoqKioqdwAqKioqKm1mVUx4a21JZWZleCo3KmdjciJ5Ymt4ekNna21vSWVmZXgmKmhmf3h4b25DZ2ttb0llZmV4JipoZn94Q2R+b2R5Y35zIzEAKncA";

    private static final String FRAGMENT_SHADER = FilterTools.getDecryptString(ENCRYPT_SHADER);;

    private float mPointXPixel = -1f;
    private float mPointYPixel = -1f;
    private float mRotationXPixel = -1f;
    private float mRotationYPixel = -1f;

    private float mFocusPointX = 0.5f;
    private float mFocusPointY = 0.5f;
    private float mFocusWidth = 0.2f;
    private float mFocusFallOffRate = 0.1f;

    private int mFocusPointLocation;
    private int mFocusWidthLocation;
    private int mFocusFallOffRateLocation;
    private int mMatrixLocation;
    private int mPressedLocation;

    private float mAngle = 0f;
    private Matrix mMatrix;
    private boolean mPressed = true;

    private boolean mShowTipShadow = false;

    public GPUImageTiltShiftSubFilter(int defaultX, int defaultY) {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        mPointXPixel = defaultX;
        mPointYPixel = defaultY;
    }

    @Override
    public void onInit() {
        super.onInit();
        mFocusPointLocation = GLES20.glGetUniformLocation(getProgram(), "focusPoint");
        mFocusWidthLocation = GLES20.glGetUniformLocation(getProgram(), "focusWidth");
        mFocusFallOffRateLocation = GLES20.glGetUniformLocation(getProgram(), "focusFallOffRate");
        mMatrixLocation = GLES20.glGetUniformLocation(getProgram(), "matrix");
        mPressedLocation = GLES20.glGetUniformLocation(getProgram(), "pressed");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        if (mPointXPixel != -1f && mPointYPixel != -1f) {
            setFocusPoint(mPointXPixel, mPointYPixel);
        } else {
            setFloatVec2(mFocusPointLocation, new float[]{mFocusPointX, mFocusPointY});
        }
        setFocusWidth(mFocusWidth);
        setFoucsFallOffRate(mFocusFallOffRate);
        if (mRotationXPixel != -1f && mRotationYPixel != -1f) {
            mMatrix = new Matrix();
            mMatrix.postRotate(isFlipVertical() ? mAngle : -mAngle, mRotationXPixel / mOutputWidth, isFlipVertical() ? 1 - mRotationYPixel / mOutputHeight : mRotationYPixel / mOutputHeight);
            float[] values = new float[9];
            mMatrix.getValues(values);
            setUniformMatrix3f(mMatrixLocation, values);
        } else {
            if (mMatrix == null) {
                mMatrix = new Matrix();
            }
            float[] values = new float[9];
            mMatrix.getValues(values);
            setUniformMatrix3f(mMatrixLocation, values);
            setPressed(mPressed);
        }
    }

    public void setShowTIpShadow(boolean show) {
        mShowTipShadow = show;
        setInteger(mPressedLocation, mShowTipShadow && mPressed ? 1 : 0);
    }

    public void setFocusPoint(float x, float y) {
        mPointXPixel = x;
        mPointYPixel = y;
        mFocusPointX = x / mOutputWidth;
        mFocusPointY = isFlipVertical() ? 1 - y / mOutputHeight : y / mOutputHeight;
        setFloatVec2(mFocusPointLocation, new float[]{mFocusPointX, mFocusPointY});
    }

    public void setFocusWidth(float width) {
        mFocusWidth = width;
        setFloat(mFocusWidthLocation, mFocusWidth);
    }

    public boolean scaleFocusWidth(float scale) {
        if (mFocusWidth <= 0.02f && scale < 1f) {
            return false;
        } else if (mFocusWidth >= 0.6f && scale > 1f) {
            return false;
        }
        mFocusWidth = mFocusWidth + (scale - 1f) * 0.5f;
        if (mFocusWidth < 0.02f) {
            mFocusWidth = 0.02f;
        } else if (mFocusWidth > 0.6f) {
            mFocusWidth = 0.6f;
        }
        setFocusWidth(mFocusWidth);
        return true;
    }

    public float getFocusWidth() {
        return mFocusWidth;
    }

    public float getPointXPixel() {
        return mPointXPixel;
    }

    public float getPointYPixel() {
        return mPointYPixel;
    }

    public float getFocusPointX() {
        return mFocusPointX;
    }

    public float getFocusPointY() {
        return mFocusPointY;
    }

    public float getAngle() {
        return mAngle;
    }

    public float getRotationXPixel() {
        return mRotationXPixel;
    }

    public float getRotationYPixel() {
        return mRotationYPixel;
    }

    public void setFoucsFallOffRate(float rate) {
        mFocusFallOffRate = rate;
        setFloat(mFocusFallOffRateLocation, mFocusFallOffRate);
    }

    public void setAngle(float value, float x, float y) {
        mRotationXPixel = x;
        mRotationYPixel = y;
        mAngle = (value + mAngle) % 360;
        mMatrix = new Matrix();
        mMatrix.postRotate(isFlipVertical() ? mAngle : -mAngle, x / mOutputWidth, isFlipVertical() ? 1 - y / mOutputHeight : y / mOutputHeight);
        float[] values = new float[9];
        mMatrix.getValues(values);
        setUniformMatrix3f(mMatrixLocation, values);
    }
    
    public void setPressed(boolean value) {
        mPressed = value;
        setInteger(mPressedLocation, mShowTipShadow && mPressed ? 1 : 0);
    }

    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void onFlipVerticalChanged() {
        if (mPointXPixel != -1f && mPointYPixel != -1f) {
            setFocusPoint(mPointXPixel, mPointYPixel);
        }
        if (mRotationXPixel != -1f && mRotationYPixel != -1f) {
            mMatrix = new Matrix();
            mMatrix.postRotate(isFlipVertical() ? mAngle : -mAngle, mRotationXPixel / mOutputWidth, isFlipVertical() ? 1 - mRotationYPixel / mOutputHeight : mRotationYPixel / mOutputHeight);
            float[] values = new float[9];
            mMatrix.getValues(values);
            setUniformMatrix3f(mMatrixLocation, values);
        }
    }
}
