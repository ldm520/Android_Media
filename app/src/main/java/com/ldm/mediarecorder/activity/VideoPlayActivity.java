package com.ldm.mediarecorder.activity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ldm.mediarecorder.R;
import com.ldm.mediarecorder.base.BaseActivity;
import com.ldm.mediarecorder.base.Constant;

import java.io.File;

/**
 * @author ldm
 * @description 使用SurfaceView+MediaPlayer播放视频或音频
 * 6.0以上手机也要参考音频播放一样处理权限
 * @time 2017/2/10 10:27
 */
public class VideoPlayActivity extends BaseActivity {
    //功能按钮:播放，暂停，停止，调低声音，调高声音
    private Button btn_play, btn_pause, btn_stop, btn_vol_low, btn_vol_height;
    //播放控件SurfaceView
    private SurfaceView mSurfaceView;
    //播放视频对象
    private MediaPlayer mediaPlayer;
    //系统声音
    private AudioManager audioManager;
    //记录播放位置
    private int position;

    @Override
    protected void setWindowView() {
        //设置窗口无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏显示
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_play);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    protected void initViews() {
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_vol_low = (Button) findViewById(R.id.btn_low);
        btn_vol_height = (Button) findViewById(R.id.btn_hight);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceVIew);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //设置SurfaceHolder类型
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置事件，回调函数
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            //SurfaceView创建时调用
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (position > 0) {
                    playVideo();
                    position = 0;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            // SurfaceView销毁视图
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                //Activity销毁时停止播放，释放资源。否则即使退出，还是能听到视频的声音
                mediaPlayer.release();
            }
        });
    }

    @Override
    protected void initEvents() {
        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_vol_low.setOnClickListener(this);
        btn_vol_height.setOnClickListener(this);
    }


    // 横竖屏切换时的处理
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 如果在播放的时候切换屏幕则保存当前观看的位置
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            outState.putInt(Constant.PLAY_POSITION, mediaPlayer.getCurrentPosition());
        }
        super.onSaveInstanceState(outState);
    }


    // 横竖屏切换后的处理
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(Constant.PLAY_POSITION)) {
            // 取得切换屏幕时保存的位置
            position = savedInstanceState.getInt(Constant.PLAY_POSITION);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * @description 开始播放视频
     * @author ldm
     * @time 2017/2/10 10:33
     */
    private void playVideo() {
        //初始化状态
        mediaPlayer.reset();
        //设置声音流类型
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //设置mp3\mp4加载路径
        try {
            //请事先在SD卡上准备好音频或视频文件，如test.3pg
            File file = new File(Environment.getExternalStorageDirectory(), "test.3gp");
            if (!file.exists()) {
                showToastMsg(getString(R.string.play_fail_for_not_exist));
            }
            mediaPlayer.setDataSource(file.getAbsolutePath());
            // 缓冲
            mediaPlayer.prepare();
            // 开始播放
            mediaPlayer.start();
            // 具体位置
            mediaPlayer.seekTo(position);
            // 视频输出到View
            mediaPlayer.setDisplay(mSurfaceView.getHolder());
            // 重置位置为0
            position = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:// 播放
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    return;
                } else {
                    playVideo();
                }
                break;
            case R.id.btn_pause:// 暂停
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    position = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                } else {
                    return;
                }
                break;
            case R.id.btn_stop:// 停止
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    position = 0;
                } else {
                    return;
                }
                break;
            case R.id.btn_low:// 调小音量
                // 获取当前的音乐音量
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // 音量>0
                if (volume > 0) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                } else {
                    return;
                }
                break;
            case R.id.btn_hight:// 调大音量
                volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // 音量小于最大音量才能调大音量
                if (volume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                } else {
                    return;
                }
                break;
        }
    }
}

