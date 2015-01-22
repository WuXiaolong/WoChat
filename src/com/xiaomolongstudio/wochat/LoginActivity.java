package com.xiaomolongstudio.wochat;

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

public class LoginActivity extends BaseActivity {
	public static final String LOGIN_ACTION = "com.way.action.LOGIN";
	@SuppressWarnings("unused")
	private static final int LOGIN_OUT_TIME = 0;
	EditText userName, userPassword;
	Button btn_login;
	private XMPPService mXxService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this, XMPPService.class));
		super.bindXMPPService(AppConfig.LOGIN_ACTION, mServiceConnection);
		setContentView(R.layout.activity_login);
		userName = (EditText) findViewById(R.id.userName);
		userPassword = (EditText) findViewById(R.id.userPassword);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mXxService != null) {
					mXxService.login(userName.getText().toString(),
							userPassword.getText().toString());
				} else {
					Toast.makeText(getApplicationContext(),
							"mXxService = null", Toast.LENGTH_LONG).show();
				}

			}
		});
	}

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
								startActivity(new Intent(LoginActivity.this,
										MainActivity.class));
								finish();
							} else if (connectedState == XMPPService.DISCONNECTED)
								T.showLong(LoginActivity.this,
										getString(R.string.request_failed)
												+ reason);

						}
					});
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXxService.unRegisterConnectionStatusCallback();
			mXxService = null;
		}

	};

}
