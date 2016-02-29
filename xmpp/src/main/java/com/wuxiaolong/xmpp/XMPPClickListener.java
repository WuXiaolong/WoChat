package com.wuxiaolong.xmpp;

/**
 * Created by WuXiaolong
 * Date on 2016/2/23.
 */
public interface XMPPClickListener {
    void connect(String msg);

    void login(String msg);
    void register(String msg);
    void changePassword(String msg);
    void setAvatar(String msg);
}
