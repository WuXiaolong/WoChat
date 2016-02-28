package com.wuxiaolong.xmpp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, XMPPClickListener {
    private XMPPService mXMPPService;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mXMPPService = new XMPPService();
        mXMPPService.setXMPPClickListener(this);
        mXMPPService.initXMPPTCPConnection();
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("加载中");
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.changePassword).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        progressDialogShow();
        switch (v.getId()) {
            case R.id.connect:
                mXMPPService.connect();
                break;
            case R.id.register:
                mXMPPService.register("test001", "123456");
                break;
            case R.id.login:
                mXMPPService.login("test001", "123456");
                break;
            case R.id.changePassword:
                mXMPPService.changePassword("654321");
                break;
            default:

                break;
        }
    }

//    @Override
//    public void connect(String msg) {
//        toastShow(msg);
//    }
//
//    @Override
//    public void register(String msg) {
//        Log.i("wxl", "注册=" + msg);
//        toastShow(msg);
//    }
//
//    @Override
//    public void login(String msg) {
//        Log.i("wxl", "登录=" + msg);
//        toastShow(msg);
//    }
//
//    @Override
//    public void changePassword(String msg) {
//        Log.i("wxl", "changePassword=" + msg);
//        toastShow(msg);
//    }

    private void toastShow(CharSequence text) {
        progressDialogDismiss();
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();

    }


    private void progressDialogShow() {

        mProgressDialog.show();

    }

    private void progressDialogDismiss() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();

    }

    @Override
    public void xmppCallback() {
        progressDialogDismiss();
    }
}
