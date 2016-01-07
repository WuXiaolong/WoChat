package com.wuxiaolong.wochat.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.wuxiaolong.wochat.AppConstant;
import com.wuxiaolong.wochat.AppUtil;
import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.leancloud.AVImClientManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChatRoomActivity extends BaseActivity {
    protected ChatFragment chatFragment;
    protected AVIMConversation mAVIMConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        chatFragment = new ChatFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_chat, chatFragment).commit();
//        chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
        String conversationId = getIntent().getStringExtra(AppConstant.CONVERSATION_ID);
        String title = getIntent().getStringExtra(AppConstant.ACTIVITY_TITLE);
        initToolbar(title);
        getSquare(conversationId);
        queryInSquare(conversationId);
    }
//

    /**
     * 根据 conversationId 查取本地缓存中的 conversation，如若没有缓存，则返回一个新建的 conversaiton
     */
    private void getSquare(String conversationId) {
        if (TextUtils.isEmpty(conversationId)) {
            throw new IllegalArgumentException("conversationId can not be null");
        }
        AVIMClient client = AVImClientManager.getInstance().getClient();
        mAVIMConversation = client.getConversation(conversationId);
    }

    /**
     * 先查询自己是否已经在该 conversation，如果存在则直接给 chatFragment 赋值，否则先加入，再赋值
     */

    private void queryInSquare(String conversationId) {
        final AVIMClient client = AVImClientManager.getInstance().getClient();
        AVIMConversationQuery conversationQuery = client.getQuery();
        conversationQuery.whereEqualTo("objectId", conversationId);
        conversationQuery.setQueryPolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
        conversationQuery.containsMembers(Arrays.asList(AVImClientManager.getInstance().getClientId()));
        conversationQuery.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (filterException(e)) {
                    if (null != list && list.size() > 0) {
                        chatFragment.setConversation(list.get(0));
                    } else {
                        joinSquare();
                    }
                } else {
                    Log.e("wxl", "queryInSquare  e=" + e.getMessage());
                }
            }
        });
    }

    /**
     * 加入 conversation
     */
    private void joinSquare() {
        mAVIMConversation.join(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (filterException(e)) {

                    chatFragment.setConversation(mAVIMConversation);
                } else {
                    Log.e("wxl", "joinSquare AVIMException=" + e.getMessage());
                }
            }
        });
    }

    /**
     * 连接聊天室服务器
     */
    static ProgressDialog mProgressDialog;

    public static void openConversation(final Activity fromActivity, final String roomName) {
        mProgressDialog = AppUtil.showProgressDialog(fromActivity);
        final String userName = LeanchatUser.getCurrentUser().getUsername();

        AVImClientManager.getInstance().open(userName, new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (filterException(e)) {
                    findConversation(avimClient, userName, roomName, fromActivity);
                } else {
                    Log.e("wxl", "open AVIMException=" + e.getMessage());
                    mProgressDialog.dismiss();
                }
            }
        });
    }


    /**
     * 通过条件查询聊天室
     */
    public static void findConversation(final AVIMClient avimClient, final String userName, final String roomName, final Activity fromActivity) {
        AVIMConversationQuery conversationQuery = avimClient.getQuery();
        conversationQuery.whereEqualTo(AppConstant.ROOM_NAME, roomName);
        conversationQuery.findInBackground(
                new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVIMException e) {
                        if (filterException(e)) {
                            if (list.size() == 0) {
                                createConversation(avimClient, userName, roomName, fromActivity);
                            } else {
                                gotoChatRoomActivity(fromActivity, list.get(0), roomName);
                            }
                        } else {
                            mProgressDialog.dismiss();
                            Log.e("wxl", "findConversation AVIMException=" + e.getMessage());
                        }
                    }
                });
    }


    public static void createConversation(AVIMClient avimClient, String userName, final String roomName, final Activity fromActivity) {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AppConstant.ROOM_NAME, roomName);
        //第2个参数是聊天室名字
        avimClient.createConversation(Arrays.asList(userName), roomName, null, true,
                new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation avimConversation, AVIMException e) {
                        if (filterException(e)) {
                            gotoChatRoomActivity(fromActivity, avimConversation, roomName);

                        } else {
                            mProgressDialog.dismiss();
                            Log.e("wxl", "createConversation AVIMException=" + e.getMessage());
                        }
                    }

                });
    }

    public static void gotoChatRoomActivity(final Activity fromActivity, AVIMConversation avimConversation, final String roomName) {
//        ChatManager.getInstance().getRoomsTable().insertRoom(avimConversation.getConversationId());
        Intent intent = new Intent(fromActivity, ChatRoomActivity.class);
        intent.putExtra(AppConstant.CONVERSATION_ID, avimConversation.getConversationId());
        intent.putExtra(AppConstant.ACTIVITY_TITLE, roomName);
        fromActivity.startActivity(intent);
        mProgressDialog.dismiss();
    }
}
