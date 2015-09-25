package com.wuxiaolong.wochat.mvp.model;

import android.graphics.drawable.Drawable;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public class ContactsModel {
    String groupName;
    String userJID;
    String userNickName;
    Drawable userAvatar;

    public String getUserNickName() {
        return userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public Drawable getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(Drawable userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getUserJID() {
        return userJID;
    }

    public void setUserJID(String userJID) {
        this.userJID = userJID;
    }
}
