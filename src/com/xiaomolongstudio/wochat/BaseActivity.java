package com.xiaomolongstudio.wochat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.xiaomolongstudio.wochat.service.XMPPService;

public class BaseActivity extends FragmentActivity {
	ServiceConnection serviceConnection;
	public static ArrayList<BackPressHandler> mListeners = new ArrayList<BackPressHandler>();

	@Override
	protected void onResume() {
		super.onResume();
		if (mListeners.size() > 0)
			for (BackPressHandler handler : mListeners) {
				handler.activityOnResume();
			}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mListeners.size() > 0)
			for (BackPressHandler handler : mListeners) {
				handler.activityOnPause();
			}
	}

	protected void onDestroy() {
		Log.i("wxl", "onDestroy");
		unbindXMPPService();

		super.onDestroy();
	}

	/**
	 * 解绑服务
	 */
	private void unbindXMPPService() {
		try {
			Log.i("wxl", "[SERVICE] Unbind");
			unbindService(serviceConnection);
		} catch (IllegalArgumentException e) {
			Log.e("wxl", "Service wasn't bound!");
		}
	}

	/**
	 * 绑定服务
	 */
	public void bindXMPPService(String action,
			ServiceConnection serviceConnection) {
		this.serviceConnection = serviceConnection;
		Log.i("wxl", "[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XMPPService.class);
		mServiceIntent.setAction(action);
		bindService(new Intent(this, XMPPService.class), serviceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	public static abstract interface BackPressHandler {

		public abstract void activityOnResume();

		public abstract void activityOnPause();

	}
}
