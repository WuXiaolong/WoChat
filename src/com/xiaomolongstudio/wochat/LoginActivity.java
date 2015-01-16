package com.xiaomolongstudio.wochat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.service.XMPPService;

public class LoginActivity extends Activity {
	public static final String LOGIN_ACTION = "com.way.action.LOGIN";
	@SuppressWarnings("unused")
	private static final int LOGIN_OUT_TIME = 0;
	EditText userName, userPassword;
	Button btn_login;
	private XMPPService mXxService;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXxService = ((XMPPService.XXBinder) service).getService();
			mXxService
					.registerConnectionStatusCallback(new IConnectionStatusCallback() {

						@Override
						public void connectionStatusChanged(int connectedState,
								String reason) {
							if (connectedState == XMPPService.CONNECTED) {
								// save2Preferences();
								startActivity(new Intent(LoginActivity.this,
										MainActivity.class));
								finish();
							} else if (connectedState == XMPPService.DISCONNECTED)
								T.showLong(LoginActivity.this,
										getString(R.string.request_failed)
												+ reason);

						}
					});
			// 开始连接xmpp服务器
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindXMPPService();
		setContentView(R.layout.activity_login);
		userName = (EditText) findViewById(R.id.userName);
		userPassword = (EditText) findViewById(R.id.userPassword);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mXxService != null) {
					mXxService.Login(userName.getText().toString(),
							userPassword.getText().toString());
				} else {
					Toast.makeText(getApplicationContext(),
							"mXxService = null", Toast.LENGTH_LONG).show();
				}

			}
		});
	}

	private void bindXMPPService() {
		L.i(LoginActivity.class, "[SERVICE] Unbind");
		Intent mServiceIntent = new Intent(this, XMPPService.class);
		mServiceIntent.setAction(LOGIN_ACTION);
		bindService(mServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void unbindXMPPService() {
		try {
			unbindService(mServiceConnection);
			L.i(LoginActivity.class, "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			L.e(LoginActivity.class, "Service wasn't bound!");
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindXMPPService();
	}
}
