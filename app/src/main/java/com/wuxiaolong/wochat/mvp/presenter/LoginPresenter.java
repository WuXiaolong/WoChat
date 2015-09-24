package com.wuxiaolong.wochat.mvp.presenter;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.wuxiaolong.wochat.mvp.view.LoginView;
import com.wuxiaolong.wochat.utils.Constant;
import com.wuxiaolong.wochat.xmpp.XMPPLogin;

/**
 * Created by WuXiaolong on 2015/9/24.
 */
public class LoginPresenter implements Presenter<LoginView> {
    LoginView mLoginView;

    @Override
    public void attachView(LoginView view) {
        this.mLoginView = view;
    }

    @Override
    public void detachView() {
        this.mLoginView = null;
    }

    public void startLogin(final String userName, final String psd) {
        mLoginView.progressShow();
        new LoginAsyncTask().execute(userName, psd);

    }

    class LoginAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            return XMPPLogin.getInstance().xmppLogin(params[0], params[1]);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mLoginView.progressDismiss();
            if (TextUtils.equals(result, Constant.LOGIN_SUCCESS)) {
                mLoginView.loginSuccess();
            } else {
                mLoginView.loginFail(result);
            }

        }
    }
}
