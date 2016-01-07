package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avoscloud.leanchatlib.event.ConversationChangeEvent;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by wli on 15/12/1.
 * 和 Conversation 相关的事件的 handler
 * 需要应用主动调用  AVIMMessageManager.setConversationEventHandler
 */
public class ConversationEventHandler extends AVIMConversationEventHandler {

    private static ConversationEventHandler eventHandler;

    public static synchronized ConversationEventHandler getInstance() {
        if (null == eventHandler) {
            eventHandler = new ConversationEventHandler();
        }
        return eventHandler;
    }

    private ConversationEventHandler() {
    }

    @Override
    public void onOfflineMessagesUnread(AVIMClient client, AVIMConversation conversation, int unreadCount) {
//    LogUtils.i("onOfflineMessagesUnread");
        super.onOfflineMessagesUnread(client, conversation, unreadCount);
    }

    @Override
    public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
//    LogUtils.i("onMemberLeft");
        refreshCacheAndNotify(conversation);
    }

    @Override
    public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
//    LogUtils.i("onMemberJoined");
        refreshCacheAndNotify(conversation);
    }

    private void refreshCacheAndNotify(AVIMConversation conversation) {
        ConversationChangeEvent conversationChangeEvent = new ConversationChangeEvent(conversation);
        EventBus.getDefault().post(conversationChangeEvent);
    }

    @Override
    public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
//    LogUtils.i("onKicked");
        refreshCacheAndNotify(conversation);
    }

    @Override
    public void onInvited(AVIMClient client, AVIMConversation conversation, String operator) {
//    LogUtils.i("onInvited");
        refreshCacheAndNotify(conversation);
    }
}
