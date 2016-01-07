package com.wuxiaolong.wochat.leancloud;

import android.content.Context;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationEventHandler;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 该类来负责处理接收消息、聊天服务连接状态管理、查找对话、获取最近对话列表最后一条消息
 * Created by lzw on 15/2/10.
 */
public class ChatManager {
    private static ChatManager chatManager;

    private volatile AVIMClient imClient;
    private volatile String selfId;

//    private RoomsTable roomsTable;

    private ChatManager() {
    }

    public static synchronized ChatManager getInstance() {
        if (chatManager == null) {
            chatManager = new ChatManager();
        }
        return chatManager;
    }

    /**
     * 设置是否打印 leanchatlib 的日志，发布应用的时候要关闭
     * 日志 TAG 为 leanchatlib，可以获得一些异常日志
     *
     * @param debugEnabled
     */
    public static void setDebugEnabled(boolean debugEnabled) {
//        LogUtils.debugEnabled = debugEnabled;
    }

    /**
     * 请在应用一启动(Application onCreate)的时候就调用，因为 SDK 一启动，就会去连接聊天服务器
     * 如果没有调用此函数设置 messageHandler ，就可能丢失一些消息
     *
     * @param context
     */
    public void init(Context context) {
        AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, new MessageHandler(context));
        AVIMClient.setClientEventHandler(LeanchatClientEventHandler.getInstance());
        //签名
        //AVIMClient.setSignatureFactory(new SignatureFactory());
    }

    /**
     * 设置 AVIMConversationEventHandler，用来处理对话成员变更回调
     *
     * @param eventHandler
     */
    public void setConversationEventHandler(AVIMConversationEventHandler eventHandler) {
        AVIMMessageManager.setConversationEventHandler(eventHandler);
    }

    /**
     * 请在登录之后，进入 MainActivity 之前，调用此函数，因为此时可以拿到当前登录用户的 ID
     *
     */
    public void setupManagerWithUserId(Context context, String userId) {
        this.selfId = userId;
//        roomsTable = RoomsTable.getInstanceByUserId(context.getApplicationContext(), userId);
    }
    public String getSelfId() {
        return selfId;
    }

//  public RoomsTable getRoomsTable() {
//    return roomsTable;
//  }

    public AVIMClient getImClient() {
        return imClient;
    }

    /**
     * 连接聊天服务器，用 userId 登录，在进入MainActivity 前调用
     *
     * @param callback AVException 常发生于网络错误、签名错误
     */
    public void openClient(final AVIMClientCallback callback) {
        if (this.selfId == null) {
            throw new IllegalStateException("please call setupManagerWithUserId() first");
        }
        imClient = AVIMClient.getInstance(this.selfId);
        imClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e != null) {
                    LeanchatClientEventHandler.getInstance().setConnectAndNotify(false);
                } else {
                    LeanchatClientEventHandler.getInstance().setConnectAndNotify(true);
                }
                if (callback != null) {
                    callback.done(avimClient, e);
                }
            }
        });
    }

    /**
     * 用户注销的时候调用，close 之后消息不会推送过来，也不可以进行发消息等操作
     *
     * @param callback AVException 常见于网络错误
     */
    public void closeWithCallback(final AVIMClientCallback callback) {
        imClient.close(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e != null) {
                }
                if (callback != null) {
                    callback.done(avimClient, e);
                }
            }
        });
        imClient = null;
        selfId = null;
    }

    /**
     * 获取 AVIMConversationQuery，用来查询对话
     *
     * @return
     */
    public AVIMConversationQuery getConversationQuery() {
        return imClient.getQuery();
    }

    //ChatUser
//    public List<Room> findRecentRooms() {
//        return ChatManager.getInstance().getRoomsTable().selectRooms();
//    }

    List<AVIMTypedMessage> filterTypedMessages(List<AVIMMessage> messages) {
        List<AVIMTypedMessage> resultMessages = new ArrayList<>();
        for (AVIMMessage msg : messages) {
            if (msg instanceof AVIMTypedMessage) {
                resultMessages.add((AVIMTypedMessage) msg);
            } else {
            }
        }
        return resultMessages;
    }


    /**
     * 查找对话的最后一条消息，如果已查找过，则立即返回
     *
     * @param conversation
     * @return 当向服务器查找失败时或无历史消息时，返回 null
     */
    public synchronized AVIMTypedMessage queryLatestMessage(AVIMConversation conversation) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<AVIMTypedMessage> typeMessages = new ArrayList<>();
        conversation.queryMessages(null, 0, 1, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
                if (e == null) {
                    typeMessages.addAll(filterTypedMessages(list));
                }
                latch.countDown();
            }
        });
        latch.await();
        if (typeMessages.size() > 0) {
            return typeMessages.get(0);
        } else {
            return null;
        }
    }

}
