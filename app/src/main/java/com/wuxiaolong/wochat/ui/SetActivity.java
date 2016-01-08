package com.wuxiaolong.wochat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVUser;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.squareup.picasso.Picasso;
import com.wuxiaolong.wochat.R;

import butterknife.Bind;

public class SetActivity extends BaseActivity {
    @Bind(R.id.avatar)
    ImageView avatar;
    @Bind(R.id.nickname)
    TextView nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        initToolbar("我");
        LeanchatUser currentUser = LeanchatUser.getCurrentUser();
        Picasso.with(SetActivity.this).load(currentUser.getAvatarUrl()).placeholder(com.avoscloud.leanchatlib.R.mipmap.chat_default_user_avatar)
                .error(com.avoscloud.leanchatlib.R.mipmap.chat_default_user_avatar).into(avatar);
        nickname.setText(currentUser.getNickname());
    }

    public void logOut(View view) {
        LeanchatUser currentUser = LeanchatUser.getCurrentUser();
        if (currentUser != null) {
            AVUser.logOut();
            LoginActivity.mTencent.logout(SetActivity.this);
            quit();
            startActivity(new Intent(mActivity, LoginActivity.class));
        }
    }
}
