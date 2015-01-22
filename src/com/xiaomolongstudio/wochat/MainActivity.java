package com.xiaomolongstudio.wochat;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.XMPPBroadcastReceiver.EventHandler;
import com.xiaomolongstudio.wochat.service.XMPPService;

public class MainActivity extends BaseActivity implements EventHandler {
	private XMPPService mXMPPService = null;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mXMPPService = ((XMPPService.XXBinder) service).getService();
			mXMPPService
					.registerConnectionStatusCallback(new IConnectionStatusCallback() {

						@Override
						public void connectionStatusChanged(int connectedState,
								String reason) {

							switch (connectedState) {
							case XMPPService.CONNECTED:
								mTitle.setText("CONNECTED");
								break;
							case XMPPService.CONNECTING:
								mTitle.setText("CONNECTING");
								break;
							case XMPPService.DISCONNECTED:
								mTitle.setText("DISCONNECTED");
								Toast.makeText(MainActivity.this, reason,
										Toast.LENGTH_SHORT).show();
								break;

							default:
								break;
							}

						}
					});
			// 开始连接xmpp服务器
			if (!mXMPPService.isAuthenticated()) {
				mXMPPService.login("test007", "123456");
				mTitle.setText("掉线");
			} else {
				mTitle.setText("在线");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};
	TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.MAIN_ACTION, mServiceConnection);
		setContentView(R.layout.activity_main);
		mTitle = (TextView) findViewById(R.id.textView1);
		final EditText editText1 = (EditText) findViewById(R.id.editText1);
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mXMPPService != null) {
					Log.d("wxl", "添加");
					mXMPPService.addUser(editText1.getText().toString(), "昵称");
				} else {
					Log.d("wxl", "mXMPPService null");
				}

			}
		});
		findViewById(R.id.button2).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mXMPPService != null) {
				} else {
				}

			}
		});
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
	public void onNetChange() {
		// TODO Auto-generated method stub

	}

}
