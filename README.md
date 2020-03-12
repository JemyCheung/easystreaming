# easystreaming
用于快速集成七牛推流SDK
[开发文档及API](https://developer.qiniu.com/pili/sdk/3719/PLDroidMediaStreaming-function-using)
[七牛官方Demo](https://github.com/pili-engineering/PLDroidMediaStreaming)

## Application初始化
```
StreamingEnv.init(getApplicationContext());
```
## 参数初始化
```
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
```

## 开始推流
prepare之后会在onStateChanged回调SDK状态，在READY时调用mMediaStreamingManager.startStreaming()即可
```
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
```

