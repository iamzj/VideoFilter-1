package com.ox.imageutil;

public class NativeImageUtil {
    static {
        System.loadLibrary("imageutil");
    }

    public static native void YUVtoRBGA(byte[] yuv, int width, int height, int[] out);

    public static native void YUVtoARBG(byte[] yuv, int width, int height, int[] out);
    
    public static native String getOxString();
}
