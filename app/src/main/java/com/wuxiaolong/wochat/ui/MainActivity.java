package com.wuxiaolong.wochat.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.fragment.ContactsFragment;
import com.wuxiaolong.wochat.fragment.MessageFragment;
import com.wuxiaolong.wochat.fragment.NewsFragment;
import com.wuxiaolong.wochat.utils.AppConfig;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    @Bind(R.id.mainImgContacts)
    ImageView mainImgContacts;
    @Bind(R.id.mainImgMsg)
    ImageView mainImgMsg;
    @Bind(R.id.mainImgNews)
    ImageView mainImgNews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        findViewById(R.id.mainMsg).setOnClickListener(this);
        findViewById(R.id.mainContacts).setOnClickListener(this);
        findViewById(R.id.mainNews).setOnClickListener(this);
        selectTab(1);
    }

    private void initBottomImage() {
        mainImgMsg.setImageResource(R.mipmap.skin_tab_icon_conversation_normal);
        mainImgContacts.setImageResource(R.mipmap.skin_tab_icon_contact_normal);
        mainImgNews.setImageResource(R.mipmap.skin_tab_icon_plugin_normal);
    }

    private void selectTab(int tab) {
        initBottomImage();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (tab) {
            case 0:
                fragmentTransaction.replace(R.id.mainContent, new MessageFragment());
                mainImgMsg.setImageResource(R.mipmap.skin_tab_icon_conversation_selected);
                break;
            case 1:
                fragmentTransaction.replace(R.id.mainContent, new ContactsFragment());
                mainImgContacts.setImageResource(R.mipmap.skin_tab_icon_contact_selected);
                break;
            case 2:
                fragmentTransaction.replace(R.id.mainContent, new NewsFragment());
                mainImgNews.setImageResource(R.mipmap.skin_tab_icon_plugin_selected);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainMsg:
                selectTab(0);
                break;
            case R.id.mainContacts:
                selectTab(1);
                break;
            case R.id.mainNews:
                selectTab(2);
                break;
        }

    }

    public void onEventMainThread(AppConfig.MainEvent event) {
        switch (event) {
            case DISCONNECT:
                Log.d("wxl", "main disconnect");
                Toast.makeText(this, this.getLocalClassName() + "=disconnect", Toast.LENGTH_LONG).show();
                break;
            case AUTHENTICATED:
                Toast.makeText(this, this.getLocalClassName() + "=authenticated", Toast.LENGTH_LONG).show();
                break;
        }
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


}
