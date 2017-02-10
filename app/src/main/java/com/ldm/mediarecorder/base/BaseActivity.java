package com.ldm.mediarecorder.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

/**
 * description： 作者：ldm 时间：20172017/2/8 10:13 邮箱：1786911211@qq.com
 */
public abstract class BaseActivity extends FragmentActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowView();
        initViews();
        initEvents();
    }

    protected abstract void setWindowView();

    protected abstract void initViews();

    protected abstract void initEvents();

    protected void showToastMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void startActivity(Activity act, Class cls) {
        Intent intent = new Intent(act, cls);
        act.startActivity(intent);
    }
}
