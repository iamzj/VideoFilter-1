package com.ox.gpuimage;

/**
 * 动态滤镜接口
 */
public interface IDynamicFilter {
    /**
     * 开启更新动态滤镜侦
     *
     * @param on
     */
    void setUpdateOn(boolean on);

    /**
     * 是否开启更新动态滤镜侦
     *
     * @return
     */
    boolean isUpdateOn();
}
