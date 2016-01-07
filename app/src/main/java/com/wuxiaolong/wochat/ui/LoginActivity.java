package com.wuxiaolong.wochat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SaveCallback;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.util.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {


    // UI references.
    private EditText mUserId;
    private EditText mPasswordView;
    private View mProgressView;
    Tencent mTencent;
    TencentLoginListener mTencentLoginListener;
    String token, openid, expires_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Tencent类是SDK的主要实现类，开发者可通过Tencent类访问腾讯开放的OpenAPI。其中APP_ID是分配给第三方应用的appid，类型为String。
        mTencent = Tencent.createInstance(AppConstant.TENCENT_APP_ID, LoginActivity.this);
        // Set up the login form.
        mUserId = (EditText) findViewById(R.id.userId);
        mProgressView = findViewById(R.id.login_progress);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


    }


    private void attemptLogin() {

        // Reset errors.
        mUserId.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String userId = mUserId.getText().toString();
        final String password = mPasswordView.getText().toString();

        if (TextUtils.isEmpty(userId)) {
            mUserId.setError(getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
        }


        mProgressView.setVisibility(View.VISIBLE);
        if (!mTencent.isSessionValid()) {
            mTencentLoginListener = new TencentLoginListener();
            mTencentLoginListener.setIsLogin(true);
            mTencent.login(this, "all", mTencentLoginListener);

        } else {
            UserInfo userInfo = new UserInfo(getApplicationContext(), mTencent.getQQToken());
            mTencentLoginListener.setIsLogin(false);
            userInfo.getUserInfo(mTencentLoginListener);
        }
//        LeanchatUser.logInInBackground(userId, password, new LogInCallback<LeanchatUser>() {
//            @Override
//            public void done(LeanchatUser avUser, AVException e) {
//                if (filterException(e)) {
//                    ChatManager chatManager = ChatManager.getInstance();
//                    chatManager.setupManagerWithUserId(LoginActivity.this, LeanchatUser.getCurrentUserId());
//                    chatManager.openClient(null);
//                    UserCacheUtils.cacheUser(LeanchatUser.getCurrentUser());
//                    new ChatRoomActivity().openConversation(LoginActivity.this, "剩者为王");
//                } else {
//                    Log.e("wxl", "AVException==" + e.getMessage());
//                }
//            }
//        }, LeanchatUser.class);


    }

    class TencentLoginListener implements IUiListener {
        boolean isLogin;

        public boolean isLogin() {
            return isLogin;
        }

        public void setIsLogin(boolean isLogin) {
            this.isLogin = isLogin;
        }


        @Override
        public void onComplete(Object object) {
            Log.e("wxl", " mTencent.login==" + object);
            Log.e("wxl", " mTencent.getOpenId==" + mTencent.getOpenId());
            if (isLogin) {
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


            } else {
                loginByLeancloud(object);
            }
        }

        @Override
        public void onError(UiError uiError) {
            Log.e("wxl", " mTencent.onError==" + uiError);
        }

        @Override
        public void onCancel() {
            Log.e("wxl", " mTencent.onCancel==");
        }
    }

    String nickname, avatar;

    public void loginByLeancloud(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        try {
            nickname = jsonObject.getString("nickname");
            avatar = jsonObject.getString("figureurl_qq_2");

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        String nickname = map.get("openid");
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
                    showToast("第三方 loginSucceed");

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
        leanchatUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (filterException(e)) {
                    showToast("第三方 loginSucceed");
                } else {
                    showToast("第三方 loginFail");
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mTencentLoginListener != null)
            Tencent.onActivityResultData(requestCode, resultCode, data, mTencentLoginListener);

    }
}

