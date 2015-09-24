package com.wuxiaolong.wochat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.mvp.presenter.LoginPresenter;
import com.wuxiaolong.wochat.mvp.view.LoginView;
import com.wuxiaolong.wochat.view.TipDialog;
import com.wuxiaolong.wochat.xmpp.XMPPLogin;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by 小尛龙 on 2015/7/3.
 */

public class LoginActivity extends BaseActivity implements LoginView {
    Button btn_login;
    @Bind(R.id.userName)
    EditText userName;
    @Bind(R.id.userPassword)
    EditText userPassword;
    TipDialog mTipDialog;
    LoginPresenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mLoginPresenter = new LoginPresenter();
        mLoginPresenter.attachView(this);
        mTipDialog = new TipDialog(LoginActivity.this, R.style.dialog);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLoginPresenter.startLogin(userName.getText().toString(), userPassword.getText().toString());
                    }
                }
        );
    }

    @Override
    public void loginSuccess() {
        new XMPPLogin().setOnStatusCallbackListener(new OnStatusCallback());
        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    @Override
    public void loginFail(String failMsg) {
        Toast.makeText(LoginActivity.this, "登录失败：" + failMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void progressShow() {
        mTipDialog.show();

    }

    @Override
    public void progressDismiss() {
        mTipDialog.dismiss();

    }


    class OnStatusCallback implements XMPPLogin.OnStatusCallbackListener {

        @Override
        public void disconnect(String reason) {
            Log.d("wxl", "disconnect");
            Toast.makeText(LoginActivity.this, "disconnect", Toast.LENGTH_LONG).show();
        }

        @Override
        public void connecting() {
            Log.d("wxl", "connecting");
            Toast.makeText(LoginActivity.this, "connecting", Toast.LENGTH_LONG).show();
        }


        @Override
        public void authenticated() {
            Log.d("wxl", "authenticated");
            Toast.makeText(LoginActivity.this, "authenticated", Toast.LENGTH_LONG).show();
        }
    }

}
