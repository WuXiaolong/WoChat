package com.wuxiaolong.wochat.ui;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avoscloud.leanchatlib.adapter.MultipleItemAdapter;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.avoscloud.leanchatlib.event.ImTypeMessageEvent;
import com.avoscloud.leanchatlib.event.ImTypeMessageResendEvent;
import com.avoscloud.leanchatlib.event.InputBottomBarTextEvent;
import com.avoscloud.leanchatlib.ui.BaseFragment;
import com.avoscloud.leanchatlib.view.AVInputBottomBar;
import com.wuxiaolong.wochat.R;
import com.wuxiaolong.wochat.WoChatApplication;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 */
public class ChatFragment extends BaseFragment {

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.chatRecyclerView)
    RecyclerView chatRecyclerView;
    @Bind(R.id.chatInputbottombar)
    AVInputBottomBar chatInputbottombar;
    LinearLayoutManager mLinearLayoutManager;
    MultipleItemAdapter mMultipleItemAdapter;
    protected AVIMConversation mAVIMConversation;
    List<LeanchatUser> friendMsgList;
    boolean isOnlyFriend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        friendMsgList = WoChatApplication.getFriendMsgList();

        mLinearLayoutManager = new LinearLayoutManager(mActivity);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMultipleItemAdapter = new MultipleItemAdapter();
        chatRecyclerView.setAdapter(mMultipleItemAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AVIMMessage message = mMultipleItemAdapter.getFirstMessage();
                if (message != null) {
                    mAVIMConversation.queryMessages(message.getMessageId(), message.getTimestamp(), 20,
                            new AVIMMessagesQueryCallback() {
                                @Override
                                public void done(List<AVIMMessage> list, AVIMException e) {
                                    swipeRefreshLayout.setRefreshing(false);
                                    if (filterException(e)) {
                                        if (null != list && list.size() > 0) {
                                            if (isOnlyFriend) {
                                                //只接受好友和自己消息
                                                List<AVIMMessage> friendList = new ArrayList<>();
                                                LeanchatUser user = LeanchatUser.getCurrentUser();
                                                for (AVIMMessage avimMessage : list) {
                                                    String msgFrom = avimMessage.getFrom();
                                                    if (friendMsgList.contains(msgFrom) || user.getUsername().equals(msgFrom)) {
                                                        friendList.add(avimMessage);
                                                    }
                                                }
                                                addMessageList(friendList);
                                            } else {
                                                addMessageList(list);
                                            }

                                        }
                                    }
                                }
                            });
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    void addMessageList(List<AVIMMessage> list) {
        mMultipleItemAdapter.addMessageList(list);
        mMultipleItemAdapter.notifyDataSetChanged();
        mLinearLayoutManager.scrollToPositionWithOffset(list.size() - 1, 0);


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("wxl", "ChatFragment onResume");
        if (null != mAVIMConversation) {
//            NotificationUtils.addTag(mAVIMConversation.getConversationId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("wxl", "ChatFragment onPause");
        if (null != mAVIMConversation) {
//            NotificationUtils.removeTag(mAVIMConversation.getConversationId());
        }
    }

    public void setConversation(AVIMConversation conversation) {
        mAVIMConversation = conversation;
        swipeRefreshLayout.setEnabled(true);
        chatInputbottombar.setTag(mAVIMConversation.getConversationId());
        fetchMessages();
//        NotificationUtils.addTag(conversation.getConversationId());
    }

    /**
     * 拉取消息，必须加入 conversation 后才能拉取消息
     */
    private void fetchMessages() {
        mAVIMConversation.queryMessages(
                new AVIMMessagesQueryCallback() {
                    @Override
                    public void done(List<AVIMMessage> list, AVIMException e) {
                        if (filterException(e)) {
                            if (null != list && list.size() > 0) {
                                Log.e("wxl", "fetchMessages isOnlyFriend=" + isOnlyFriend);
                                if (isOnlyFriend) {
                                    //只接受好友和自己消息
                                    List<AVIMMessage> friendList = new ArrayList<>();
                                    LeanchatUser user = LeanchatUser.getCurrentUser();
                                    for (AVIMMessage avimMessage : list) {
                                        String msgFrom = avimMessage.getFrom();
                                        if (friendMsgList.contains(msgFrom) || user.getUsername().equals(msgFrom)) {
                                            friendList.add(avimMessage);
                                        }
                                    }
                                    fetchMessages(friendList);
                                } else {
                                    fetchMessages(list);
                                }


                            }
                        } else {
                            Log.e("wxl", "fetchMessages AVIMException=" + e.getMessage());
                        }
                    }
                }

        );
    }


    void fetchMessages(List<AVIMMessage> list) {
        mMultipleItemAdapter.setMessageList(list);
        chatRecyclerView.setAdapter(mMultipleItemAdapter);
        mMultipleItemAdapter.notifyDataSetChanged();
        scrollToBottom();

    }

    private void scrollToBottom() {
        mLinearLayoutManager.scrollToPositionWithOffset(mMultipleItemAdapter.getItemCount() - 1, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 输入事件处理，接收后构造成 AVIMTextMessage 然后发送
     * 因为不排除某些特殊情况会受到其他页面过来的无效消息，所以此处加了 tag 判断
     */
    public void onEvent(InputBottomBarTextEvent textEvent) {
        if (null != mAVIMConversation && null != textEvent) {
            if (!TextUtils.isEmpty(textEvent.sendContent) && mAVIMConversation.getConversationId().equals(textEvent.tag)) {
                sendText(textEvent.sendContent);
            }
        }
    }

    /**
     * 加好友
     */
//    public void onEvent(LeftChatItemClickEvent event) {
//        if (null != mAVIMConversation && null != event) {
//            Log.i("wxl", "avatarUrl==" + event.avatarUrl);
//            AddFriendDialog addFriendDialog = new AddFriendDialog(mActivity, R.style.AddFriendDialog);
//            addFriendDialog.show();
//            addFriendDialog.initData(event.userId);
//        }
//    }
    private void sendText(String content) {
        AVIMTextMessage message = new AVIMTextMessage();
        message.setText(content);
        sendMessage(message);
    }

    public void sendMessage(AVIMTypedMessage message) {
        mMultipleItemAdapter.addMessage(message);
        mMultipleItemAdapter.notifyDataSetChanged();
        scrollToBottom();
        mAVIMConversation.sendMessage(message, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                mMultipleItemAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 重新发送已经发送失败的消息
     */
    public void onEvent(ImTypeMessageResendEvent event) {
        if (null != mAVIMConversation && null != event) {
            if (AVIMMessage.AVIMMessageStatus.AVIMMessageStatusFailed == event.message.getMessageStatus()
                    && mAVIMConversation.getConversationId().equals(event.message.getConversationId())) {
                mAVIMConversation.sendMessage(event.message, new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                        mMultipleItemAdapter.notifyDataSetChanged();
                    }
                });
                mMultipleItemAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 处理推送过来的消息
     * 同理，避免无效消息，此处加了 conversation id 判断
     */
    public void onEvent(ImTypeMessageEvent event) {
        Log.e("wxl", "处理推送过来的消息");
        if (null != mAVIMConversation && null != event && mAVIMConversation.getConversationId().equals(event.conversation.getConversationId())) {

            if (isOnlyFriend) {
                //只接受好友消息
                if (friendMsgList.contains(event.message.getFrom())) {
                    handleMsg(event);
                }
            } else {
                handleMsg(event);
            }

        }
    }

    public void handleMsg(ImTypeMessageEvent event) {
        mMultipleItemAdapter.addMessage(event.message);
        mMultipleItemAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

}
