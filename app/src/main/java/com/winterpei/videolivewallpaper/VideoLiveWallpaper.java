package com.winterpei.videolivewallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;


/**
 * @author xingyang.pei
 * @date 2017/9/7.
 */

public class VideoLiveWallpaper extends WallpaperService {

    private static final String TAG = "VideoLiveWallpaper";

    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.winter.livewallpaper";
    private static final String VIDEO_PATH = "videoPath";
    public static final String KEY_ACTION = "action";
    public static final int ACTION_VOICE_SILENCE = 110;
    public static final int ACTION_VOICE_NORMAL = 111;
    public static final int ACTION_VIDEO_PATH = 112;

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    public static void voiceSilence(Context context) {
        final Intent intent = new Intent(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(VideoLiveWallpaper.KEY_ACTION, VideoLiveWallpaper.ACTION_VOICE_SILENCE);
        context.sendBroadcast(intent);
    }

    public static void voiceNormal(Context context) {
        final Intent intent = new Intent(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(VideoLiveWallpaper.KEY_ACTION, VideoLiveWallpaper.ACTION_VOICE_NORMAL);
        context.sendBroadcast(intent);
    }

    public static void setVideoPath(final Context context, String videoPath) {
        LogUtils.show(TAG, videoPath);
        final Intent intent = new Intent(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(VideoLiveWallpaper.KEY_ACTION, VideoLiveWallpaper.ACTION_VIDEO_PATH);
        intent.putExtra(VideoLiveWallpaper.VIDEO_PATH, videoPath);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                context.sendBroadcast(intent);
            }
        }, 1000);
    }

    public static void setVideoToWallpaper(Context context) {
        final Intent wallpaperIntent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        wallpaperIntent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, VideoLiveWallpaper.class));
        context.startActivity(wallpaperIntent);
    }

    class VideoEngine extends Engine {

        private MediaPlayer mMediaPlayer;
        private BroadcastReceiver mVideoBroadcastReceiver;
        private String videoPath;

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                videoPath = (String) msg.obj;
                try {
                    if (!TextUtils.isEmpty(videoPath)) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                        }
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(videoPath);
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.setVolume(0, 0);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void onCreate(final SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter intentFilter = new IntentFilter(VideoLiveWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
            mVideoBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int action = intent.getIntExtra(KEY_ACTION, -1);
                    switch (action) {
                        case ACTION_VOICE_NORMAL:
                            mMediaPlayer.setVolume(1.0f, 1.0f);
                            break;
                        case ACTION_VOICE_SILENCE:
                            mMediaPlayer.setVolume(0, 0);
                            break;
                        case ACTION_VIDEO_PATH:
                            videoPath = intent.getStringExtra(VIDEO_PATH);
                            Message message = Message.obtain();
                            message.obj = videoPath;
                            mHandler.sendMessage(message);
                            break;
                    }
                }
            };
            registerReceiver(mVideoBroadcastReceiver, intentFilter);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            LogUtils.show(TAG, "onSurfaceCreated==" + videoPath);
            if (null == mMediaPlayer) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setSurface(holder.getSurface());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mMediaPlayer.start();
            } else {
                mMediaPlayer.pause();
            }
        }

        @Override
        public void onDestroy() {
            unregisterReceiver(mVideoBroadcastReceiver);
            super.onDestroy();
            mHandler = null;
        }
    }
}
