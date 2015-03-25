package com.xiaomolongstudio.wochat.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.adapter.AddFriendsAdapter;
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

public class AddFriendsActivity extends BaseActionActivity {
	private ListView mListView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		setContentView(R.layout.add_friends);
		mListView = (ListView) findViewById(R.id.mListView);
	}

	private List<Map<String, Object>> friendsList;

	public void initAddFriends() {
		if (mXMPPService != null) {
			friendsList = new ArrayList<Map<String, Object>>();
			Roster roster = mXMPPService.roster();

			Collection<RosterEntry> it = roster.getEntries();
			Map<String, Object> map;
			for (RosterEntry rosterEnter : it) {
				if (rosterEnter.getType() == ItemType.from) {
					map = new HashMap<String, Object>();
					map.put("useID", rosterEnter.getUser().split("@")[0]);
					map.put("fromJid", rosterEnter.getUser());
					if (rosterEnter.getName() == null) {
						map.put("nickName", rosterEnter.getUser().split("@")[0]);
					} else {
						map.put("nickName", rosterEnter.getName());
					}
					friendsList.add(map);
				}

			}
			if (friendsList.size() > 0) {
				AddFriendsAdapter addFriendsAdapter = new AddFriendsAdapter(
						AddFriendsActivity.this, friendsList, mXMPPService);
				mListView.setAdapter(addFriendsAdapter);
			}
		}
	}

	private void agree() {
		Toast.makeText(this, "同意", Toast.LENGTH_LONG).show();
		String fromJid = AddFriendsActivity.this.getIntent().getStringExtra(
				"fromJid");
		String fromNickName = AddFriendsActivity.this.getIntent()
				.getStringExtra("fromNickName");
		Log.i("wxl", "fromJid=" + fromJid);
		Log.i("wxl", "fromNickName=" + fromNickName);
		String groupName = "Friends";
		mXMPPService.createEntry(fromJid, fromNickName);
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
			initAddFriends();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};
}
