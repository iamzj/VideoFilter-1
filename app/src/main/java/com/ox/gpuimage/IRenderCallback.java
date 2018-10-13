package com.ox.gpuimage;

import android.graphics.SurfaceTexture;

/**
 * Render回调接口
 */
public interface IRenderCallback {
    void onSurfaceTextureCreated(SurfaceTexture surfaceTexture);
    void onFrameAvaliable(long frameTimeNanos);
}
