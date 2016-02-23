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
    private XMPPHandler mXMPPHandler = new XMPPHandler();
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mXMPPService = new XMPPService(mXMPPHandler);
        mXMPPHandler.setXMPPClickListener(this);
        mXMPPService.initXMPPTCPConnection();
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("加载中");
        findViewById(R.id.connect).setOnClickListener(this);
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
        }
    }

    @Override
    public void connect(String msg) {
        toastShow(msg);
    }

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
}
