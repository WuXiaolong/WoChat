package com.wuxiaolong.wochat.leancloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wuxiaolong.wochat.AppConstant;
import com.wuxiaolong.wochat.ui.ChatRoomActivity;
import com.wuxiaolong.wochat.ui.LoginActivity;


/**
 * Created by wli on 15/9/8.
 * 因为 notification 点击时，控制权不在 app，此时如果 app 被 kill 或者上下文改变后，
 * 有可能对 notification 的响应会做相应的变化，所以此处将所有 notification 都发送至此类，
 * 然后由此类做分发。
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("wxl", "NotificationBroadcastReceiver onReceive");
//        if (ChatManager.getInstance().getImClient() == null) {
//            gotoLoginActivity(context);
//        } else {
//            String conversationId = intent.getStringExtra(AppConstant.CONVERSATION_ID);
//            Log.e("wxl", "NotificationBroadcastReceiver conversationId=" + conversationId);
//            if (!TextUtils.isEmpty(conversationId)) {
////                if (AppConstant.SQUARE_CONVERSATION_ID.equals(conversationId)) {
//                gotoChatRoomActivity(context, intent);
////                } else {
////                    gotoSingleChatActivity(context, intent);
////                }
//            }
//        }
    }

    /**
     * 如果 app 上下文已经缺失，则跳转到登陆页面，走重新登陆的流程
     *
     * @param context
     */
    private void gotoLoginActivity(Context context) {
        Intent startActivityIntent = new Intent(context, LoginActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startActivityIntent);
    }

    /**
     * 跳转至广场页面
     *
     * @param context
     * @param intent
     */
    private void gotoChatRoomActivity(Context context, Intent intent) {
        Intent startActivityIntent = new Intent(context, ChatRoomActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityIntent.putExtra(AppConstant.CONVERSATION_ID, intent.getStringExtra(AppConstant.CONVERSATION_ID));
        startActivityIntent.putExtra(AppConstant.ACTIVITY_TITLE, intent.getStringExtra(AppConstant.ACTIVITY_TITLE));
        context.startActivity(startActivityIntent);
    }

    /**
     * 跳转至单聊页面
     *
     * @param context
     * @param intent
     */
    private void gotoSingleChatActivity(Context context, Intent intent) {
//        Intent startActivityIntent = new Intent(context, AVSingleChatActivity.class);
//        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivityIntent.putExtra(Constants.MEMBER_ID, intent.getStringExtra(Constants.MEMBER_ID));
//        context.startActivity(startActivityIntent);
    }
}
