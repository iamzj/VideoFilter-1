package com.ox.gpuimage;

import android.annotation.SuppressLint;
import android.opengl.GLES20;

import com.ox.gpuimage.util.FilterTools;

/**
 * 高斯模糊（选择区域不模糊）滤镜
 */
@SuppressLint("WrongCall")
public class GPUImageSelectiveBlurFilter extends GPUImageTwoInputFilter {

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
            " uniform highp mat3 matrix;\n" +
            " \n" +
            " uniform highp float focusWidth;\n" +
            " uniform lowp vec2 focusPoint;\n" +
            " uniform highp float focusFallOffRate;\n" +
            " uniform int pressed;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 sharpImageColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     lowp vec4 blurredImageColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
            "     if (pressed == 0) {\n" +
            "       blurredImageColor = texture2D(inputImageTexture2, textureCoordinate2);\n" +
            "     }\n" +
            "     highp vec3 coordinate = vec3(textureCoordinate2, 0.0) * matrix;\n" +
            "     highp vec3 focusPointM = vec3(focusPoint, 0.0) * matrix;\n" +
            "     \n" +
            "     highp float topFocusLevel = focusPointM.y - focusWidth * 0.5;\n" +
            "     highp float bottomFocusLevel = focusPointM.y + focusWidth * 0.5;\n" +
            "     lowp float blurIntensity = 1.0 - smoothstep(topFocusLevel - focusFallOffRate, topFocusLevel, coordinate.y);\n" +
            "     blurIntensity += smoothstep(bottomFocusLevel, bottomFocusLevel + focusFallOffRate, coordinate.y);\n" +
            "     if (pressed != 0) {\n" +
            "       blurIntensity *= 0.85;\n" +
            "     }\n" +
            "     gl_FragColor = mix(sharpImageColor, blurredImageColor, blurIntensity);\n" +
            " }\n";*/

    private static final String ENCRYPT_SHADER = "KnxreHNjZG0qYmNtYnoqfG9pOCp+b3J+f3hvSWVleG5jZGt+bzEAKnxreHNjZG0qYmNtYnoqfG9pOCp+b3J+f3hvSWVleG5jZGt+bzgxACoAKn9kY2xleGcqeWtnemZveDhOKmNken9+Q2drbW9eb3J+f3hvMQAqf2RjbGV4Zyp5a2d6Zm94OE4qY2R6f35DZ2ttb15vcn5/eG84MSoAKgAqf2RjbGV4ZypmZX16KmxmZWt+Km9yaWZ/bm9JY3hpZm9Ya25jf3kxACp/ZGNsZXhnKmZlfXoqfG9pOCpvcmlmf25vSWN4aWZvWmVjZH4xACp/ZGNsZXhnKmZlfXoqbGZla34qb3JpZn9ub0hmf3hZY3BvMQAqf2RjbGV4ZypiY21ieipsZmVrfipreXpvaX5Ya35jZTEAKn9kY2xleGcqY2R+Knp4b3l5b24xAAAqfGVjbipna2NkIiMAKnEAKioqKipmZX16KnxvaT4qeWJreHpDZ2ttb0llZmV4Kjcqfm9yfn94bzhOImNken9+Q2drbW9eb3J+f3hvJip+b3J+f3hvSWVleG5jZGt+byMxACoqKioqZmV9eip8b2k+Kmhmf3h4b25DZ2ttb0llZmV4KjcqfG9pPiI7JDomKjskOiYqOyQ6Jio7JDojMQAqKioqKmNsKiJ6eG95eW9uKjc3KjojKnEAKioqKioqKmhmf3h4b25DZ2ttb0llZmV4Kjcqfm9yfn94bzhOImNken9+Q2drbW9eb3J+f3hvOCYqfm9yfn94b0llZXhuY2Rrfm84IzEAKioqKip3ACoqKioqACoqKioqYmNtYnoqfG9pOCp+b3J+f3hvSWVleG5jZGt+b15lX3lvKjcqfG9pOCJ+b3J+f3hvSWVleG5jZGt+bzgkciYqIn5vcn5/eG9JZWV4bmNka35vOCRzKiAqa3l6b2l+WGt+Y2UqISo6JD8qJyo6JD8qICpreXpvaX5Ya35jZSMjMQAqKioqKmJjbWJ6KmxmZWt+Km5jeX5rZGlvTHhlZ0lvZH5veCo3Km5jeX5rZGlvIm9yaWZ/bm9JY3hpZm9aZWNkfiYqfm9yfn94b0llZXhuY2Rrfm9eZV95byMxACoqKioqZmV9eipsZmVrfipoZn94Q2R+b2R5Y35zKjcqeWdlZX5ieX5veiJvcmlmf25vSWN4aWZvWGtuY395Kicqb3JpZn9ub0hmf3hZY3BvJipvcmlmf25vSWN4aWZvWGtuY395JipuY3l+a2Rpb0x4ZWdJb2R+b3gjMQAqKioqKmNsKiJ6eG95eW9uKis3KjojKnEAKioqKioqKmhmf3hDZH5vZHljfnMqIDcqOiQyPzEAKioqKip3ACoqKioqACoqKioqbWZVTHhrbUllZmV4KjcqZ2NyInlia3h6Q2drbW9JZWZleCYqaGZ/eHhvbkNna21vSWVmZXgmKmhmf3hDZH5vZHljfnMjMQAqdwA=";
    
    private static final String FRAGMENT_SHADER = FilterTools.getDecryptString(ENCRYPT_SHADER);

    
    private float mPointXPixel = -1f;
    private float mPointYPixel = -1f;

    private float mExcludeCircleRadius = 0.5f;
    private float mExcludeCirclePointX = 0.5f;
    private float mExcludeCirclePointY = 0.5f;
    private float mExcludeBlurSize = 0.4f;
    private float mAspectRatio = 1.0f;

    private int mExcludeCircleRadiusLocation;
    private int mExcludeCirclePointLocation;
    private int mExcludeBlurSizeLocation;
    private int mAspectRatioLocation;
    private int mPressedLocation;

    private float mBlurSize = 1.5f;
    private boolean mPressed = true;

    private boolean mShowTipShadow = false;

    public GPUImageSelectiveBlurFilter(int defaultX, int defaultY) {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
        mPointXPixel = defaultX;
        mPointYPixel = defaultY;
    }

    @Override
    public void onInit() {
        super.onInit();
        mExcludeCircleRadiusLocation = GLES20.glGetUniformLocation(getProgram(), "excludeCircleRadius");
        mExcludeCirclePointLocation = GLES20.glGetUniformLocation(getProgram(), "excludeCirclePoint");
        mExcludeBlurSizeLocation = GLES20.glGetUniformLocation(getProgram(), "excludeBlurSize");
        mAspectRatioLocation = GLES20.glGetUniformLocation(getProgram(), "aspectRatio");
        mPressedLocation = GLES20.glGetUniformLocation(getProgram(), "pressed");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setFloat(mExcludeCircleRadiusLocation, mExcludeCircleRadius);
        if (mPointXPixel != -1f && mPointYPixel != -1f) {
            setExcludeCirclePoint(mPointXPixel, mPointYPixel);
        } else {
            setFloatVec2(mExcludeCirclePointLocation, new float[]{mExcludeCirclePointX, mExcludeCirclePointY});
        }
        setFloat(mExcludeBlurSizeLocation, mExcludeBlurSize);
        setFloat(mAspectRatioLocation, mAspectRatio);
        setInteger(mPressedLocation, mShowTipShadow && mPressed ? 1 : 0);
    }

    public void setShowTIpShadow(boolean show) {
        mShowTipShadow = show;
        setInteger(mPressedLocation, mShowTipShadow && mPressed ? 1 : 0);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setAspectRatio((float) height / width);
    }

    public float getExcludeCircleRadius() {
        return mExcludeCircleRadius;
    }

    public void setExcludeCircleRadius(float radius) {
        mExcludeCircleRadius = radius;
        setFloat(mExcludeCircleRadiusLocation, mExcludeCircleRadius);
    }

    public boolean scaleExcludeCircle(float scale) {
        if (mExcludeCircleRadius <= 0.2f && scale < 1f) {
            return false;
        } else if (mExcludeCircleRadius >= 0.8f && scale > 1f) {
            return false;
        }
        mExcludeCircleRadius = mExcludeCircleRadius + (scale - 1f) * 0.4f;
        if (mExcludeCircleRadius < 0.2f) {
            mExcludeCircleRadius = 0.2f;
        } else if (mExcludeCircleRadius > 0.8f) {
            mExcludeCircleRadius = 0.8f;
        }
        setExcludeCircleRadius(mExcludeCircleRadius);
        setExcludeBlurSize(mExcludeCircleRadius * 0.7f);
        return true;
    }

    public float getExcludeCircle() {
        return mExcludeCircleRadius;
    }

    public float getFoucsX() {
        return mPointXPixel;
    }

    public float getFoucsY() {
        return mPointYPixel;
    }

    public void setExcludeCirclePoint(float x, float y) {
        mPointXPixel = x;
        mPointYPixel = y;
        mExcludeCirclePointX = x / mOutputWidth;
        mExcludeCirclePointY = isFlipVertical() ? 1 - y / mOutputHeight : y / mOutputHeight;
        mExcludeCirclePointY += (mExcludeCirclePointY - 0.5f) * (mAspectRatio - 1);
        setFloatVec2(mExcludeCirclePointLocation, new float[]{mExcludeCirclePointX, mExcludeCirclePointY});
    }

    public void setExcludeBlurSize(float size) {
        mExcludeBlurSize = size;
        setFloat(mExcludeBlurSizeLocation, mExcludeBlurSize);
    }

    public float getExcludeBlurSize() {
        return mExcludeBlurSize;
    }

    public void setAspectRatio(float ratio) {
        mAspectRatio = ratio;
        setFloat(mAspectRatioLocation, mAspectRatio);
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }
    
    public void setPressed(boolean value) {
        mPressed = value;
        setInteger(mPressedLocation, mShowTipShadow &&mPressed ? 1 : 0);
    }

    public boolean isPressed() {
        return mPressed;
    }

    @Override
    public void onFlipVerticalChanged() {
        if (mPointXPixel != -1f && mPointYPixel != -1f) {
            setExcludeCirclePoint(mPointXPixel, mPointYPixel);
        }
    }
}
