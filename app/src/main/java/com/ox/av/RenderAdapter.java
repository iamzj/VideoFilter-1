package com.ox.av;

import android.graphics.SurfaceTexture;

public interface RenderAdapter {
    void drawFrame(boolean eosRequested);

    void realse();

    void requestRender();

    SurfaceTexture getSurfaceTexture();
}
