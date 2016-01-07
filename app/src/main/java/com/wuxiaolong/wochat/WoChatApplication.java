package com.wuxiaolong.wochat;

import android.app.Application;
import android.text.TextUtils;

import com.avos.avoscloud.AVOSCloud;
import com.avoscloud.leanchatlib.controller.ConversationEventHandler;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.avoscloud.leanchatlib.utils.ThirdPartUserUtils;
import com.wuxiaolong.wochat.leancloud.ChatManager;
import com.wuxiaolong.wochat.leancloud.LeanchatUserProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by WuXiaolong on 2015/12/2.
 */
public class WoChatApplication extends Application {
    public static boolean debug = true;
    public static volatile List<LeanchatUser> friendMsgList = new ArrayList<>();
    public static volatile List<Map<String, String>> channelList = new ArrayList<>();//频道list

    public static List<Map<String, String>> getChannelList() {
        return channelList;
    }

    public static void setChannelList(List<Map<String, String>> channelList) {
        WoChatApplication.channelList = channelList;
    }

    public static List<LeanchatUser> getFriendMsgList() {
        return friendMsgList;
    }

    public static void setFriendMsgList(List<LeanchatUser> friendMsgList) {
        WoChatApplication.friendMsgList = friendMsgList;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String appId = "HLDqbejBl9oJj6IbAFNP8LY5";
        String appKey = "jAchoSJBaGmaUMyjCDAsvM9D";
//        initImageLoader(this);
        LeanchatUser.alwaysUseSubUserClass(LeanchatUser.class);
        AVOSCloud.initialize(this, appId, appKey);
//        AVObject.registerSubclass(AddRequest.class);
//        AVObject.registerSubclass(UpdateInfo.class);
        // 必须在启动的时候注册 MessageHandler
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

    /**
     * 初始化ImageLoader
     */
//    public static void initImageLoader(Context context) {
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
//                context)
//                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
//                        //.memoryCache(new WeakMemoryCache())
//                .denyCacheImageMultipleSizesInMemory()
//                .tasksProcessingOrder(QueueProcessingType.LIFO)
//                .build();
//        ImageLoader.getInstance().init(config);
//    }
}
