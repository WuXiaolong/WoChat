package com.xiaomolongstudio.wochat;

import java.util.ArrayList;

import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {
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

	public static abstract interface BackPressHandler {

		public abstract void activityOnResume();

		public abstract void activityOnPause();

	}
}
