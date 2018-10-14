package com.ox.gpuimage;

import android.graphics.Bitmap;

/**
 * @author lyzirving
 *         time          2018/10/14
 *         email         lyzirving@sina.com
 *         information
 */

public class GPUImageStickerFilterGroup extends GPUImageFilterGroup {

    private GPUImageStickerFilter mGPUImageStickerFilter;
    private GPUImageFilter mGPUImageFilter;

    public GPUImageStickerFilterGroup(boolean isLastFilter) {
        mGPUImageStickerFilter = new GPUImageStickerFilter();
        addFilter(mGPUImageStickerFilter);
        if (isLastFilter) {
            mGPUImageFilter = new GPUImageFilter();
            addFilter(mGPUImageFilter);
        }
    }

    public void setImage(Bitmap bitmap) {
        mGPUImageStickerFilter.setSrcBitmap(bitmap);
    }

}
