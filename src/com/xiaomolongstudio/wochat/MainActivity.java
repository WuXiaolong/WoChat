package com.xiaomolongstudio.wochat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.xiaomolongstudio.wochat.XMPPBroadcastReceiver.EventHandler;
import com.xiaomolongstudio.wochat.service.XMPPService;

public class MainActivity extends Activity implements
		IConnectionStatusCallback, EventHandler {
	private XMPPService mXxService;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XMPPService.XXBinder) service).getService();
			mXxService.registerConnectionStatusCallback(MainActivity.this);
			// 开始连接xmpp服务器
			if (!mXxService.isAuthenticated()) {
				// String usr = PreferenceUtils.getPrefString(MainActivity.this,
				// PreferenceConstants.ACCOUNT, "");
				// String password = PreferenceUtils.getPrefString(
				// MainActivity.this, PreferenceConstants.PASSWORD, "");
				mXxService.Login("test007", "123456");
				mTitle.setText("掉线");
				// setStatusImage(false);
				// mTitleProgressBar.setVisibility(View.VISIBLE);
			} else {
				mTitle.setText("在线");
				// setStatusImage(true);
				// mTitleProgressBar.setVisibility(View.GONE);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};
	TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(MainActivity.this, XMPPService.class));
		setContentView(R.layout.activity_main);
		mTitle = (TextView) findViewById(R.id.textView1);
	}

	private void bindXMPPService() {
		L.i(LoginActivity.class, "[SERVICE] Unbind");
		bindService(new Intent(MainActivity.this, XMPPService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			L.i(LoginActivity.class, "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			L.e(LoginActivity.class, "Service wasn't bound!");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		bindXMPPService();
		XMPPBroadcastReceiver.mListeners.add(this);
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
		} else {
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unbindXMPPService();
		XMPPBroadcastReceiver.mListeners.remove(this);
	}

	@Override
	public void onNetChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		switch (connectedState) {
		case XMPPService.CONNECTED:
			mTitle.setText("CONNECTED");
			// mTitleNameView.setText(XMPPHelper.splitJidAndServer(PreferenceUtils
			// .getPrefString(MainActivity.this,
			// PreferenceConstants.ACCOUNT, "")));
			// mTitleProgressBar.setVisibility(View.GONE);
			// // mTitleStatusView.setVisibility(View.GONE);
			// setStatusImage(true);
			break;
		case XMPPService.CONNECTING:
			mTitle.setText("CONNECTING");
			// mTitleNameView.setText(R.string.login_prompt_msg);
			// mTitleProgressBar.setVisibility(View.VISIBLE);
			// mTitleStatusView.setVisibility(View.GONE);
			break;
		case XMPPService.DISCONNECTED:
			mTitle.setText("DISCONNECTED");
			// mTitleNameView.setText(R.string.login_prompt_no);
			// mTitleProgressBar.setVisibility(View.GONE);
			// mTitleStatusView.setVisibility(View.GONE);
			T.showLong(this, reason);
			break;

		default:
			break;
		}

	}
}
