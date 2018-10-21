package com.ox.av.edit;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.lyzirving.test.videofilter.ComponentContext;
import com.lyzirving.test.videofilter.util.FileUtil;
import com.lyzirving.test.videofilter.util.GlobalConfig;
import com.ox.av.AndroidMuxer;
import com.ox.av.EglCore;
import com.ox.av.GlUtil;
import com.ox.av.Muxer;
import com.ox.av.VideoEncoderCore;
import com.ox.av.WindowSurface;
import com.ox.av.play.MoviePlayer;
import com.ox.gpuimage.GPUImageFilter;
import com.ox.gpuimage.GPUImageRenderer;
import com.ox.gpuimage.IRenderCallback;
import com.ox.gpuimage.Rotation;
import com.ox.gpuimage.util.LocationUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 视频添加滤镜
 * <p/>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoFilterDevice {

    private final static String TAG = VideoFilterDevice.class.getSimpleName();

    private File mSrcFile;

    private File mDstFile;
    private File mTmpFile;

    private GPUImageRenderer mRender;

    private FrameInfo mFrameInfo;

    private Object mFrameInfoLock = new Object();

    private int mWidth;
    private int mHeight;
    private int mBitRate;
    private int mDegrees;
    private String[] mLocation;
    private boolean mHasAudio;

    private DecodeTask mDecodeTask;
    private FrameCallback mFrameCallback = new FrameCallback() {
        @Override
        public void preRender(long presentationTimeUsec) {
            pushFrameInfo(new FrameInfo(presentationTimeUsec));
        }

        @Override
        public void postRender() {

        }

        @Override
        public void loopReset() {

        }

        @Override
        public Object getFrameLock() {
            return mFrameInfoLock;
        }
    };
    private RenderThread mRenderThread;
    private PlayerFeedback mPlayerFeedback = new PlayerFeedback() {
        @Override
        public void playbackStopped() {
            mRenderThread.getHandler().setRecordingEnabled(false);
            mRenderThread.getHandler().sendShutdown();
        }
    };
    private ResultCallback mResultCallback;

    private Surface mPlaySurface;

    public VideoFilterDevice(File srcVideoFile, File dstVideoFile, GPUImageFilter filter) throws IOException {
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcVideoFile.getAbsolutePath());
        String degreesString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        int degrees = Integer.valueOf(degreesString);
        String bitrateString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_BITRATE);
        int bitrate = Integer.valueOf(bitrateString);
        String widthString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int width = Integer.valueOf(widthString);
        String heightString = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        int height = Integer.valueOf(heightString);
        String location = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_LOCATION);
        init(srcVideoFile, dstVideoFile, filter, width, height, degrees, bitrate,
                LocationUtil.parseLocation(location),false, null);
    }

    public VideoFilterDevice(File srcVideoFile, File dstVideoFile, GPUImageFilter filter,
                             int width, int height, int degrees, int bitrate, String[] location, boolean resize,
                             ResultCallback callback) throws IOException {
        init(srcVideoFile, dstVideoFile, filter, width, height, degrees, bitrate, location, resize, callback);
    }

    private void init(File srcVideoFile, File dstVideoFile, GPUImageFilter filter,
                      int width, int height, int degrees, int bitrate, String[] location, boolean resize,
                      ResultCallback callback) throws IOException {
        mResultCallback = callback;
        mSrcFile = srcVideoFile;
        mDstFile = dstVideoFile;
        mHasAudio = hasAudio();
        if (mHasAudio) {
            mTmpFile = new File(FileUtil.getExternalCacheDir(ComponentContext.getContext(), ".tmp") + File.separator + System.currentTimeMillis());
        }
        mRenderThread = new RenderThread(this, mHasAudio ? mTmpFile : dstVideoFile);
        mDecodeTask = new DecodeTask(srcVideoFile, mFrameCallback, mPlayerFeedback);
        mDegrees = degrees;
        //mWidth、mHeight表示输出视频的大小
        if (resize) {
            //若要改变输出视频的大小，则使用传入的参数
            mWidth = width;
            mHeight = height;
        } else {
            //若不改变输出视频的大小，则使用源视频真实的大小
            mWidth = mDecodeTask.mVideoWidth;
            mHeight = mDecodeTask.mVideoHeight;
        }
        filter.setRotation(Rotation.fromInt(mDegrees));
        mBitRate = bitrate;
        // 部分视频编辑后会模糊，调高比特率
        if (bitrate < 4 * GlobalConfig.BIT_RATE_UNIT) {
            mBitRate = 4 *  GlobalConfig.BIT_RATE_UNIT;
        }
        mLocation = location;
        mRender = new GPUImageRenderer(filter, new IRenderCallback() {
            @Override
            public void onSurfaceTextureCreated(SurfaceTexture surfaceTexture) {
            }

            @Override
            public void onFrameAvaliable(long frameTimeNanos) {
                mRenderThread.getHandler().sendDoFrame(frameTimeNanos);
            }
        }, false);

    }

    private boolean hasAudio() {
        boolean ret = true;
        // FIXME:是否还有其他更好的方法判断是否有音频
        MediaExtractor extractor = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSrcFile.getAbsolutePath());

            int trackIndex = MoviePlayer.selectTrack(extractor, "audio/");
            MediaFormat mediaFormat = extractor.getTrackFormat(trackIndex);
            extractor.selectTrack(trackIndex);

            mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        } catch (Exception e) {
            ret = false;
        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }
        return ret;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getOutputWidth() {
        return mWidth;
    }

    public int getOutputHeight() {
        return mHeight;
    }

    public int getVideoWidth() {
        return mDecodeTask.mVideoWidth;
    }

    public int getVideoHeight() {
        return mDecodeTask.mVideoHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public String[] getLocaction() {
        return mLocation;
    }

    public int getOrintation() {
        return mDegrees;
    }

    private void onSurfaceCreated(int width, int height) {
        mRender.onSurfaceCreated(null, null);
        mRender.onSurfaceChanged(null, width, height);
        mPlaySurface = new Surface(mRender.getSurfaceTexture());
        mDecodeTask.execute(mPlaySurface);
    }

    private void onSurfaceDestroy() {
        Log.d(TAG, "onSurfaceDestroy");
        if (mPlaySurface != null) {
            mPlaySurface.release();
        }
        mRender.onSurfaceDestroy();
        onSuccess();
    }

    private void onDrawFrame() {
        long time = mFrameInfo == null ? 0 : mFrameInfo.getTimestamp();
        mRender.onDrawFrame(time / 1000);
    }

    private FrameInfo getFrameInfo() {
        synchronized (mFrameInfoLock) {
            return mFrameInfo;
        }
    }

    private void popFrameInfo() {
        synchronized (mFrameInfoLock) {
            mFrameInfo = null;
            mFrameInfoLock.notify();
        }
    }

    private void pushFrameInfo(FrameInfo frameInfo) {
        synchronized (mFrameInfoLock) {
            while (mFrameInfo != null) {
                try {
                    mFrameInfoLock.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "", e);
                    //StatisticsUtils.statisticsMakeVideoError("pushFrameInfo " + e.getMessage(), mWidth, mHeight);
                }
            }
            mFrameInfo = frameInfo;
        }
    }

    public void start() {
        mRenderThread.start();
    }

    private void onError() {
        if (mResultCallback != null) {
            mResultCallback.onError();
        }
    }

    private void onSuccess() {
        if (!mHasAudio) {
            if (mResultCallback != null) {
                mResultCallback.onSuccess(mDstFile);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    AVMuxer.muxing(ComponentContext.getContext(), mTmpFile.getAbsolutePath(), mSrcFile.getAbsolutePath(), mDstFile.getAbsolutePath(), mDegrees);
                    FileUtil.delete(mTmpFile);
                    if (mResultCallback != null) {
                        mResultCallback.onSuccess(mDstFile);
                    }
                }
            }.start();
        }
    }

    public File getmSrcFile() {
        return mSrcFile;
    }

    public interface ResultCallback {
        void onError();

        void onSuccess(File outputFile);
    }

    /**
     * Interface to be implemented by class that manages playback UI.
     * <p/>
     * Callback methods will be invoked on the UI thread.
     */
    public interface PlayerFeedback {
        void playbackStopped();
    }

    /**
     * Callback invoked when rendering video frames.  The MoviePlayer client must
     * provide one of these.
     */
    public interface FrameCallback {
        /**
         * Called immediately before the frame is rendered.
         *
         * @param presentationTimeUsec The desired presentation time, in microseconds.
         */
        void preRender(long presentationTimeUsec);

        /**
         * Called immediately after the frame render call returns.  The frame may not have
         * actually been rendered yet.
         * TODO: is this actually useful?
         */
        void postRender();

        /**
         * Called after the last frame of a looped movie has been rendered.  This allows the
         * callback to adjust its expectations of the next presentation time stamp.
         */
        void loopReset();

        Object getFrameLock();
    }

    private static class FrameInfo {
        private long mTimestamp;

        public FrameInfo(long timestamp) {
            setTimestamp(timestamp);
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            mTimestamp = timestamp;
        }
    }

    /**
     * This class handles all OpenGL rendering.
     * <p/>
     * We use Choreographer to coordinate with the device vsync.  We deliver one frame
     * per vsync.  We can't actually know when the frame we render will be drawn, but at
     * least we get a consistent frame interval.
     * <p/>
     * Start the render thread after the Surface has been created.
     */
    private static class RenderThread extends Thread {
        // Object must be created on render thread to get correct Looper, but is used from
        // UI thread, so we need to declare it volatile to ensure the UI thread sees a fully
        // constructed object.
        private volatile RenderHandler mHandler;

        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

        private EglCore mEglCore;

        // Used for recording.
        private boolean mRecordingEnabled;
        private File mOutputFile;
        private WindowSurface mInputWindowSurface;
        private VideoEncoderCore mVideoEncoder;

        private VideoFilterDevice mDevice;

        /**
         * Pass in the SurfaceView's SurfaceHolder.  Note the Surface may not yet exist.
         */
        public RenderThread(VideoFilterDevice device, File outputFile) {
            mDevice = device;
            mOutputFile = outputFile;
        }

        /**
         * Thread entry point.
         * <p/>
         * The thread should not be started until the Surface associated with the SurfaceHolder
         * has been created.  That way we don't have to wait for a separate "surface created"
         * message to arrive.
         */
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new RenderHandler(this);
            mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
            setRecordingEnabled(true);
            surfaceCreated(mDevice.getOutputWidth(), mDevice.getOutputHeight());
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }

            Looper.loop();

            surfaceDestroy();
            Log.d(TAG, "looper quit");
            releaseGl();
            mEglCore.release();

            synchronized (mStartLock) {
                mReady = false;
            }
        }

        /**
         * Waits until the render thread is ready to receive messages.
         * <p/>
         * Call from the UI thread.
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * Shuts everything down.
         */
        private void shutdown() {
            Log.d(TAG, "shutdown");
            stopEncoder();
            Looper.myLooper().quit();
        }

        /**
         * Returns the render thread's Handler.  This may be called from any thread.
         */
        public RenderHandler getHandler() {
            return mHandler;
        }

        /**
         * Prepares the surface.
         */
        private void surfaceCreated(int width, int height) {
            mDevice.onSurfaceCreated(width, height);
            Log.d(TAG, "surfaceChanged " + width + "x" + height);
        }

        private void surfaceDestroy() {
            mDevice.onSurfaceDestroy();
        }

        /**
         * Releases most of the GL resources we currently hold.
         * <p/>
         * Does not release EglCore.
         */
        private void releaseGl() {
            GlUtil.checkGlError("releaseGl start");

            GlUtil.checkGlError("releaseGl done");

            mEglCore.makeNothingCurrent();
        }

        /**
         * Updates the recording state.  Stops or starts recording as needed.
         */
        private void setRecordingEnabled(boolean enabled) {
            if (enabled == mRecordingEnabled) {
                return;
            }
            if (enabled) {
                startEncoder();
            } else {
                stopEncoder();
            }
            mRecordingEnabled = enabled;
        }

        /**
         * Creates the video encoder object and starts the encoder thread.  Creates an EGL
         * surface for encoder input.
         */
        private void startEncoder() {
            Log.d(TAG, "starting to record");
            VideoEncoderCore encoderCore;
            try {
                Muxer muxer = AndroidMuxer.create(mOutputFile.toString(), Muxer.FORMAT.MPEG4, false);
                muxer.setOrientationHint(mDevice.getOrintation());
                String[] location = mDevice.getLocaction();
                try {
                    if (location != null) {
                        muxer.setLocation(Float.valueOf(location[0]), Float.valueOf(location[1]));
                    }
                } catch (Throwable tr) {
                   // StatisticsUtils.statisticsMakeVideoError("startEncoder1 " + tr.getMessage(), mDevice.mWidth, mDevice.mHeight);
                }
                if (mDevice.getBitRate() == 0) {
                    try {
                        encoderCore = new VideoEncoderCore(mDevice.getOutputWidth(), mDevice.getOutputHeight(),
                                2 * 1000 * 1000, muxer);
                    } catch (Throwable tr) {
                        try {
                            encoderCore = new VideoEncoderCore(mDevice.getOutputWidth(), mDevice.getOutputHeight(),
                                    1 * 1000 * 1000, muxer);
                        } catch (Throwable tr1) {
                            encoderCore = new VideoEncoderCore(mDevice.getOutputWidth(), mDevice.getOutputHeight(),
                                    0, muxer);
                        }
                    }
                } else {
                    encoderCore = new VideoEncoderCore(mDevice.getOutputWidth(), mDevice.getOutputHeight(),
                            mDevice.getBitRate(), muxer);
                }
            } catch (IOException ioe) {
                //StatisticsUtils.statisticsMakeVideoError("startEncoder2 " + ioe.getMessage(), mDevice.mWidth, mDevice.mHeight);
                throw new RuntimeException(ioe);
            }
            mInputWindowSurface = new WindowSurface(mEglCore, encoderCore.getInputSurface());
            mVideoEncoder = encoderCore;
            mInputWindowSurface.makeCurrent();
        }

        /**
         * Stops the video encoder if it's running.
         */
        private void stopEncoder() {
            // 偶现bug，先try住
            try {
                if (mVideoEncoder != null) {
                    Log.d(TAG, "stopping recorder, mVideoEncoder=" + mVideoEncoder);
                    mVideoEncoder.drainEncoder(true);
                    mVideoEncoder.release();
                    mVideoEncoder = null;
                }
                if (mInputWindowSurface != null) {
                    mInputWindowSurface.release();
                    mInputWindowSurface = null;
                }
            } catch (Exception e) {
                if (mDevice != null) {
                    //StatisticsUtils.statisticsMakeVideoError("stopEncoder " + e.getMessage(), mDevice.mWidth, mDevice.mHeight);
                }
            }
        }

        /**
         * Advance state and draw frame in response to a vsync event.
         */
        private void doFrame() {
            try {
                FrameInfo fi = mDevice.getFrameInfo();
                if (fi != null && fi.getTimestamp() > 0) {
                    mInputWindowSurface.makeCurrent();
                    mVideoEncoder.drainEncoder(false, fi.getTimestamp());

                    mDevice.onDrawFrame();
                    mInputWindowSurface.setPresentationTime(fi.getTimestamp() * 1000);
                    mInputWindowSurface.swapBuffers();
                } else {
                    mDevice.onDrawFrame();
                }
                mDevice.popFrameInfo();
            } catch (Exception e) {
                //StatisticsUtils.statisticsMakeVideoError("doFrame " + e.getMessage(), mDevice.mWidth, mDevice.mHeight);
            }
        }
    }

    /**
     * Handler for RenderThread.  Used for messages sent from the UI thread to the render thread.
     * <p/>
     * The object is created on the render thread, and the various "send" methods are called
     * from the UI thread.
     */
    private static class RenderHandler extends Handler {
        private static final int MSG_DO_FRAME = 2;
        private static final int MSG_RECORDING_ENABLED = 3;
        private static final int MSG_SHUTDOWN = 5;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<RenderThread> mWeakRenderThread;

        /**
         * Call from render thread.
         */
        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        /**
         * Sends the "do frame" message, forwarding the Choreographer event.
         * <p/>
         * Call from UI thread.
         */
        public void sendDoFrame(long frameTimeNanos) {
            sendMessage(obtainMessage(RenderHandler.MSG_DO_FRAME,
                    (int) (frameTimeNanos >> 32), (int) frameTimeNanos));
        }

        /**
         * Enable or disable recording.
         * <p/>
         * Call from non-UI thread.
         */
        public void setRecordingEnabled(boolean enabled) {
            sendMessage(obtainMessage(MSG_RECORDING_ENABLED, enabled ? 1 : 0, 0));
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p/>
         * Call from UI thread.
         */
        public void sendShutdown() {
            sendMessage(obtainMessage(RenderHandler.MSG_SHUTDOWN));
        }

        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.d(TAG, "RenderHandler [" + this + "]: what=" + what);

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_DO_FRAME:
                    renderThread.doFrame();
                    break;
                case MSG_RECORDING_ENABLED:
                    renderThread.setRecordingEnabled(msg.arg1 != 0);
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

    /**
     * Thread helper for video playback.
     * <p/>
     * The PlayerFeedback callbacks will execute on the thread that creates the object,
     * assuming that thread has a looper.  Otherwise, they will execute on the main looper.
     */
    private static class DecodeTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;
        private final Object mStopLock = new Object();
        FrameCallback mFrameCallback;
        private PlayerFeedback mFeedback;
        private boolean mStopped = false;
        // Declare this here to reduce allocations.
        private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        // May be set/read by different threads.
        private volatile boolean mIsStopRequested;
        private File mSourceFile;
        private Surface mOutputSurface;
        private boolean mLoop;
        private int mVideoWidth;
        private int mVideoHeight;

        public DecodeTask(File sourceFile, FrameCallback frameCallback, PlayerFeedback playFeedback)
                throws IOException {
            mSourceFile = sourceFile;
            mFrameCallback = frameCallback;
            mFeedback = playFeedback;

            // Pop the file open and pull out the video characteristics.
            // TODO: consider leaving the extractor open.  Should be able to just seek back to
            //       the start after each iteration of play.  Need to rearrange the API a bit --
            //       currently play() is taking an all-in-one open+work+release approach.
            MediaExtractor extractor = null;
            try {
                extractor = new MediaExtractor();
                extractor.setDataSource(sourceFile.toString());
                int trackIndex = selectTrack(extractor, "video/");
                if (trackIndex < 0) {
                    throw new RuntimeException("No video track found in " + mSourceFile);
                }
                extractor.selectTrack(trackIndex);

                MediaFormat format = extractor.getTrackFormat(trackIndex);
                mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
            } finally {
                if (extractor != null) {
                    extractor.release();
                }
            }
        }

        /**
         * Selects the video track, if any.
         *
         * @return the track index, or -1 if no video track is found.
         */
        public static int selectTrack(MediaExtractor extractor, String mineType) {
            // Select the first video track we find, ignore the rest.
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith(mineType)) {
                    {
                        Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                    }
                    return i;
                }
            }

            return -1;
        }

        /**
         * Wait for the player to stop.
         * <p/>
         * Called from any thread other than the PlayTask thread.
         */
        public void waitForStop() {
            synchronized (mStopLock) {
                while (!mStopped) {
                    try {
                        mStopLock.wait();
                    } catch (InterruptedException ie) {
                        // discard
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                play();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                // tell anybody waiting on us that we're done
                synchronized (mStopLock) {
                    mStopped = true;
                    mStopLock.notifyAll();
                }
                mFeedback.playbackStopped();
            }
        }

        /**
         * Decodes the video stream, sending frames to the surface.
         * <p/>
         * Does not return until video playback is complete, or we get a "stop" signal from
         * frameCallback.
         */
        public void play() throws IOException {
            MediaExtractor extractor = null;
            MediaCodec decoder = null;

            // The MediaExtractor error messages aren't very useful.  Check to see if the input
            // file exists so we can throw a better one if it's not there.
            if (!mSourceFile.canRead()) {
                throw new FileNotFoundException("Unable to read " + mSourceFile);
            }

            try {
                extractor = new MediaExtractor();
                extractor.setDataSource(mSourceFile.toString());
                int trackIndex = selectTrack(extractor, "video/");
                if (trackIndex < 0) {
                    throw new RuntimeException("No video track found in " + mSourceFile);
                }
                extractor.selectTrack(trackIndex);

                MediaFormat format = extractor.getTrackFormat(trackIndex);

                // Create a MediaCodec decoder, and configure it with the MediaFormat from the
                // extractor.  It's very important to use the format from the extractor because
                // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
                String mime = format.getString(MediaFormat.KEY_MIME);
                decoder = MediaCodec.createDecoderByType(mime);
                decoder.configure(format, mOutputSurface, null, 0);
                decoder.start();

                doExtract(extractor, trackIndex, decoder, mFrameCallback);
            } catch (Exception e) {
              // 异步原因可能会导致的 java.lang.IllegalStateException crash
                //StatisticsUtils.statisticsMakeVideoError("play " + e.getMessage(), mVideoWidth, mVideoHeight);
            } finally {
                // release everything we grabbed
                try {
                    if (decoder != null) {
                        decoder.stop();
                        decoder.release();
                        decoder = null;
                    }
                    if (extractor != null) {
                        extractor.release();
                        extractor = null;
                    }
                } catch (Exception e) {
                }
            }
        }

        /**
         * Work loop.  We execute here until we run out of video or are told to stop.
         */
        private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                               FrameCallback frameCallback) {
            // We need to strike a balance between providing input and reading output that
            // operates efficiently without delays on the output side.
            //
            // To avoid delays on the output side, we need to keep the codec's input buffers
            // fed.  There can be significant latency between submitting frame N to the decoder
            // and receiving frame N on the output, so we need to stay ahead of the game.
            //
            // Many video decoders seem to want several frames of video before they start
            // producing output -- one implementation wanted four before it appeared to
            // configure itself.  We need to provide a bunch of input frames up front, and try
            // to keep the queue full as we go.
            //
            // (Note it's possible for the encoded data to be written to the stream out of order,
            // so we can't generally submit a single frame and wait for it to appear.)
            //
            // We can't just fixate on the input side though.  If we spend too much time trying
            // to stuff the input, we might miss a presentation deadline.  At 60Hz we have 16.7ms
            // between frames, so sleeping for 10ms would eat up a significant fraction of the
            // time allowed.  (Most video is at 30Hz or less, so for most content we'll have
            // significantly longer.)  Waiting for output is okay, but sleeping on availability
            // of input buffers is unwise if we need to be providing output on a regular schedule.
            //
            //
            // In some situations, startup latency may be a concern.  To minimize startup time,
            // we'd want to stuff the input full as quickly as possible.  This turns out to be
            // somewhat complicated, as the codec may still be starting up and will refuse to
            // accept input.  Removing the timeout from dequeueInputBuffer() results in spinning
            // on the CPU.
            //
            // If you have tight startup latency requirements, it would probably be best to
            // "prime the pump" with a sequence of frames that aren't actually shown (e.g.
            // grab the first 10 NAL units and shove them through, then rewind to the start of
            // the first key frame).
            //
            // The actual latency seems to depend on strongly on the nature of the video (e.g.
            // resolution).
            //
            //
            // One conceptually nice approach is to loop on the input side to ensure that the codec
            // always has all the input it can handle.  After submitting a buffer, we immediately
            // check to see if it will accept another.  We can use a short timeout so we don't
            // miss a presentation deadline.  On the output side we only check once, with a longer
            // timeout, then return to the outer loop to see if the codec is hungry for more input.
            //
            // In practice, every call to check for available buffers involves a lot of message-
            // passing between threads and processes.  Setting a very brief timeout doesn't
            // exactly work because the overhead required to determine that no buffer is available
            // is substantial.  On one device, the "clever" approach caused significantly greater
            // and more highly variable startup latency.
            //
            // The code below takes a very simple-minded approach that works, but carries a risk
            // of occasionally running out of output.  A more sophisticated approach might
            // detect an output timeout and use that as a signal to try to enqueue several input
            // buffers on the next iteration.
            //
            // If you want to experiment, set the VERBOSE flag to true and watch the behavior
            // in logcat.  Use "logcat -v threadtime" to see sub-second timing.

            final int TIMEOUT_USEC = 10000;
            ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
            int inputChunk = 0;
            long firstInputTimeNsec = -1;

            boolean outputDone = false;
            boolean inputDone = false;
            while (!outputDone) {
                /**
                 * 用户点击取消保存
                 */
                /*if (!VideoEditActivity.mIsSaving) {
                    DLog.e("save", "saveCanceled");
                    break;
                }*/
                Log.d(TAG, "loop");
                if (mIsStopRequested) {
                    Log.d(TAG, "Stop requested");
                    return;
                }

                // Feed more data to the decoder.
                if (!inputDone) {
                    int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        if (firstInputTimeNsec == -1) {
                            firstInputTimeNsec = System.nanoTime();
                        }
                        ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                        // Read the sample data into the ByteBuffer.  This neither respects nor
                        // updates inputBuf's position, limit, etc.
                        int chunkSize = extractor.readSampleData(inputBuf, 0);
                        if (chunkSize < 0) {
                            // End of stream -- send empty frame with EOS flag set.
                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                            Log.d(TAG, "sent input EOS");
                        } else {
                            if (extractor.getSampleTrackIndex() != trackIndex) {
                                Log.w(TAG, "WEIRD: got sample from track " +
                                        extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                            }
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                    presentationTimeUs, 0 /*flags*/);
                            {
                                Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                        chunkSize);
                            }
                            inputChunk++;
                            extractor.advance();
                        }
                    } else {
                        Log.d(TAG, "input buffer not available");
                    }
                }

                if (!outputDone) {
                    int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        Log.d(TAG, "decoder output buffers changed");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = decoder.getOutputFormat();
                        Log.d(TAG, "decoder output format changed: " + newFormat);
                    } else if (decoderStatus < 0) {
                        throw new RuntimeException(
                                "unexpected result from decoder.dequeueOutputBuffer: " +
                                        decoderStatus);
                    } else { // decoderStatus >= 0
                        if (firstInputTimeNsec != 0) {
                            // Log the delay from the first buffer of input to the first buffer
                            // of output.
                            long nowNsec = System.nanoTime();
                            Log.d(TAG, "startup lag " + ((nowNsec - firstInputTimeNsec) / 1000000.0) + " ms");
                            firstInputTimeNsec = 0;
                        }
                        boolean doLoop = false;
                        Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                                " (size=" + mBufferInfo.size + ")");
                        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "output EOS");
                            if (mLoop) {
                                doLoop = true;
                            } else {
                                outputDone = true;
                            }
                        }

                        boolean doRender = (mBufferInfo.size != 0);
                        if (doRender && frameCallback != null) {
                            synchronized (frameCallback.getFrameLock()) {
                                // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                                // to SurfaceTexture to convert to a texture.  We can't control when it
                                // appears on-screen, but we can manage the pace at which we release
                                // the buffers.
                                frameCallback.preRender(mBufferInfo.presentationTimeUs);
                                decoder.releaseOutputBuffer(decoderStatus, doRender);
                                frameCallback.postRender();
                            }
                        } else {
                            decoder.releaseOutputBuffer(decoderStatus, doRender);
                        }

                        if (doLoop) {
                            Log.d(TAG, "Reached EOS, looping");
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                            inputDone = false;
                            decoder.flush();    // reset decoder state
                            frameCallback.loopReset();
                        }
                    }
                }
            }
        }

        /**
         * Creates a new thread, and starts execution of the player.
         */
        public void execute(Surface outputSurface) {
            mOutputSurface = outputSurface;
            new Thread(this, "Movie Player").start();
        }
    }
}
