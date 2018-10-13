package com.lyzirving.test.videofilter.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class GlobalConfig {
    /**
     * urls
     */
    public final static String PRIVACY_URL = "https://goo.gl/sS395E";
    public final static String AD_CHOICE = "https://m.facebook.com/ads/audience_network/";
    public final static String GP_LINK_PRE = "https://play.google.com/store/apps/details?id=";

    //video
    public static int videoBitrate = 4000000;
    public static int videoFramerate = 30;
    public static int videoIframeInterval = 1;

    //audio
    public static int audioBitRate = 800000;
    public static int audioSampleRate = 48000;
    public static int audioChannelCount = 2;

    public static final int SUGGEST_VIDEO_WIDTH = 720;
    public static final int SUGGEST_VIDEO_HEIGHT = 1280;
    public static final String SUGGEST_BIT_RATE_IN_STRING = "Auto(Recommended)";
    public static final int SUGGEST_FRAME_RATE = 30;
    public static final int SUGGEST_ORIENTATION= -1;
    public static final int SUGGEST_ORIENTATION_LANDSCAPE= 90;
    public static final int SUGGEST_ORIENTATION_PORTRAIT= 0;
    public static final int INVALID_VIDEO_BITRATE = -1;

    public static final int BIT_RATE_UNIT = 1048576;

    public static final String SUGGESTED_RESOLUTION = "720p";

    public static final int ORIENTATION_LANDSCAPE = 100;
    public static final int ORIENTAION_PORTRAIT = 101;
    private static int sVideoOrientation = ORIENTAION_PORTRAIT;

    public static final int SUGGEST_COUNT_DOWN_TIME = 3;
    public static final int NO_COUNT_DOWN_TIME = 0;

    public static final boolean SUGGEST_SHAKE_TO_STOP = false;

    /**
     * 值为0～100
     */
    public static final float SUGGEST_SHAKE_SENSITIVITY = 50f;

    public static final boolean SUGGEST_HIDE_FLOATING_WINDOW = false;

    public static final boolean SUGGEST_OPEN_WATERMARK = false;


    /**
     * 是否需要录屏倒计时
     */
    public static boolean isNeedCountDownAnim = false;

    private static boolean sHasRecordAudioPermission = false;

    /**
     * GoScreenRecorder
     * 文件储存基本路径
     */
    public static final String BASE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ScreenRecord" ;


    /**
     * 原始视频目录
     */
    public static String getOringinalVideoDirectory() {
        return getDirectory("video");
    }

    /**
     * 已编辑视频目录
     */
    public static String getEditedVideoDirectory() {
        return getDirectory("edited");
    }

    /**
     * 游戏视频目录
     */
    public static String getGameVideoDirectory() {
        return getDirectory("game");
    }

    /**
     * 临时文件目录
     */
    public static String getTempDirectory() {
        return getDirectory("temp");
    }

    /**
     * 截屏目录
     */
    public static String getScreenShotsSaveDirectory() {
       return getDirectory("Screenshots");
    }

    /**
     * 竖屏水印目录
     */
    public static String getPortraitWatermarkDirectory() {
        return getDirectory("watermark/portrait");
    }

    /**
     * 竖屏水印目录
     */
    public static String getPortraitPictureDirectory() {
        return getDirectory("watermark/portrait_picture");
    }

    /**
     * 横屏水印目录
     */
    public static String getLandWatermarkDirectory() {
        return getDirectory("watermark/land");
    }

    /**
     * 横屏水印目录
     */
    public static String getLandPictureDirectory() {
        return getDirectory("watermark/land_picture");
    }

    private static String getDirectory(String parentName){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir =BASE_FILE_PATH + File.separator + parentName + File.separator;

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
            return rootDir;
        } else {
            return null;
        }
    }

    public static boolean hasRecordAudioPermission() {
        return sHasRecordAudioPermission;
    }

    public static void setHasRecordAudioPermission(boolean hasRecordAudioPermission) {
        sHasRecordAudioPermission = hasRecordAudioPermission;
    }

    public static void setVideoOrientation(int orientation) {
        if (orientation != ORIENTATION_LANDSCAPE || orientation != ORIENTAION_PORTRAIT) {
            throw new IllegalArgumentException();
        }
        sVideoOrientation = orientation;
    }

    public static boolean isLandscape() {
        return sVideoOrientation == ORIENTATION_LANDSCAPE;
    }

    /**
     * 获取手机运行内存的大小
     * @param context
     * @return
     */
    public static String getTotalRam(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        return totalRam + "GB";//返回1GB/2GB/3GB/4GB
    }

    /**
     * 获取Sd总存储
     * @return
     */
    public static String getSDTotalSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }

    /**
     * 获得Sd卡剩余容量
     * @return
     */
    public static String getSDAvailableSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }


}
