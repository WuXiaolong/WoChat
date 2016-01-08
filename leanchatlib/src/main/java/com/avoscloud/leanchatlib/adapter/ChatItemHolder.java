package com.avoscloud.leanchatlib.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avoscloud.leanchatlib.R;
import com.avoscloud.leanchatlib.controller.LeanchatUser;
import com.avoscloud.leanchatlib.event.ImTypeMessageResendEvent;
import com.avoscloud.leanchatlib.utils.AppUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by WuXiaolong on 2015/12/19.
 */
public class ChatItemHolder extends AVCommonViewHolder {
    protected boolean isLeft;
    protected AVIMMessage message;
    protected ImageView avatarView;
    protected TextView timeView;
    protected TextView nameView;
    protected LinearLayout conventLayout;
    protected FrameLayout statusLayout;
    protected ProgressBar progressBar;
    protected TextView statusView;
    protected ImageView errorView;
    protected Context mContext;

    public ChatItemHolder(Context context, ViewGroup root, boolean isLeft) {
        super(context, root, isLeft ? R.layout.chat_item_left_layout : R.layout.chat_item_right_layout);
        this.isLeft = isLeft;
        this.mContext = context;
        initView();
    }

    public void initView() {
        if (isLeft) {
            avatarView = (ImageView) itemView.findViewById(R.id.chat_left_iv_avatar);
            timeView = (TextView) itemView.findViewById(R.id.chat_left_tv_time);
            nameView = (TextView) itemView.findViewById(R.id.chat_left_tv_name);
            conventLayout = (LinearLayout) itemView.findViewById(R.id.chat_left_layout_content);
            statusLayout = (FrameLayout) itemView.findViewById(R.id.chat_left_layout_status);
            statusView = (TextView) itemView.findViewById(R.id.chat_left_tv_status);
            progressBar = (ProgressBar) itemView.findViewById(R.id.chat_left_progressbar);
            errorView = (ImageView) itemView.findViewById(R.id.chat_left_tv_error);
            avatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNameClick();
                }
            });
        } else {
            avatarView = (ImageView) itemView.findViewById(R.id.chat_right_iv_avatar);
            timeView = (TextView) itemView.findViewById(R.id.chat_right_tv_time);
            nameView = (TextView) itemView.findViewById(R.id.chat_right_tv_name);
            conventLayout = (LinearLayout) itemView.findViewById(R.id.chat_right_layout_content);
            statusLayout = (FrameLayout) itemView.findViewById(R.id.chat_right_layout_status);
            progressBar = (ProgressBar) itemView.findViewById(R.id.chat_right_progressbar);
            errorView = (ImageView) itemView.findViewById(R.id.chat_right_tv_error);
            statusView = (TextView) itemView.findViewById(R.id.chat_right_tv_status);
        }
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onErrorClick();
            }
        });

    }

    @Override
    public void bindData(Object o) {
        message = (AVIMMessage) o;
        timeView.setText(AppUtil.millisecsToDateString(message.getTimestamp()));

        String userId = message.getFrom();
        if (isLeft) {
            AppUtil.queryUser(userId, new FindCallback<LeanchatUser>() {
                @Override
                public void done(List<LeanchatUser> list, AVException e) {
                    if (e == null) {
                        avatarView.setContentDescription(list.get(0).getAvatarUrl());
                        Picasso.with(mContext).load(list.get(0).getAvatarUrl()).placeholder(R.mipmap.chat_default_user_avatar)
                                .error(R.mipmap.chat_default_user_avatar).into(avatarView);
                        nameView.setText(list.get(0).getNickname());
                    }
                }
            });
        } else {
            LeanchatUser currentUser = LeanchatUser.getCurrentUser();
            Picasso.with(mContext).load(currentUser.getAvatarUrl()).placeholder(R.mipmap.chat_default_user_avatar)
                    .error(R.mipmap.chat_default_user_avatar).into(avatarView);
            nameView.setText(currentUser.getNickname());
        }
        switch (message.getMessageStatus()) {
            case AVIMMessageStatusFailed:
                statusLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);
                break;
            case AVIMMessageStatusSent:
                statusLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                break;
            case AVIMMessageStatusSending:
                statusLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                break;
            case AVIMMessageStatusNone:
            case AVIMMessageStatusReceipt:
                statusLayout.setVisibility(View.GONE);
                break;
        }

//        ChatManager.getInstance().getRoomsTable().clearUnread(message.getConversationId());
    }

    public void onErrorClick() {
        ImTypeMessageResendEvent event = new ImTypeMessageResendEvent();
        event.message = message;
        EventBus.getDefault().post(event);
    }

    public void onNameClick() {
//        LeftChatItemClickEvent clickEvent = new LeftChatItemClickEvent();
//        clickEvent.userId = message.getFrom();
//        clickEvent.nickname = nameView.getText().toString();
//        clickEvent.avatarUrl = avatarView.getContentDescription().toString();
//        EventBus.getDefault().post(clickEvent);
    }

    public void showTimeView(boolean isShow) {
        timeView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void showUserName(boolean isShow) {
        nameView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
