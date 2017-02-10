package com.ldm.mediarecorder.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.ldm.mediarecorder.R;
import com.ldm.mediarecorder.adapter.AudioAdapter;
import com.ldm.mediarecorder.base.BaseActivity;
import com.ldm.mediarecorder.base.Constant;
import com.ldm.mediarecorder.model.FileBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ldm
 * @description 通过AudioRecord录制声音（基于字节流录音，使用相对灵活）并播放
 * @time 2017/2/9 9:03
 */
public class AudioRecordActivity extends BaseActivity {
    private Button start_tv;
    private ListView listView;
    //线程操作
    private ExecutorService mExecutorService;
    //当前是否正在录音
    private volatile boolean isRecording;
    //录音开始时间与结束时间
    private long startTime, endTime;
    //录音所保存的文件
    private File mAudioFile;
    //文件列表数据
    private List<FileBean> dataList;
    private AudioAdapter mAudioAdapter;
    private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";
    private static final int BUFFER_SIZE = 2048;
    private byte[] mBuffer;
    private FileOutputStream mFileOutPutStream;
    //文件流录音API
    private AudioRecord mAudioRecord;
    private volatile boolean isPlaying;

    //更新UI线程的Handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.RECORD_SUCCESS:
                    //录音成功，展示数据
                    if (null == mAudioAdapter) {
                        mAudioAdapter = new AudioAdapter(AudioRecordActivity.this, dataList, R.layout.file_item_layout);
                    }
                    listView.setAdapter(mAudioAdapter);
                    break;
                //录音失败
                case Constant.RECORD_FAIL:
                    showToastMsg(getString(R.string.record_fail));
                    break;
                //录音时间太短
                case Constant.RECORD_TOO_SHORT:
                    showToastMsg(getString(R.string.time_too_short));
                    break;
                case Constant.PLAY_COMPLETION:
                    showToastMsg(getString(R.string.play_over));
                    break;
                case Constant.PLAY_ERROR:
                    showToastMsg(getString(R.string.play_error));
                    break;

            }
        }
    };

    @Override
    protected void setWindowView() {
        setContentView(R.layout.activity_record);
        dataList = new ArrayList<>();
        mExecutorService = Executors.newSingleThreadExecutor();
        mBuffer = new byte[BUFFER_SIZE];
    }

    @Override
    protected void initViews() {
        this.start_tv = (Button) findViewById(R.id.start_tv);
        this.listView = (ListView) findViewById(R.id.listview);
    }

    @Override
    protected void initEvents() {
        this.start_tv.setOnClickListener(this);
        //点击Item播放对应的声音文件
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //使用AudioTrack播放声音流文件
                startPlay(dataList.get(i).getFile());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //开始录音操作
            case R.id.start_tv:
                //正在录音
                if (isRecording) {
                    isRecording = false;
                    start_tv.setText(R.string.start_record);
                    //停止录音
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            stopRecord();
                        }
                    });
                } else {
                    isRecording = true;
                    start_tv.setText(R.string.stop_record);
                    //录音操作
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT > 22) {
                                //6.0以上权限管理
                                permissionForM();
                            } else {
                                //开始录音
                                startRecord();
                            }

                        }
                    });
                }

                break;

        }
    }


    /**
     * @description 开始录音操作
     * @author ldm
     * @time 2017/2/9 16:29
     */
    private void startRecord() {
        try {
            //创建录音文件,.m4a为MPEG-4音频标准的文件的扩展名
            mAudioFile = new File(mFilePath + System.currentTimeMillis() + ".pcm");
            //创建父文件夹
            mAudioFile.getParentFile().mkdirs();
            //创建文件
            mAudioFile.createNewFile();
            //创建文件输出流
            mFileOutPutStream = new FileOutputStream(mAudioFile);
            //配置AudioRecord
            //从麦克风采集数据
            int audioSource = MediaRecorder.AudioSource.MIC;
            //设置采样频率
            int sampleRate = 44100;
            //设置单声道输入
            int channelConfig = AudioFormat.CHANNEL_IN_MONO;
            //设置格式，安卓手机都支持的是PCM16
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //计算AudioRecord内部buffer大小
            int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            //根据上面的设置参数初始化AudioRecord
            mAudioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, Math.max(minBufferSize, BUFFER_SIZE));
            //开始录音
            mAudioRecord.startRecording();
            //记录开始时间
            startTime = System.currentTimeMillis();
            //写入数据到文件
            while (isRecording) {
                int read = mAudioRecord.read(mBuffer, 0, BUFFER_SIZE);
                if (read > 0) {
                    //保存到指定文件
                    mFileOutPutStream.write(mBuffer, 0, read);
                }
            }
        } catch (IOException e) {
            mHandler.sendEmptyMessage(Constant.RECORD_FAIL);
        } finally {
//            if (null != mAudioRecord) {
//                //释放资源
//                mAudioRecord.release();
//            }
        }

    }

    /**
     * @description 停止录音
     * @author ldm
     * @time 2017/2/9 16:45
     */
    private void stopRecord() {
        try {
            //停止录音
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            mFileOutPutStream.close();
            //记录时长
            endTime = System.currentTimeMillis();
            //录音时间处理，比如只有大于2秒的录音才算成功
            int time = (int) ((endTime - startTime) / 1000);
            if (time >= 3) {
                //录音成功,添加数据
                FileBean bean = new FileBean();
                bean.setFile(mAudioFile);
                bean.setFileLength(time);
                dataList.add(bean);
                //录音成功,发Message
                mHandler.sendEmptyMessage(Constant.RECORD_SUCCESS);
            } else {
                mAudioFile = null;
                mHandler.sendEmptyMessage(Constant.RECORD_TOO_SHORT);
            }
        } catch (Exception e) {
            mHandler.sendEmptyMessage(Constant.RECORD_FAIL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mExecutorService) {
            mExecutorService.shutdownNow();
        }
    }
    /*******6.0以上版本手机权限处理***************************/
    /**
     * @description 兼容手机6.0权限管理
     * @author ldm
     * @time 2016/5/24 14:59
     */
    private void permissionForM() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constant.PERMISSIONS_REQUEST_FOR_AUDIO);
        } else {
            startRecord();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Constant.PERMISSIONS_REQUEST_FOR_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecord();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void playAudio(final File audioFile) {
        isPlaying = true;
        //异步执行声音播放功能
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                startPlay(audioFile);
            }
        });
    }

    /**
     * @description 播放音频流文件声音
     * @author ldm
     * @time 2017/2/10 8:13
     */
    private void startPlay(File audioFile) {
        AudioTrack audioTrack = null;
        FileInputStream fis = null;
        try {
            //配置播放器
            //首先设备播放器声音类型
            int streamType = AudioManager.STREAM_MUSIC;
            //设置播放的频率，和录音时的频率一致
            int sampleRate = 44100;
            //播放输出声道
            int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            //播放格式
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            //设置流模式
            int mode = AudioTrack.MODE_STREAM;
            //计算buffer大小
            int minSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
            //初始化播放器
            audioTrack = new AudioTrack(streamType, sampleRate, channelConfig, audioFormat, Math.max(minSize, BUFFER_SIZE), mode);
            //从音频文件中读取文件流数据
            fis = new FileInputStream(mAudioFile);
            //读取数据到播放器中
            int read = -1;
            while ((read = fis.read(mBuffer)) > 0) {
                int ret = audioTrack.write(mBuffer, 0, read);
                switch (ret) {
                    case AudioTrack.ERROR_INVALID_OPERATION:
                    case AudioTrack.ERROR_BAD_VALUE:
                    case AudioTrack.ERROR:
                        playFail(audioTrack);
                        return;
                    default:
                        break;
                }
            }
            mHandler.sendEmptyMessage(Constant.PLAY_COMPLETION);
        } catch (Exception e) {
            e.printStackTrace();
            playFail(audioTrack);
        } finally {
            isPlaying = false;
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @description
     * @author ldm
     * @time 2017/2/10 播放失败处理
     */
    private void playFail(AudioTrack audioTrack) {
        mAudioFile = null;
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        mHandler.sendEmptyMessage(Constant.PLAY_ERROR);
    }
}
