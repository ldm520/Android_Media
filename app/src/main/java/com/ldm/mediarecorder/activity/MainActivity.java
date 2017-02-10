package com.ldm.mediarecorder.activity;

import android.view.View;
import android.widget.TextView;

import com.ldm.mediarecorder.R;
import com.ldm.mediarecorder.base.BaseActivity;

public class MainActivity extends BaseActivity {
    private TextView mediarecorder_tv;
    private TextView audiorecord_tv;
    private TextView mediplayer_tv;
    private TextView videoview_tv;

    @Override
    protected void setWindowView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initViews() {
        this.mediarecorder_tv = (TextView) findViewById(R.id.mediarecorder_tv);
        this.audiorecord_tv = (TextView) findViewById(R.id.audiorecord_tv);
        this.mediplayer_tv = (TextView) findViewById(R.id.mediplayer_tv);
        this.videoview_tv = (TextView) findViewById(R.id.videoview_tv);
    }

    @Override
    protected void initEvents() {
        this.mediarecorder_tv.setOnClickListener(this);
        this.audiorecord_tv.setOnClickListener(this);
        this.mediplayer_tv.setOnClickListener(this);
        this.videoview_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mediarecorder_tv:
                startActivity(this, MediaRecorderActivity.class);
                break;
            case R.id.audiorecord_tv:
                startActivity(this, AudioRecordActivity.class);
                break;
            case R.id.mediplayer_tv:
                startActivity(this, VideoPlayActivity.class);
                break;

            case R.id.videoview_tv:
                startActivity(this, VideoViewActivity.class);
                break;
        }
    }
}
