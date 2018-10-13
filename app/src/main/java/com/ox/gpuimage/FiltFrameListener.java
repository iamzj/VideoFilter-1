package com.ox.gpuimage;

import android.graphics.Bitmap;

public interface FiltFrameListener {
    boolean needCallback();
    void onFiltFrameDraw(Bitmap bitmap);
}
