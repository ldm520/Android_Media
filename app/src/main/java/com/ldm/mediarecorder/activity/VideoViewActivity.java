package com.ldm.mediarecorder.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.ldm.mediarecorder.R;
import com.ldm.mediarecorder.base.BaseActivity;

/**
 * @author ldm
 * @description 使用VideoViewActivity播放视频
 * 6.0以上手机也要参考音频播放一样处理权限
 * @time 2017/2/10 10:27
 */
public class VideoViewActivity extends BaseActivity {
    private VideoView videoview;
    //视频地址，可以是网络视频，也可以是本地视频,当然不能为空
    private final String video_url = "";
    private WifiManager.WifiLock mWifiLock;
    private int mCurrentPosition;

    @Override
    protected void setWindowView() {
        setContentView(R.layout.activity_videoview);
        //6.0以上系统手机，调用下面代码需要动态获取权限
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL,
                "wifilock");
        mWifiLock.acquire();
    }

    @Override
    protected void initViews() {
        videoview = (VideoView) findViewById(R.id.videoview);
        videoview.setKeepScreenOn(true);
        //系统视频播放控制器，通常项目中是自定义播放控件器UI
        MediaController mc = new MediaController(this);
        //隐藏VideoView自带的进度条
        mc.setVisibility(View.INVISIBLE);
        videoview.setMediaController(mc);
        //播放本地视频
        //video.setVideoPath(VideoPath);
        //播放网络视频
        videoview.setVideoURI(Uri.parse(video_url));
        videoview.requestFocus();
    }

    @Override
    protected void initEvents() {
        //播放前缓冲监听事件，比如加载视频时有加载提示框
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //准备完成，比如可以隐藏加载框
            }
        });
        //播放完成监听
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //提示用户播放结束
            }
        });
        //播放发生错误监听
        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                //错误处理
                return true;
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onStart() {
        //开始播放
        videoview.start();
        if (mCurrentPosition > 0) {
            videoview.seekTo(mCurrentPosition);
        }
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        //保存播放位置
        mCurrentPosition = videoview.getCurrentPosition();

    }

    @Override
    protected void onStop() {
        super.onStop();
        videoview.pause();
    }

    @Override
    protected void onDestroy() {
        try {
            videoview.stopPlayback();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mWifiLock.isHeld())
            mWifiLock.release();
        super.onDestroy();
    }
}

