package com.ijemy.easystreaming;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

import com.qiniu.pili.droid.streaming.AVCodecType;
import com.qiniu.pili.droid.streaming.AudioSourceCallback;
import com.qiniu.pili.droid.streaming.CameraStreamingSetting;
import com.qiniu.pili.droid.streaming.MediaStreamingManager;
import com.qiniu.pili.droid.streaming.StreamStatusCallback;
import com.qiniu.pili.droid.streaming.StreamingProfile;
import com.qiniu.pili.droid.streaming.StreamingSessionListener;
import com.qiniu.pili.droid.streaming.StreamingState;
import com.qiniu.pili.droid.streaming.StreamingStateChangedListener;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;

public class StreamingByCameraActivity extends Activity
        implements StreamingStateChangedListener, StreamStatusCallback, AudioSourceCallback, StreamingSessionListener {

    CameraPreviewFrameView mCameraPreviewSurfaceView;
    private MediaStreamingManager mMediaStreamingManager;
    private StreamingProfile mProfile;
    private String TAG = "StreamingByCameraActivity";
    private String mUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_streaming);
        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        init();
    }

    private void init() {
        mCameraPreviewSurfaceView = findViewById(R.id.cameraPreview_surfaceView);
        try {
            //编码设置 观众看到的视频画面
            //方式1 采用SDK带的默认参数
            mProfile = new StreamingProfile();
            mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH1)//Fps=30, Video Bitrate=1200
                    .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)//Audio Bitrate=48, Audio Sample Rate=44100
                    .setEncodingSizeLevel(StreamingProfile.VIDEO_ENCODING_HEIGHT_480)//16:9的时候是848 x 480
                    .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)//质量优先，实际的码率可能高于设置的码率
                    .setPublishUrl(mUrl);//设置推流地址

            //方式2 自定义编码参数
            /**
            // audio sample rate is 44100, audio bitrate is 48 * 1024 bps
            StreamingProfile.AudioProfile aProfile= new StreamingProfile.AudioProfile(44100, 48 * 1024);
            // fps is 20, video bitrate is 1000 * 1024 bps, maxKeyFrameInterval is 60, profile is HIGH
            StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(20, 1000 * 1024, 60, StreamingProfile.H264Profile.HIGH);
            StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);
            mProfile.setAVProfile(avProfile);
            **/

            //预览设置 主播看到的视频画面
            CameraStreamingSetting camerasetting = new CameraStreamingSetting();
            camerasetting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)//后置摄像头
                    .setContinuousFocusModeEnabled(true)//自动对焦
                    .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)//预览采集大小
                    .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);//预览比例

            //streaming engine init and setListener
            mMediaStreamingManager = new MediaStreamingManager(this, mCameraPreviewSurfaceView, AVCodecType.HW_VIDEO_SURFACE_AS_INPUT_WITH_HW_AUDIO_CODEC);  // soft codec
            mMediaStreamingManager.prepare(camerasetting, mProfile);
            mMediaStreamingManager.setStreamingStateListener(this);
            mMediaStreamingManager.setStreamingSessionListener(this);
            mMediaStreamingManager.setStreamStatusCallback(this);
            mMediaStreamingManager.setAudioSourceCallback(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStateChanged(StreamingState streamingState, Object extra) {

        Log.e(TAG, "streamingState = " + streamingState + "extra = " + extra);
        switch (streamingState) {
            case PREPARING:
                Log.e(TAG, "PREPARING");
                break;
            case READY:
                Log.e(TAG, "READY");
                // start streaming when READY
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mMediaStreamingManager != null) {
                            mMediaStreamingManager.startStreaming();
                        }
                    }
                }).start();
                break;
            case CONNECTING:
                Log.e(TAG, "连接中");
                break;
            case STREAMING:
                Log.e(TAG, "推流中");
                // The av packet had been sent.
                break;
            case SHUTDOWN:
                Log.e(TAG, "直播中断");
                // The streaming had been finished.
                break;
            case IOERROR:
                // Network connect error.
                Log.e(TAG, "网络连接失败");
                break;
            case OPEN_CAMERA_FAIL:
                Log.e(TAG, "摄像头打开失败");
                // Failed to open camera.
                break;
            case DISCONNECTED:
                Log.e(TAG, "已经断开连接");
                // The socket is broken while streaming
                break;
            case TORCH_INFO:
                Log.e(TAG, "开启闪光灯");
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaStreamingManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // You must invoke pause here.
        mMediaStreamingManager.pause();
    }

    @Override
    public void notifyStreamStatusChanged(StreamingProfile.StreamStatus status) {
        Log.e(TAG, "StreamStatus = " + status);
    }

    @Override
    public void onAudioSourceAvailable(ByteBuffer srcBuffer, int size, long tsInNanoTime, boolean isEof) {

    }

    @Override
    public boolean onRecordAudioFailedHandled(int code) {
        Log.i(TAG, "onRecordAudioFailedHandled");
        return false;
    }

    @Override
    public boolean onRestartStreamingHandled(int code) {
        Log.i(TAG, "onRestartStreamingHandled");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mMediaStreamingManager != null) {
                    mMediaStreamingManager.startStreaming();
                }
            }
        }).start();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mMediaStreamingManager.pause();
    }

    @Override
    public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
        return null;
    }

    @Override
    public int onPreviewFpsSelected(List<int[]> list) {
        return -1;
    }

}
