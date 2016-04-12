package com.wuxiaolong.easemob.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wuxiaolong.easemob.R;

public class BaseActivity extends AppCompatActivity {
    public String TAG = "wxl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }
}
