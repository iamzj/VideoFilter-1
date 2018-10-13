package com.ox.videolib;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by winn on 17/8/31.
 */

public class CombineBean {

    public static final int TYPE_IMG = 1;
    public static final int TYPE_VIDEO = 2;

    public int type = TYPE_VIDEO;
    /** 文件路径 **/
    public String file;

    public int srcWidth;
    public int srcHeight;
    /** 原视频播放时长，单位ms **/
    public int srcDuring;
    /** 是否静音播放 **/
    public boolean slient;

    /** 合成后坐标 **/
    public Point pos;
    /** 缩放后大小（裁剪前） **/
    public float scale;
    /** 裁剪区域（缩放后的区域） **/
    public Rect clipRect;

    public boolean hasAudio;

    /**
     * 延时播放时间
     */
    public int startOffset;
}
