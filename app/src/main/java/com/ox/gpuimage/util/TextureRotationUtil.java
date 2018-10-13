/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ox.gpuimage.util;

import com.ox.gpuimage.Rotation;

public class TextureRotationUtil {

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_90[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    public static final float TEXTURE_ROTATED_270[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private TextureRotationUtil() {
    }

    public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                      final boolean flipVertical) {
        float[] rotatedTex;
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case ROTATION_180:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case ROTATION_270:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            case NORMAL:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        return rotatedTex;
    }

    /*public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                      final boolean flipVertical, CameraController.Size size, int topMaskH) {
        float[] rotatedTex;
        float top = (float) topMaskH / size.getHeight();
        float bottom = 1 - (float) (size.getHeight() - topMaskH - size.getWidth()) / size.getHeight();
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TEXTURE_ROTATED_90;
                rotatedTex = new float[] {
                        1 - top, rotatedTex[1],
                        1 - top, rotatedTex[3],
                        1 - bottom, rotatedTex[5],
                        1 - bottom, rotatedTex[7],
                };
                break;
            case ROTATION_180:
                rotatedTex = TEXTURE_ROTATED_180;
                rotatedTex = new float[] {
                        1 - top, rotatedTex[1],
                        1 - bottom, rotatedTex[3],
                        1 - top, rotatedTex[5],
                        1 - bottom, rotatedTex[7],
                };
                break;
            case ROTATION_270:
                rotatedTex = TEXTURE_ROTATED_270;
                rotatedTex = new float[] {
                        1 - bottom, rotatedTex[1],
                        1 - bottom, rotatedTex[3],
                        1 - top, rotatedTex[5],
                        1 - top, rotatedTex[7],
                };
                break;
            case NORMAL:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                rotatedTex = new float[] {
                        top, rotatedTex[1],
                        bottom, rotatedTex[3],
                        top, rotatedTex[5],
                        bottom, rotatedTex[7],
                };
                break;
        }

        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        return rotatedTex;
    }*/

    public static float[] flipVerticle(float[] src) {
        float[] result = new float[]{
                src[0], flip(src[1]),
                src[2], flip(src[3]),
                src[4], flip(src[5]),
                src[6], flip(src[7]),
        };
        return result;
    }

    private static float flip(final float i) {
        return 1.0f - i;
//        if (i == 0.0f) {
//            return 1.0f;
//        }
//        return 0.0f;
    }
}
