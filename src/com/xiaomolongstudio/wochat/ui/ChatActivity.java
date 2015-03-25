package com.xiaomolongstudio.wochat.ui;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.xiaomolongstudio.wochat.ui.ChatRoomActivity.ChatRoomAdapter;
import com.xiaomolongstudio.wochat.ui.ChatRoomActivity.ChatRoomAdapter.ViewHolder;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.DateUtil;
import com.xiaomolongstudio.wochat.utils.IMMessage;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;

public class ChatActivity extends BaseActionActivity implements OnClickListener {
	EditText userName, password;
	Button btn_login;
	private List<IMMessage> iMMessageList = null;
	private static int pageSize = 10;
	protected String to;// 聊天人
	private EditText editText;
	private ListView listView;
	private ChatRoomAdapter chatRoomAdapter = null;
	private Map<String, Object> map = null;
	// private List<IMMessage> chatData;
	private android.os.Message msg;
	private Chat mChat = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.bindXMPPService(AppConfig.CHATROOM_ACTION, mServiceConnection);
		setContentView(R.layout.chat);
		to = getIntent().getStringExtra("to");
		Log.d("wxl", "to=" + to);
		initView();
		iMMessageList = MessageManager.getInstance(ChatActivity.this)
				.getMessageListByFrom(to, 1, pageSize);
	}

	@SuppressLint("NewApi")
	public void initView() {
		actionBar.setTitle("聊天室");
		listView = (ListView) findViewById(R.id.listView);
		editText = (EditText) findViewById(R.id.editText);
		findViewById(R.id.btn_send).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_send:

			if (editText.getText().toString().length() > 0) {
				// sendMessage(editText.getText().toString());

				String time = DateUtil.date2Str(Calendar.getInstance(),
						AppConfig.MS_FORMART);
				Message message = new Message();
				message.setProperty(IMMessage.KEY_TIME, time);
				message.setBody(editText.getText().toString());
				try {
					mChat.sendMessage(message);

				} catch (Exception e) {
					e.printStackTrace();
				}

				// IMMessage newMessage = new IMMessage();
				// newMessage.setMsgFrom(mChat.getParticipant());
				// newMessage.setChatContent(editText.getText().toString());
				// newMessage.setMsgTime(time);
				// iMMessageList.add(newMessage);
				// MessageManager.getInstance(ChatActivity.this).saveIMMessage(
				// newMessage);
				//
				// if (chatRoomAdapter == null) {
				// chatRoomAdapter = new ChatRoomAdapter(iMMessageList);
				// listView.setAdapter(chatRoomAdapter);
				// } else {
				// chatRoomAdapter.getmChatData().addAll(iMMessageList);
				// chatRoomAdapter.notifyDataSetChanged();
				// }
				//
				// editText.setText("");
			}

			break;

		default:
			break;
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
				convertView = LayoutInflater.from(ChatActivity.this).inflate(
						R.layout.chat_room_item, null);
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
			String userName = PreferenceUtils.getPrefString(ChatActivity.this,
					PreferenceConstants.USER_NAME, "");
			if ((userName).equals(mChatData.get(position).getMsgFrom()
					.toString())) {
				viewHolder.rightLayout.setVisibility(View.VISIBLE);
				viewHolder.leftLayout.setVisibility(View.GONE);
				viewHolder.rightChatContent.setText(mChatData.get(position)
						.getChatContent().toString());
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
						.getChatContent().toString());
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
												.get(position).getMsgFrom()
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

	private void initChat() {
		mChat = mXMPPService.chat(to);
		mChat.addMessageListener(new MessageListener() {

			@Override
			public void processMessage(Chat chat, Message message) {
				Log.d("wxl", "body=" + message.getBody());
				IMMessage newMessage = new IMMessage();
				newMessage.setMsgFrom(message.getFrom());
				newMessage.setChatContent(message.getBody());
				newMessage.setMsgTime(message.getProperty(IMMessage.KEY_TIME)
						+ "");
				iMMessageList.add(newMessage);

				if (chatRoomAdapter == null) {
					chatRoomAdapter = new ChatRoomAdapter(iMMessageList);
					listView.setAdapter(chatRoomAdapter);
				} else {
					chatRoomAdapter.getmChatData().addAll(iMMessageList);
					chatRoomAdapter.notifyDataSetChanged();
				}

				editText.setText("");
			}
		});
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
								Toast.makeText(ChatActivity.this, "用户上线",
										Toast.LENGTH_LONG).show();
								// 重新加入房间
								// rejoin = true;
								// initRoom();
								break;
							case XMPPService.CONNECTING:
								Log.i("wxl", "CONNECTING");

								break;
							case XMPPService.DISCONNECTED:
								Toast.makeText(ChatActivity.this, "用户掉线",
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
						ChatActivity.this, PreferenceConstants.USER_NAME, "");
				String userPassword = PreferenceUtils.getPrefString(
						ChatActivity.this, PreferenceConstants.PASSWORD, "");
				if (!TextUtils.isEmpty(userName)
						&& !TextUtils.isEmpty(userPassword))
					mXMPPService.login(userName, userPassword);
			}
			Log.i("wxl", "isAuthenticated=" + mXMPPService.isAuthenticated());
			// initRoom();
			initChat();
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
}
