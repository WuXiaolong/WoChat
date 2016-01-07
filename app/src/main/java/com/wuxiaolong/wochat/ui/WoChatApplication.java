package com.wuxiaolong.wochat.ui;

import android.app.Application;
import android.text.TextUtils;

import com.avos.avoscloud.AVOSCloud;
import com.avoscloud.leanchatlib.controller.ConversationEventHandler;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.avoscloud.leanchatlib.controller.LeanchatUserProvider;
import com.avoscloud.leanchatlib.controller.ThirdPartUserUtils;
import com.wuxiaolong.wochat.leancloud.ChatManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WuXiaolong on 2015/12/2.
 */
public class WoChatApplication extends Application {
    String appId = "HLDqbejBl9oJj6IbAFNP8LY5";
    String appKey = "jAchoSJBaGmaUMyjCDAsvM9D";
    public static boolean debug = true;
    public static volatile List<LeanchatUser> friendMsgList = new ArrayList<>();


    public static List<LeanchatUser> getFriendMsgList() {
        return friendMsgList;
    }

    public static void setFriendMsgList(List<LeanchatUser> friendMsgList) {
        WoChatApplication.friendMsgList = friendMsgList;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LeanchatUser.alwaysUseSubUserClass(LeanchatUser.class);
        AVOSCloud.initialize(this, appId, appKey);
//        AVObject.registerSubclass(AddRequest.class);
//        AVObject.registerSubclass(UpdateInfo.class);
        ThirdPartUserUtils.setThirdPartUserProvider(new LeanchatUserProvider());
        initChatManager();
    }

    private void initChatManager() {
        final ChatManager chatManager = ChatManager.getInstance();
        chatManager.init(this);
        String currentUserId = LeanchatUser.getCurrentUserId();
        if (!TextUtils.isEmpty(currentUserId)) {
            chatManager.setupManagerWithUserId(this, currentUserId);
        }
        chatManager.setConversationEventHandler(ConversationEventHandler.getInstance());
        ChatManager.setDebugEnabled(debug);
    }

}
