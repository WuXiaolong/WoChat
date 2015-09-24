package com.wuxiaolong.wochat.mvp.view;

/**
 * Created by WuXiaolong on 2015/9/24.
 */
public interface LoginView {
    void loginSuccess();

    void loginFail(String failMsg);

    void progressShow();

    void progressDismiss();
}
