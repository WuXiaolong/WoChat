package com.avoscloud.leanchatlib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avoscloud.leanchatlib.controller.AVImClientManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by WuXiaolong on 2015/12/18.
 */
public class MultipleItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int ITEM_LEFT_TEXT = 0;
    private final int ITEM_RIGHT_TEXT = 1;
    private boolean isShowUserName = true;
    // 时间间隔最小为十分钟
    private final long TIME_INTERVAL = 10 * 60 * 1000;
    private final int ITEM_LEFT = 100;
    private final int ITEM_RIGHT = 200;
    private List<AVIMMessage> messageList = new ArrayList<AVIMMessage>();

    public MultipleItemAdapter() {
    }

    public void setMessageList(List<AVIMMessage> messages) {
        messageList.clear();
        if (null != messages) {
            messageList.addAll(messages);
        }
    }

    public void addMessageList(List<AVIMMessage> messages) {
        messageList.addAll(0, messages);
    }

    /**
     * 发送信息
     */
    public void addMessage(AVIMMessage message) {
        messageList.addAll(Arrays.asList(message));
    }

    public AVIMMessage getFirstMessage() {
        if (null != messageList && messageList.size() > 0) {
            return messageList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_LEFT_TEXT) {
            return new ChatItemTextHolder(parent.getContext(), parent, true);
        } else if (viewType == ITEM_RIGHT_TEXT) {
            return new ChatItemTextHolder(parent.getContext(), parent, false);
        } else {
            //TODO
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AVCommonViewHolder avCommonViewHolder = ((AVCommonViewHolder) holder);
        avCommonViewHolder.bindData(messageList.get(position));
        if (holder instanceof ChatItemHolder) {
            ((ChatItemHolder) holder).showTimeView(shouldShowTime(position));
            ((ChatItemHolder) holder).showUserName(isShowUserName);
        }
    }

    @Override
    public int getItemViewType(int position) {
        AVIMMessage message = messageList.get(position);
        if (null != message && message instanceof AVIMTypedMessage) {
            AVIMTypedMessage typedMessage = (AVIMTypedMessage) message;
            boolean isMe = message.getFrom().equals(AVImClientManager.getInstance().getClientId());
            if (typedMessage.getMessageType() == AVIMReservedMessageType.TextMessageType.getType()) {
                return isMe ? ITEM_RIGHT_TEXT : ITEM_LEFT_TEXT;
            } else {
                return isMe ? ITEM_RIGHT : ITEM_LEFT;
            }
        }
        return 8888;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private boolean shouldShowTime(int position) {
        if (position == 0) {
            return true;
        }
        long lastTime = messageList.get(position - 1).getTimestamp();
        long curTime = messageList.get(position).getTimestamp();
        return curTime - lastTime > TIME_INTERVAL;
    }
}