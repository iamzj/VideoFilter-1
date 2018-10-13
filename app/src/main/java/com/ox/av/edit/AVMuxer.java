package com.ox.av.edit;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import com.lyzirving.test.videofilter.util.FileUtil;
import com.lyzirving.test.videofilter.util.TrimType;
import com.ox.av.play.MoviePlayer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Created by winn on 17/9/6.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AVMuxer {
    public static final String TAG = AVMuxer.class.getSimpleName();

    public static void muxing(Context context, String videoFile, String audioFilePath, String dstFile, int rotate) {

        boolean success = true;
        String outputFile;
        try {
            File file = new File(dstFile);
            file.delete();
            file.createNewFile();
            outputFile = file.getAbsolutePath();
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(videoFile);
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFilePath);

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            muxer.setOrientationHint(rotate);

            int trackIndex = MoviePlayer.selectTrack(videoExtractor, "video/");
            MediaFormat videoFormat = videoExtractor.getTrackFormat(trackIndex);
            videoExtractor.selectTrack(trackIndex);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            int videoMaxInputSize = 1024 * 1024;
            try {
                videoMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int videoTrack = muxer.addTrack(videoFormat);

            trackIndex = MoviePlayer.selectTrack(audioExtractor, "audio/");
            MediaFormat audioFormat = audioExtractor.getTrackFormat(trackIndex);
            audioExtractor.selectTrack(trackIndex);
            audioFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            int audioMaxInputSize = 500 * 1024;
            try {
                audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int audioTrack = muxer.addTrack(audioFormat);

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 0;
            ByteBuffer videoBuf = ByteBuffer.allocate(videoMaxInputSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(audioMaxInputSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS = true;
                    videoBufferInfo.size = 0;
                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();//BUFFER_FLAG_KEY_FRAME;
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();
                    frameCount++;
                }
            }

            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            boolean sawEOS2 = false;
            while (!sawEOS2) {

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();
                }
            }
            videoExtractor.release();
            audioExtractor.release();
            muxer.stop();
            muxer.release();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        // 如果合并失败，直接把视频拷贝到目标目录
        if (!success) {
            try {
                FileUtil.copyFile(videoFile, dstFile, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void trimAudioAndMuxing(String videoFile, String audioFilePath, String dstFile, int degrees,
                                          long trimStartMs, long trimEndMs, int trimType) {
        boolean success = true;
        String outputFile;
        long duration = (trimEndMs - trimStartMs) * 1000;
        try {
            File file = new File(dstFile);
            file.delete();
            file.createNewFile();
            outputFile = file.getAbsolutePath();
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(videoFile);
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFilePath);

            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//            muxer.setOrientationHint(degrees);

            int trackIndex = MoviePlayer.selectTrack(videoExtractor, "video/");
            MediaFormat videoFormat = videoExtractor.getTrackFormat(trackIndex);
            videoExtractor.selectTrack(trackIndex);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            int videoMaxInputSize = 1024 * 1024;
            try {
                videoMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int videoTrack = muxer.addTrack(videoFormat);

            trackIndex = MoviePlayer.selectTrack(audioExtractor, "audio/");
            MediaFormat audioFormat = audioExtractor.getTrackFormat(trackIndex);
            audioExtractor.selectTrack(trackIndex);
            audioFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
            int audioMaxInputSize = 500 * 1024;
            try {
                audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int audioTrack = muxer.addTrack(audioFormat);

            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 0;
            ByteBuffer videoBuf = ByteBuffer.allocate(videoMaxInputSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(audioMaxInputSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS = true;
                    videoBufferInfo.size = 0;
                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();//BUFFER_FLAG_KEY_FRAME;
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();
                    frameCount++;
                }
            }

            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            boolean sawEOS2 = false;
            while (!sawEOS2) {

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    long sampleTime = audioExtractor.getSampleTime();
                    if (checkTimeBound(sampleTime, trimType, trimStartMs, trimEndMs)) {
                        //保留两边时，后面部分presentationTimeUs要减去中间删除的时间
                        if (trimType == TrimType.CUT_CENTER && sampleTime > trimEndMs * 1000) {
                            sampleTime = sampleTime - duration;
                        } else if (trimType == TrimType.RETAIN_CENTER) {
                            sampleTime = sampleTime - trimStartMs * 1000;
                        }
                        audioBufferInfo.presentationTimeUs = sampleTime;
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    }
                    audioExtractor.advance();
                }
            }
            videoExtractor.release();
            audioExtractor.release();
            muxer.stop();
            muxer.release();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        // 如果合并失败，直接把视频拷贝到目标目录
        if (!success) {
            try {
                FileUtil.copyFile(videoFile, dstFile, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否在截切范围
     *
     * @param sampleTime 合成的采样时间
     */
    private static boolean checkTimeBound(long sampleTime, int trimType, long startTime, long endTime) {
        sampleTime = sampleTime / 1000;
        switch (trimType) {
            case TrimType.NONE_TRIM:
                return true;
            //保留两边
            case TrimType.CUT_CENTER:
                if (sampleTime < startTime || sampleTime >= endTime) {
                    return true;
                }
                break;
            //保留中间
            case TrimType.RETAIN_CENTER:
                if (sampleTime >= startTime && sampleTime <= endTime) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }
}
