package com.wuxiaolong.xmpp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, XMPPClickListener {
    private XMPPService mXMPPService;
    private ProgressDialog mProgressDialog;
    private final String TAG = "wxl";

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
        findViewById(R.id.avatar).setOnClickListener(this);
        findViewById(R.id.sendMessage).setOnClickListener(this);
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
                mXMPPService.login("test001", "654321");
                break;
            case R.id.changePassword:
                mXMPPService.changePassword("123456");
                break;
            case R.id.avatar:

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                mXMPPService.setAvatar(bitmap);
                break;
            case R.id.sendMessage:

                mXMPPService.sendMessage("发送消息");
                break;
            default:

                break;
        }
    }


    private void toastShow(final CharSequence text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, text + "");
                progressDialogDismiss();
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });


    }


    private void progressDialogShow() {

        mProgressDialog.show();

    }

    private void progressDialogDismiss() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();

    }


    @Override
    public void connect(String msg) {
        toastShow(msg);
    }

    @Override
    public void login(String msg) {
        toastShow(msg);
    }

    @Override
    public void register(String msg) {
        toastShow(msg);
    }

    @Override
    public void changePassword(String msg) {
        toastShow(msg);
    }

    @Override
    public void setAvatar(String msg) {
        toastShow(msg);

    }
}
