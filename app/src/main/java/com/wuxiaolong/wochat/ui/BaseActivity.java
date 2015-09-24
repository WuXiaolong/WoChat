package com.wuxiaolong.wochat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.xmpp.XMPPLogin;


public class BaseActivity extends AppCompatActivity {
    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    public void setStatusCallback() {
        new XMPPLogin().setOnStatusCallbackListener(new XMPPLogin.OnStatusCallbackListener() {

            @Override
            public void disconnect(String reason) {
                Log.d("wxl", "disconnect");
                Toast.makeText(BaseActivity.this, "disconnect", Toast.LENGTH_LONG).show();
            }

            @Override
            public void connecting() {
                Log.d("wxl", "connecting");
                Toast.makeText(BaseActivity.this, "connecting", Toast.LENGTH_LONG).show();
            }


            @Override
            public void authenticated() {
                Log.d("wxl", "authenticated");
                Toast.makeText(BaseActivity.this, "authenticated", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
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
}
