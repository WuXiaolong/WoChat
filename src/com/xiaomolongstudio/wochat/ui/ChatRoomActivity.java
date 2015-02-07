package com.xiaomolongstudio.wochat.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.packet.VCard;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.db.MessageManager;
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.IMMessage;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

@SuppressLint("SimpleDateFormat")
public class ChatRoomActivity extends BaseActionActivity implements
		OnClickListener {
	private MultiUserChat mMultiUserChat = null;
	private String roomName = "cntv";
	private EditText editText;
	private ListView listView;
	private ChatRoomAdapter chatRoomAdapter = null;
	private Map<String, Object> map = null;
	private List<IMMessage> chatData;
	private android.os.Message msg;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		this.setContentView(R.layout.chat_room);
		initView();

	}

	@SuppressLint("NewApi")
	public void initView() {
		actionBar.setTitle("聊天室");
		listView = (ListView) findViewById(R.id.listView);
		editText = (EditText) findViewById(R.id.editText);
		findViewById(R.id.btn_send).setOnClickListener(this);
	}

	/**
	 * 初始化聊天室
	 */
	public void initRoom() {
		roomName = this.getIntent().getStringExtra("flag");
		Log.d("wxl", "mXMPPService=" + mXMPPService);
		String userName = PreferenceUtils.getPrefString(ChatRoomActivity.this,
				PreferenceConstants.USER_NAME, "");
		String userPassword = PreferenceUtils.getPrefString(
				ChatRoomActivity.this, PreferenceConstants.PASSWORD, "");
		mMultiUserChat = mXMPPService.createOrJoinRoom(userName, userPassword,
				roomName);
		if (mMultiUserChat == null) {
			Toast.makeText(ChatRoomActivity.this, "进入聊天室失败", Toast.LENGTH_LONG)
					.show();
			return;
		}
		try {
			mMultiUserChat.addMessageListener(new CustomPacketListener());
			mMultiUserChat
					.addParticipantStatusListener(new ParticipantStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void sendMessage(String messageStr) {
		try {
			mMultiUserChat.sendMessage(messageStr);
			editText.setText("");
			rejoin = false;
		} catch (Exception e) {
			Toast.makeText(ChatRoomActivity.this, "发送失败", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
	}

	class ChatRoomAdapter extends BaseAdapter {
		List<IMMessage> mChatData;
		ViewHolder viewHolder;

		public List<IMMessage> getmChatData() {
			return mChatData;
		}

		public void setmChatData(List<IMMessage> mChatData) {
			this.mChatData = mChatData;
		}

		ChatRoomAdapter(List<IMMessage> mChatData) {
			this.mChatData = mChatData;
		}

		@Override
		public int getCount() {
			return mChatData.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "InflateParams", "NewApi" })
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(ChatRoomActivity.this)
						.inflate(R.layout.chat_room_item, null);
				viewHolder.leftChatContent = (TextView) convertView
						.findViewById(R.id.leftChatContent);
				viewHolder.rightChatContent = (TextView) convertView
						.findViewById(R.id.rightChatContent);
				viewHolder.leftUserImg = (ImageView) convertView
						.findViewById(R.id.leftUserImg);
				viewHolder.rightUserImg = (ImageView) convertView
						.findViewById(R.id.rightUserImg);
				viewHolder.leftLayout = (LinearLayout) convertView
						.findViewById(R.id.leftLayout);
				viewHolder.rightLayout = (RelativeLayout) convertView
						.findViewById(R.id.rightLayout);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			String userName = PreferenceUtils.getPrefString(
					ChatRoomActivity.this, PreferenceConstants.USER_NAME, "");
			if ((userName).equals(mChatData.get(position).getMsg_from()
					.toString())) {
				viewHolder.rightLayout.setVisibility(View.VISIBLE);
				viewHolder.leftLayout.setVisibility(View.GONE);
				viewHolder.rightChatContent.setText(mChatData.get(position)
						.getChat_content().toString());
				if (mChatData.get(position).getAvatar() == null) {
					viewHolder.rightUserImg
							.setBackgroundResource(R.drawable.ic_launcher);
				} else {
					// viewHolder.rightUserImg.setBackground((Drawable)
					// mChatData
					// .get(position).getAvatar());
				}
				viewHolder.rightUserImg
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// Intent intent = new Intent(
								// ChatRoomActivity.this,
								// FriendMsgActivity.class);
								// intent.putExtra("form",
								// mChatData.get(position)
								// .get("form").toString());
								// startActivity(intent);

							}
						});
			} else {
				viewHolder.rightLayout.setVisibility(View.GONE);
				viewHolder.leftLayout.setVisibility(View.VISIBLE);
				viewHolder.leftChatContent.setText(mChatData.get(position)
						.getChat_content().toString());
				if (mChatData.get(position).getAvatar() == null) {
					viewHolder.leftUserImg
							.setBackgroundResource(R.drawable.ic_launcher);
				} else {
					// viewHolder.leftUserImg.setBackground((Drawable) mChatData
					// .get(position).getAvatar());
				}
				viewHolder.leftUserImg
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// Intent intent = new Intent(
								// ChatRoomActivity.this,
								// FriendMsgActivity.class);
								// intent.putExtra("form",
								// mChatData.get(position)
								// .get("form").toString());
								// startActivity(intent);
								Toast.makeText(getApplicationContext(),
										"好友请求已发出", Toast.LENGTH_LONG).show();
								mXMPPService.createEntry(
										mXMPPService.getUserJid(mChatData
												.get(position).getMsg_from()
												.toString()), "nickname");
							}
						});
			}
			return convertView;
		}

		class ViewHolder {
			TextView leftChatContent, rightChatContent;
			ImageView leftUserImg, rightUserImg;
			LinearLayout leftLayout;
			RelativeLayout rightLayout;
		}
	}

	/**
	 * 会议室状态监听事件（成员的进入、离开等）
	 * 
	 * @author Administrator
	 * 
	 */
	class ParticipantStatus implements ParticipantStatusListener {

		@Override
		public void adminGranted(String arg0) {

		}

		@Override
		public void adminRevoked(String arg0) {

		}

		@Override
		public void banned(String arg0, String arg1, String arg2) {

		}

		@Override
		public void joined(String participant) {
			Log.d("wxl", StringUtils.parseResource(participant)
					+ " has joined the room.");
		}

		@Override
		public void kicked(String arg0, String arg1, String arg2) {

		}

		@Override
		public void left(String participant) {
			Log.d("wxl", StringUtils.parseResource(participant)
					+ " has leave the room.");

		}

		@Override
		public void membershipGranted(String arg0) {

		}

		@Override
		public void membershipRevoked(String arg0) {

		}

		@Override
		public void moderatorGranted(String arg0) {

		}

		@Override
		public void moderatorRevoked(String arg0) {

		}

		@Override
		public void nicknameChanged(String participant, String newNickname) {
			System.out.println(StringUtils.parseResource(participant)
					+ " is now known as " + newNickname + ".");
		}

		@Override
		public void ownershipGranted(String arg0) {

		}

		@Override
		public void ownershipRevoked(String arg0) {

		}

		@Override
		public void voiceGranted(String arg0) {

		}

		@Override
		public void voiceRevoked(String arg0) {

		}

	}

	/**
	 * 查询会议室成员名字
	 * 
	 * @param muc
	 */
	public static List<String> findMulitUser(MultiUserChat muc) {
		List<String> listUser = new ArrayList<String>();
		Iterator<String> it = muc.getOccupants();
		// 遍历出聊天室人员名称
		while (it.hasNext()) {
			// 聊天室成员名字
			String name = StringUtils.parseResource(it.next());
			listUser.add(name);
		}
		return listUser;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("wxl", "onDestroy");
		if (mMultiUserChat != null) {

			mMultiUserChat.removeMessageListener(new CustomPacketListener());
			mMultiUserChat
					.removeParticipantStatusListener(new ParticipantStatus());
			mMultiUserChat.leave();
		}

	}

	private boolean rejoin = false;
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
								Toast.makeText(ChatRoomActivity.this, "用户上线",
										Toast.LENGTH_LONG).show();
								// 重新加入房间
								rejoin = true;
								initRoom();
								break;
							case XMPPService.CONNECTING:
								Log.i("wxl", "CONNECTING");

								break;
							case XMPPService.DISCONNECTED:
								Toast.makeText(ChatRoomActivity.this, "用户掉线",
										Toast.LENGTH_LONG).show();
								Log.i("wxl", "DISCONNECTED");
								Log.i("wxl", "reason=" + reason);
								break;

							default:
								break;
							}

						}
					});
			// 开始连接xmpp服务器
			if (!mXMPPService.isAuthenticated()) {
				String userName = PreferenceUtils.getPrefString(
						ChatRoomActivity.this, PreferenceConstants.USER_NAME,
						"");
				String userPassword = PreferenceUtils
						.getPrefString(ChatRoomActivity.this,
								PreferenceConstants.PASSWORD, "");
				if (!TextUtils.isEmpty(userName)
						&& !TextUtils.isEmpty(userPassword))
					mXMPPService.login(userName, userPassword);
			}
			Log.i("wxl", "isAuthenticated=" + mXMPPService.isAuthenticated());
			initRoom();
			// List<IMMessage> messageList = MessageManager.getInstance(
			// ChatRoomActivity.this).getMessageListByroomId(roomName, 1,
			// 10);
			// for (int i = 0; i < messageList.size(); i++) {
			// Log.d("wxl",
			// "messageList------------------"
			// + messageList.get(i).getChat_content());
			// }

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};

	/**
	 * 会议室消息监听类
	 * 
	 * @author Administrator
	 * 
	 */

	public class CustomPacketListener implements PacketListener {

		@Override
		public void processPacket(final Packet packet) {
			if (!rejoin) {
				Message message = (Message) packet;
				String msgForm = message.getFrom();
				int index = msgForm.indexOf("/");
				String form = msgForm.substring(index + 1, msgForm.length());
				Log.d("wxl", "CustomPacketListener-------Body-----------"
						+ message.getBody());
				try {
					VCard vCard = mXMPPService.vCard(form);
					IMMessage iMMessage = new IMMessage();
					iMMessage.setAvatar(vCard.getAvatar());
					// iMMessage.setAvatar(mXMPPService.getUserImage(form));
					try {
						iMMessage.setNickname(vCard.getNickName());
					} catch (Exception e) {
						iMMessage.setNickname(form);
					}
					iMMessage.setRoomId(roomName);
					iMMessage.setMsg_from(form);
					iMMessage.setChat_content(message.getBody());
					String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date());
					iMMessage.setMsg_time(time);
					msg = new android.os.Message();
					msg.what = AppConfig.CHAT_ROOM_MESSAGE;
					msg.obj = iMMessage;
					handler.sendMessage(msg);
				} catch (Exception e) {
				}
			}
			// 接收来自聊天室的聊天信息
			// String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			// .format(new Date());
			// MucHistory mh = new MucHistory();
			// mh.setUserAccount(account);
			// String from = StringUtils.parseResource(message.getFrom());
			// String fromRoomName = StringUtils.parseName(message.getFrom());
			// mh.setMhRoomName(fromRoomName);
			// mh.setFriendAccount(from);
			// mh.setMhInfo(message.getBody());
			// mh.setMhTime(time);
			// mh.setMhType("left");

		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case AppConfig.CHAT_ROOM_MESSAGE:
				chatData = new ArrayList<IMMessage>();
				map = new HashMap<String, Object>();
				IMMessage iMMessage = (IMMessage) msg.obj;

				map.put("form", iMMessage.getMsg_from());
				map.put("avatar", iMMessage.getAvatar());
				map.put("chatContent", iMMessage.getChat_content());
				chatData.add(iMMessage);

				// MessageManager.getInstance(ChatRoomActivity.this)
				// .saveIMMessage(iMMessage);

				if (chatRoomAdapter == null) {
					chatRoomAdapter = new ChatRoomAdapter(chatData);
					listView.setAdapter(chatRoomAdapter);
				} else {
					chatRoomAdapter.getmChatData().addAll(chatData);
					chatRoomAdapter.notifyDataSetChanged();
				}
				listView.setSelection(listView.getCount() - 1);

				break;
			}
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:

			if (editText.getText().toString().length() > 0) {
				sendMessage(editText.getText().toString());
			}

			break;

		default:
			break;
		}

	}

}
