package com.wuxiaolong.wochat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class XMPPBroadcastReceiver extends BroadcastReceiver {
    public static final String BOOT_COMPLETED_ACTION = "com.way.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        Log.i("wxl", "XMPPBroadcastReceiver action = " + action);

        /**
         * 网络变化监听
         */
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
//            Toast.makeText(context, "CONNECTIVITY_ACTION！", Toast.LENGTH_SHORT).show();
//            if (mListeners.size() > 0)// 通知接口完成加载
//                for (EventHandler handler : mListeners) {
//                    handler.onNetChange();
//                }
//            mOnEventHandlerListener.onNetChange();
        }
        /**
         * 关机监听
         */
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            Log.i("wxl", "System shutdown, stopping service.");
//            Intent xmppServiceIntent = new Intent(context, XMPPService.class);
//            context.stopService(xmppServiceIntent);
        }
        /**
         * 开机监听
         */
        else {
//			 if (!TextUtils.isEmpty(PreferenceUtils.getPrefString(context,
//                     PreferenceConstants.PASSWORD, ""))
//                     && PreferenceUtils.getPrefBoolean(context,
//                     PreferenceConstants.AUTO_START, true)) {
//                 Intent i = new Intent(context, XMPPService.class);
//			i.setAction(BOOT_COMPLETED_ACTION);
//			context.startService(i);
            // }
        }
    }

    static OnEventHandlerListener mOnEventHandlerListener;

    public static void setOnEventHandlerListener(OnEventHandlerListener listener) {
        mOnEventHandlerListener = listener;
    }

    public interface OnEventHandlerListener {

        public void onNetChange();
    }
}
