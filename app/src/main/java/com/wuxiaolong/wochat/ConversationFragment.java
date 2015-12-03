package com.wuxiaolong.wochat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.mobileim.conversation.IYWConversationListener;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {


    public ConversationFragment() {
        // Required empty public constructor
    }

    List<Map<String, String>> mConversationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 获取会话管理类
        IYWConversationService conversationService = LoginSampleHelper.getInstance().getIMKit().getConversationService();
// 添加会话列表变更监听
        conversationService.addConversationListener(new IYWConversationListener() {
            @Override
            public void onItemUpdated() {
                // 会话列表有变更，如果ISV开发者拿list来做ui展现用，可以直接调用BaseAdapter.notifyDataSetChanged()即可。

            }
        });
//获取最近会话列表
        List<YWConversation> conversationList = ConversationSampleHelper.getAllConversations();

        Map<String, String> conversationMap = new HashMap<>();
        mConversationList = new ArrayList<>();
        for (YWConversation conversation : conversationList) {
            if (conversation.getConversationType() == YWConversationType.Tribe) {
                Log.e("wxl", "getLatestContent==" + conversation.getLatestContent() +
                        "\ngetLatestTimeInMillisecond==" + conversation.getLatestTimeInMillisecond() +
                        "\ngetTribeId==" + ConversationSampleHelper.getTribeIdFromConversation(conversation)
                        + "\ngetMessageSender==" + conversation.getConversationBody());
            }

//            conversationMap.put(AppConstants.CONVERSATION_ID,conversation.getConversationId());
//            conversationMap.put(AppConstants.CONVERSATION_ID,conversation.getLatestContent());
//            conversationMap.put(AppConstants.CONVERSATION_ID,conversation.getMessageSender());
        }
    }
}
