package com.xiaomolongstudio.wochat.xmpp;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.L;

public class XMPPBroadcastReceiver extends BroadcastReceiver {
	public static final String BOOT_COMPLETED_ACTION = "com.way.action.BOOT_COMPLETED";
	public static ArrayList<EventHandler> mListeners = new ArrayList<EventHandler>();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		L.i("action = " + action);
		/**
		 * 网络变化监听
		 */
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
			if (mListeners.size() > 0)// 通知接口完成加载
				for (EventHandler handler : mListeners) {
					handler.onNetChange();
				}
		}
		/**
		 * 关机监听
		 */
		else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			L.d("System shutdown, stopping service.");
			Intent xmppServiceIntent = new Intent(context, XMPPService.class);
			context.stopService(xmppServiceIntent);
		}
		/**
		 * 开机监听
		 */
		else {
			// if (!TextUtils.isEmpty(PreferenceUtils.getPrefString(context,
			// PreferenceConstants.PASSWORD, ""))
			// && PreferenceUtils.getPrefBoolean(context,
			// PreferenceConstants.AUTO_START, true)) {
			Intent i = new Intent(context, XMPPService.class);
			i.setAction(BOOT_COMPLETED_ACTION);
			context.startService(i);
			// }
		}
	}

	public static abstract interface EventHandler {

		public abstract void onNetChange();
	}
}
