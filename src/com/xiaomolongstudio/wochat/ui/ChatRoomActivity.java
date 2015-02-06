package com.xiaomolongstudio.wochat.ui;

import java.util.ArrayList;
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
import android.graphics.drawable.Drawable;
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
import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.IMMessage;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

public class ChatRoomActivity extends BaseActionActivity implements
		OnClickListener {
	private MultiUserChat mMultiUserChat = null;
	private String roomName = "cntv";
	private EditText editText;
	private ListView listView;
	private ChatRoomAdapter chatRoomAdapter = null;
	private Map<String, Object> map = null;
	private List<IMMessage> chatData = new ArrayList<IMMessage>();
	private android.os.Message msg;
	// private IDanmakuView mDanmakuView;
	private boolean isDanMu = true;

	// private BaseDanmakuParser mParser;

	@SuppressLint("HandlerLeak")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		this.setContentView(R.layout.chat_room);
		initView();
		// initDanmaku();
	}

	@SuppressLint("NewApi")
	public void initView() {
		actionBar.setTitle("聊天室");
		listView = (ListView) findViewById(R.id.listView);
		editText = (EditText) findViewById(R.id.editText);
		// mDanmakuView = (DanmakuSurfaceView) findViewById(R.id.sv_danmaku);
		findViewById(R.id.btn_send).setOnClickListener(this);

	}

	// private void initDanmaku() {
	// if (mDanmakuView != null) {
	// mParser = createParser(this.getResources().openRawResource(
	// R.raw.comments));
	// mDanmakuView.setCallback(new Callback() {
	//
	// public void updateTimer(DanmakuTimer timer) {
	//
	// }
	//
	// public void prepared() {
	// mDanmakuView.start();
	// }
	// });
	// mDanmakuView.prepare(mParser);
	// mDanmakuView.showFPS(true);
	// mDanmakuView.enableDanmakuDrawingCache(true);
	// }
	// }
	//
	// private BaseDanmakuParser createParser(InputStream stream) {
	//
	// if (stream == null) {
	// return new BaseDanmakuParser() {
	//
	// @Override
	// protected Danmakus parse() {
	// return new Danmakus();
	// }
	// };
	// }
	//
	// ILoader loader = DanmakuLoaderFactory
	// .create(DanmakuLoaderFactory.TAG_BILI);
	//
	// try {
	// loader.load(stream);
	// } catch (IllegalDataException e) {
	// e.printStackTrace();
	// }
	// BaseDanmakuParser parser = new BiliDanmukuParser();
	// IDataSource<?> dataSource = loader.getDataSource();
	// parser.load(dataSource);
	// return parser;
	//
	// }

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
			// String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			// .format(new Date());
			// Message message = new Message();
			// message.setProperty(AppConfig.KEY_TIME, "111");
			// message.setBody(messageStr);
			mMultiUserChat.sendMessage(messageStr);
			editText.setText("");
		} catch (Exception e) {
			Toast.makeText(ChatRoomActivity.this, "发送失败", Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
	}

	/**
	 * 会议室消息监听类
	 * 
	 * @author Administrator
	 * 
	 */

	public class CustomPacketListener implements PacketListener {

		@Override
		public void processPacket(final Packet packet) {
			Message message = (Message) packet;
			String msgForm = message.getFrom();
			int index = msgForm.indexOf("/");
			String form = msgForm.substring(index + 1, msgForm.length());
			try {
				VCard vCard = mXMPPService.vCard(form);
				IMMessage iMMessage = new IMMessage();
				iMMessage.setAvatar(mXMPPService.getUserImage(form));
				try {
					iMMessage.setNickname(vCard.getNickName());
				} catch (Exception e) {
					iMMessage.setNickname(form);
				}
				iMMessage.setMsg_from(form);
				iMMessage.setChat_content(message.getBody());

				msg = new android.os.Message();
				msg.what = AppConfig.CHAT_ROOM_MESSAGE;
				msg.obj = iMMessage;
				handler.sendMessage(msg);
			} catch (Exception e) {
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
					viewHolder.rightUserImg.setBackground((Drawable) mChatData
							.get(position).getAvatar());
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
					viewHolder.leftUserImg.setBackground((Drawable) mChatData
							.get(position).getAvatar());
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
								Log.d("wxl",
										"Msg_from="
												+ mChatData.get(position)
														.getMsg_from()
														.toString());
								mXMPPService.createEntry(
										mXMPPService.getUserJid(mChatData
												.get(position).getMsg_from()
												.toString()), "nnnn");
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
	protected void onPause() {
		super.onPause();

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

		// if (mDanmakuView != null) {
		// // dont forget release!
		// mDanmakuView.release();
		// }
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
								Toast.makeText(ChatRoomActivity.this, "用户上线",
										Toast.LENGTH_LONG).show();
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
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mXMPPService.unRegisterConnectionStatusCallback();
			mXMPPService = null;
		}

	};
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case AppConfig.CHAT_ROOM_MESSAGE:
				// chatData = new ArrayList<Map<String, Object>>();
				map = new HashMap<String, Object>();
				IMMessage iMMessage = (IMMessage) msg.obj;

				map.put("form", iMMessage.getMsg_from());
				map.put("avatar", iMMessage.getAvatar());
				map.put("chatContent", iMMessage.getChat_content());
				chatData.add(iMMessage);

				if (chatRoomAdapter == null) {
					chatRoomAdapter = new ChatRoomAdapter(chatData);
					listView.setAdapter(chatRoomAdapter);
				} else {
					chatRoomAdapter.getmChatData().addAll(chatData);
					chatRoomAdapter.notifyDataSetChanged();
				}
				listView.setSelection(listView.getCount() - 1);

				/**
				 * 弹幕
				 */
				// if (mDanmakuView == null || !mDanmakuView.isPrepared())
				// return;
				// BaseDanmaku danmaku = DanmakuFactory
				// .createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
				//
				// danmaku.text = iMMessage.getChat_content();
				// danmaku.padding = 5;
				// danmaku.priority = 1;
				// danmaku.time = mDanmakuView.getCurrentTime() + 200;
				// danmaku.textSize = 25f * (mParser.getDisplayer().getDensity()
				// - 0.6f);
				// danmaku.textColor = Color.BLUE;
				// danmaku.textShadowColor = Color.WHITE;
				// // danmaku.underlineColor = Color.GREEN;
				// // danmaku.borderColor = Color.GREEN;
				// mDanmakuView.addDanmaku(danmaku);

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
		// case R.id.right_img:
		// // setRequestedOrientation(getRequestedOrientation() ==
		// // ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ?
		// // ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		// // : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// if (isDanMu == true) {
		// listView.setVisibility(View.VISIBLE);
		// mDanmakuView.setVisibility(View.GONE);
		//
		// } else {
		// listView.setVisibility(View.GONE);
		// mDanmakuView.setVisibility(View.VISIBLE);
		// }
		// isDanMu = !isDanMu;
		// break;

		default:
			break;
		}

	}

}
