package com.wuxiaolong.wochat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wuxiaolong.wochat.R;

import de.greenrobot.event.EventBus;


public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
