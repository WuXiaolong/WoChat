package com.xiaomolongstudio.wochat.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

public class AddFriendsActivity extends BaseActionActivity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		setContentView(R.layout.add_friends);
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				agree();

			}
		});
	}

	private void agree() {
		Toast.makeText(this, "同意", Toast.LENGTH_LONG).show();
		String fromJid = AddFriendsActivity.this.getIntent().getStringExtra(
				"fromJid");
		String fromNickName = AddFriendsActivity.this.getIntent()
				.getStringExtra("fromNickName");
		Log.i("wxl", "fromJid=" + fromJid);
		Log.i("wxl", "fromNickName=" + fromNickName);
//		mXMPPService.createEntry(fromJid, fromNickName);
		mXMPPService.agree(fromJid);
	}

	private XMPPService mXMPPService;
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
								Log.i("wxl", "CONNECTED");
								break;
							case XMPPService.CONNECTING:
								Log.i("wxl", "CONNECTING");

								break;
							case XMPPService.DISCONNECTED:
								// Toast.makeText(ChatRoomActivity.this, "用户掉线",
								// Toast.LENGTH_LONG).show();
								break;

							default:
								break;
							}

						}
					});
			// 开始连接xmpp服务器
			if (!mXMPPService.isAuthenticated()) {
				String userName = PreferenceUtils.getPrefString(
						AddFriendsActivity.this, PreferenceConstants.USER_NAME,
						"");
				String userPassword = PreferenceUtils.getPrefString(
						AddFriendsActivity.this, PreferenceConstants.PASSWORD,
						"");
				if (!TextUtils.isEmpty(userName)
						&& !TextUtils.isEmpty(userPassword))
					mXMPPService.login(userName, userPassword);
			}
			Log.i("wxl", "isAuthenticated=" + mXMPPService.isAuthenticated());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};
}
