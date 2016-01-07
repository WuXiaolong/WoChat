package com.wuxiaolong.wochat.ui;

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
import com.avos.avoscloud.LogInCallback;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.leancloud.ChatManager;
import com.wuxiaolong.wochat.leancloud.UserCacheUtils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {


    // UI references.
    private EditText mUserId;
    private EditText mPasswordView;
    private View mProgressView;
    public static final String AUTO_LOGIN_STATE_ACTION = "com.openim.autoLoginStateActionn";
    private static final String USER_ID = "userId";
    private static final String PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
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
        LeanchatUser.logInInBackground(userId, password, new LogInCallback<LeanchatUser>() {
            @Override
            public void done(LeanchatUser avUser, AVException e) {
                if (filterException(e)) {
                    ChatManager chatManager = ChatManager.getInstance();
                    chatManager.setupManagerWithUserId(LoginActivity.this, LeanchatUser.getCurrentUserId());
                    chatManager.openClient(null);
                    UserCacheUtils.cacheUser(LeanchatUser.getCurrentUser());
                    new ChatRoomActivity().openConversation(LoginActivity.this, "剩者为王");
                } else {
                    Log.e("wxl", "AVException==" + e.getMessage());
                }
            }
        }, LeanchatUser.class);


    }


}

