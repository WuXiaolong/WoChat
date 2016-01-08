package com.wuxiaolong.wochat.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.avoscloud.leanchatlib.controller.UserCacheUtils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.leancloud.ChatManager;
import com.wuxiaolong.wochat.ui.chat.ChatRoomActivity;
import com.wuxiaolong.wochat.util.AppConstant;
import com.wuxiaolong.wochat.util.AppUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {


    public static Tencent mTencent;
    private TencentLoginListener mTencentLoginListener;
    private String token, openid, expires_in;
    private String nickname, avatar;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Tencent类是SDK的主要实现类，开发者可通过Tencent类访问腾讯开放的OpenAPI。其中APP_ID是分配给第三方应用的appid，类型为String。
        mTencent = Tencent.createInstance(AppConstant.TENCENT_APP_ID, LoginActivity.this);
        LeanchatUser currentUser = LeanchatUser.getCurrentUser();
        if (currentUser != null) {
            gotoChatRoomActivity();
        }
    }


    public void onLogin(View view) {
        mProgressDialog = AppUtil.showProgressDialog(LoginActivity.this);

        if (!mTencent.isSessionValid()) {
            mTencentLoginListener = new TencentLoginListener();
            mTencentLoginListener.setIsLogin(true);
            mTencent.login(this, "all", mTencentLoginListener);

        } else {
            UserInfo userInfo = new UserInfo(getApplicationContext(), mTencent.getQQToken());
            mTencentLoginListener.setIsLogin(false);
            userInfo.getUserInfo(mTencentLoginListener);
        }


    }

    class TencentLoginListener implements IUiListener {
        boolean isLogin;


        public void setIsLogin(boolean isLogin) {
            this.isLogin = isLogin;
        }


        @Override
        public void onComplete(Object object) {
            if (isLogin) {
                tencentLoginonComplete(object);
            } else {
                loginByLeancloud(object);
            }
        }

        @Override
        public void onError(UiError uiError) {
            mProgressDialog.dismiss();
            Log.e("wxl", " mTencent.onError==" + uiError);
        }

        @Override
        public void onCancel() {
            mProgressDialog.dismiss();
            Log.e("wxl", " mTencent.onCancel==");
        }
    }

    public void tencentLoginonComplete(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        try {
            token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            openid = jsonObject.getString(Constants.PARAM_OPEN_ID);
            expires_in = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            mTencent.setAccessToken(token, expires_in);
            mTencent.setOpenId(openid);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        UserInfo userInfo = new UserInfo(getApplicationContext(), mTencent.getQQToken());
        mTencentLoginListener.setIsLogin(false);
        userInfo.getUserInfo(mTencentLoginListener);
    }


    public void loginByLeancloud(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        try {
            nickname = jsonObject.getString("nickname");
            avatar = jsonObject.getString("figureurl_qq_2");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        AVUser.AVThirdPartyUserAuth auth = new AVUser.AVThirdPartyUserAuth(token, String.valueOf(expires_in), AVUser.AVThirdPartyUserAuth.SNS_TENCENT_WEIBO, openid);
        AVUser.loginWithAuthData(auth, new LeancloudLogInCallback());
    }

    class LeancloudLogInCallback extends LogInCallback<AVUser> {

        @Override
        public void done(AVUser user, AVException e) {

            if (e == null) {
                boolean registerCompleted = user.getBoolean(LeanchatUser.REGISTER_COMPLETED);
                Log.i("wxl", "registerCompleted=" + registerCompleted);
                //恭喜你，已经和我们的 AVUser 绑定成功
                if (registerCompleted) {
                    gotoChatRoomActivity();
                } else {
                    //保存AVFile头像
                    final AVFile avFile = new AVFile(nickname, avatar, null);
                    avFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (filterException(e)) {
                                saveUserAvatar(avFile, nickname);
                            } else {
                                showToast("loginWithAuthData fail");
                            }
                        }
                    });
                }

            } else {
                mProgressDialog.dismiss();
                e.printStackTrace();
                Log.e("wxl", "loginWithAuthData fail");
            }
        }
    }

    /**
     * 保存用户头像和昵称
     */

    private void saveUserAvatar(AVFile avFile, String nickname) {
        final LeanchatUser leanchatUser = LeanchatUser.getCurrentUser();
        leanchatUser.put(LeanchatUser.AVATAR, avFile);
        leanchatUser.put(LeanchatUser.NICKNAME, nickname);
        leanchatUser.put(LeanchatUser.REGISTER_COMPLETED, true);
        leanchatUser.saveInBackground(
                new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        mProgressDialog.dismiss();
                        if (filterException(e)) {
                            gotoChatRoomActivity();
                        } else {
                            showToast("loginFail");
                        }
                    }
                }

        );
    }

    void gotoChatRoomActivity() {
        ChatManager chatManager = ChatManager.getInstance();
        chatManager.setupManagerWithUserId(LoginActivity.this, LeanchatUser.getCurrentUserId());
        chatManager.openClient(null);
        UserCacheUtils.cacheUser(LeanchatUser.getCurrentUser());
        ChatRoomActivity.openConversation(LoginActivity.this, getString(R.string.wewin));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mTencentLoginListener != null)
            Tencent.onActivityResultData(requestCode, resultCode, data, mTencentLoginListener);

    }
}

