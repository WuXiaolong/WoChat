package com.wuxiaolong.wochat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.mvp.presenter.LoginPresenter;
import com.wuxiaolong.wochat.mvp.view.LoginView;
import com.wuxiaolong.wochat.utils.Constant;
import com.wuxiaolong.wochat.utils.PreferenceUtils;

public class WelComeActivity extends AppCompatActivity implements LoginView {
    LoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wel_come);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mLoginPresenter = new LoginPresenter();
        mLoginPresenter.attachView(this);
        boolean isLogin = PreferenceUtils.getPrefBoolean(getApplicationContext(), Constant.ISLOGIN, false);
        if (isLogin) {
            String userName = PreferenceUtils.getPrefString(getApplicationContext(), Constant.USERNAME, "");
            String userPassword = PreferenceUtils.getPrefString(getApplicationContext(), Constant.USERPASSWORD, "");
            mLoginPresenter.startLogin(userName, userPassword);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }


    }

    @Override
    public void loginSuccess() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void loginFail(String failMsg) {
        Toast.makeText(getApplicationContext(), "登录失败：" + failMsg, Toast.LENGTH_LONG).show();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    @Override
    public void progressShow() {

    }

    @Override
    public void progressDismiss() {

    }
}
