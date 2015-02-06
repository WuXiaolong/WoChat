package com.xiaomolongstudio.wochat.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.smack.SmackImpl;
import com.xiaomolongstudio.wochat.ui.AddFriendsActivity;
import com.xiaomolongstudio.wochat.ui.BaseActivity;
import com.xiaomolongstudio.wochat.ui.BaseActivity.BackPressHandler;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.AppUtils;
import com.xiaomolongstudio.wochat.utils.FormatTools;
import com.xiaomolongstudio.wochat.utils.L;
import com.xiaomolongstudio.wochat.utils.NetUtil;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;
import com.xiaomolongstudio.wochat.xmpp.XMPPBroadcastReceiver;
import com.xiaomolongstudio.wochat.xmpp.XMPPBroadcastReceiver.EventHandler;
import com.xiaomolongstudio.wochat.xmpp.XXException;
//connection.getAccountManager().createAccount(username, password);  //创建一个用户  
//http://blog.csdn.net/u013339223/article/details/41241771

public class XMPPService extends BaseService implements EventHandler,
		BackPressHandler {
	public static final int CONNECTED = 0;
	public static final int DISCONNECTED = -1;
	public static final int CONNECTING = 1;
	public static final String PONG_TIMEOUT = "pong timeout";// 连接超时
	public static final String NETWORK_ERROR = "network error";// 网络错误
	public static final String LOGOUT = "logout";// 手动退出
	public static final String LOGIN_FAILED = "login failed";// 登录失败
	public static final String DISCONNECTED_WITHOUT_WARNING = "disconnected without warning";// 没有警告的断开连接

	private IBinder mBinder = new XXBinder();
	private IConnectionStatusCallback mConnectionStatusCallback;
	private SmackImpl mSmackImpl;
	private Thread mConnectingThread;
	private Handler mMainHandler = new Handler();

	private boolean mIsFirstLoginAction;
	// 自动重连 start
	private static final int RECONNECT_AFTER = 5;
	private static final int RECONNECT_MAXIMUM = 10 * 60;// 最大重连时间间隔
	private static final String RECONNECT_ALARM = "com.way.xx.RECONNECT_ALARM";
	// private boolean mIsNeedReConnection = false; // 是否需要重连
	private int mConnectedState = DISCONNECTED; // 是否已经连接
	private int mReconnectTimeout = RECONNECT_AFTER;
	private Intent mAlarmIntent = new Intent(RECONNECT_ALARM);
	private PendingIntent mPAlarmIntent;
	private BroadcastReceiver mAlarmReceiver = new ReconnectAlarmReceiver();
	// 自动重连 end
	private ActivityManager mActivityManager;
	private String mPackageName;
	private HashSet<String> mIsBoundTo = new HashSet<String>();

	/**
	 * 注册注解面和聊天界面时连接状态变化回调
	 * 
	 * @param cb
	 */
	public void registerConnectionStatusCallback(IConnectionStatusCallback cb) {
		mConnectionStatusCallback = cb;
	}

	public void unRegisterConnectionStatusCallback() {
		mConnectionStatusCallback = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		L.i(XMPPService.class, "[SERVICE] onBind");
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			mIsBoundTo.add(chatPartner);
		}
		String action = intent.getAction();
		if (!TextUtils.isEmpty(action)
				&& TextUtils.equals(action, AppConfig.LOGIN_ACTION)) {
			mIsFirstLoginAction = true;
		} else {
			mIsFirstLoginAction = false;
		}
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			mIsBoundTo.add(chatPartner);
		}
		String action = intent.getAction();
		if (!TextUtils.isEmpty(action)
				&& TextUtils.equals(action, AppConfig.LOGIN_ACTION)) {
			mIsFirstLoginAction = true;
		} else {
			mIsFirstLoginAction = false;
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		String chatPartner = intent.getDataString();
		if ((chatPartner != null)) {
			mIsBoundTo.remove(chatPartner);
		}
		return true;
	}

	public class XXBinder extends Binder {
		public XMPPService getService() {
			return XMPPService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		XMPPBroadcastReceiver.mListeners.add(this);
		BaseActivity.mListeners.add(this);
		mActivityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		mPackageName = getPackageName();
		mPAlarmIntent = PendingIntent.getBroadcast(this, 0, mAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		registerReceiver(mAlarmReceiver, new IntentFilter(RECONNECT_ALARM));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null
				&& intent.getAction() != null
				&& TextUtils.equals(intent.getAction(),
						XMPPBroadcastReceiver.BOOT_COMPLETED_ACTION)) {
			String userName = PreferenceUtils.getPrefString(XMPPService.this,
					PreferenceConstants.USER_NAME, "");
			String userPassword = PreferenceUtils.getPrefString(
					XMPPService.this, PreferenceConstants.PASSWORD, "");
			if (!TextUtils.isEmpty(userName)
					&& !TextUtils.isEmpty(userPassword))
				login(userName, userPassword);
		}
		mMainHandler.removeCallbacks(monitorStatus);
		mMainHandler.postDelayed(monitorStatus, 1000L);// 检查应用是否在后台运行线程
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		XMPPBroadcastReceiver.mListeners.remove(this);
		BaseActivity.mListeners.remove(this);
		((AlarmManager) getSystemService(Context.ALARM_SERVICE))
				.cancel(mPAlarmIntent);// 取消重连闹钟
		unregisterReceiver(mAlarmReceiver);// 注销广播监听
		logout();
	}

	// ping-pong服务器
	// String xmpp_host = "192.168.1.103";

	// String xmpp_host = "192.168.2.8";

	public static final String XMPP_IDENTITY_NAME = "xx";
	public static final String XMPP_IDENTITY_TYPE = "phone";
	private static final int PACKET_TIMEOUT = 30000;
	private ConnectionConfiguration mXMPPConfig;
	private XMPPConnection mXMPPConnection;
	// private XMPPService mService;
	private Roster mRoster;
	private PacketListener mPongListener;

	// ping-pong服务器
	private String mPingID;
	private long mPingTimestamp;
	private PendingIntent mPingAlarmPendIntent;
	private PendingIntent mPongTimeoutAlarmPendIntent;
	private static final String PING_ALARM = "com.way.xx.PING_ALARM";
	private static final String PONG_TIMEOUT_ALARM = "com.way.xx.PONG_TIMEOUT_ALARM";
	private Intent mPingAlarmIntent = new Intent(PING_ALARM);
	private Intent mPongTimeoutAlarmIntent = new Intent(PONG_TIMEOUT_ALARM);

	private PongTimeoutAlarmReceiver mPongTimeoutAlarmReceiver = new PongTimeoutAlarmReceiver();
	private BroadcastReceiver mPingAlarmReceiver = new PingAlarmReceiver();

	/**
	 * 初始化
	 */
	public void initXMPPConnection() {
		registerSmackProviders();
		this.mXMPPConfig = new ConnectionConfiguration(AppConfig.XMPP_HOST,
				AppConfig.XMPP_PORT, AppConfig.XMPP_SERVICE_NAME);

		this.mXMPPConfig.setReconnectionAllowed(true);
		this.mXMPPConfig.setSendPresence(true);
		this.mXMPPConfig.setCompressionEnabled(false); // disable for now
		this.mXMPPConfig.setDebuggerEnabled(true);
		this.mXMPPConfig
				.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
	}

	/**
	 * 登录
	 * 
	 * @param account
	 * @param password
	 * @return
	 */
	public boolean isLogin(String account, String password) {
		try {
			if (mXMPPConnection.isConnected()) {
				try {
					mXMPPConnection.disconnect();
				} catch (Exception e) {
					L.d("conn.disconnect() failed: " + e);
				}
			}
			SmackConfiguration.setPacketReplyTimeout(PACKET_TIMEOUT);
			SmackConfiguration.setKeepAliveInterval(-1);
			SmackConfiguration.setDefaultPingInterval(0);

			// registerPacketListener();
			mXMPPConnection.connect();
			if (!mXMPPConnection.isConnected()) {
				throw new XXException("SMACK connect failed without exception!");
			}
			mXMPPConnection.addConnectionListener(new ConnectionListener() {
				public void connectionClosedOnError(Exception e) {
					XMPPService.this.postConnectionFailed(e.getMessage());
				}

				public void connectionClosed() {
				}

				public void reconnectingIn(int seconds) {
				}

				public void reconnectionFailed(Exception e) {
				}

				public void reconnectionSuccessful() {
				}
			});
			// SMACK auto-logins if we were authenticated before
			if (!mXMPPConnection.isAuthenticated()) {
				// String ressource = PreferenceUtils.getPrefString(mService,
				// PreferenceConstants.RESSOURCE, "XX");
				mXMPPConnection.login(account, password);
			}
			setStatusFromConfig();// 更新在线状态

		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			L.e(SmackImpl.class, "login(): " + Log.getStackTraceString(e));
		}
		registerAllListener();// 注册监听其他的事件，比如新消息
		return mXMPPConnection.isAuthenticated();
	}

	/**
	 * 登录
	 * 
	 * @param userName
	 * @param password
	 */

	public void login(final String userName, final String password) {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
			connectionFailed(NETWORK_ERROR);
			return;
		}
		if (mConnectingThread != null) {
			L.i("a connection is still goign on!");
			return;
		}
		mConnectingThread = new Thread() {
			@Override
			public void run() {
				try {
					postConnecting();
					initXMPPConnection();
					if (isLogin(userName, password)) {
						// 登陆成功
						postConnectionScuessed();
					} else {
						// 登陆失败
						postConnectionFailed(LOGIN_FAILED);
					}
				} catch (Exception e) {
					String message = e.getLocalizedMessage();
					// 登陆失败
					if (e.getCause() != null)
						message += "\n" + e.getCause().getLocalizedMessage();
					postConnectionFailed(message);
					L.i(XMPPService.class, "YaximXMPPException in doConnect():");
					e.printStackTrace();
				} finally {
					if (mConnectingThread != null)
						synchronized (mConnectingThread) {
							mConnectingThread = null;
						}
				}
			}

		};
		mConnectingThread.start();
	}

	// 退出
	public boolean logout() {
		// mIsNeedReConnection = false;// 手动退出就不需要重连闹钟了
		boolean isLogout = false;
		if (mConnectingThread != null) {
			synchronized (mConnectingThread) {
				try {
					mConnectingThread.interrupt();
					mConnectingThread.join(50);
				} catch (InterruptedException e) {
					L.e("doDisconnect: failed catching connecting thread");
				} finally {
					mConnectingThread = null;
				}
			}
		}
		if (mSmackImpl != null) {
			isLogout = mSmackImpl.logout();
			mSmackImpl = null;
		}
		connectionFailed(LOGOUT);// 手动退出
		return isLogout;
	}

	/**
	 * 创建/进入聊天室
	 * 
	 * @param userName
	 * @param password
	 * @param roomName
	 * @return
	 */
	public MultiUserChat createOrJoinRoom(String userName, String password,
			String roomName) {
		Log.d("wxl", "isRoomExists==" + isRoomExists(roomName));
		if (isRoomExists(roomName)) {
			return joinMultiUserChat(userName, password, roomName);

		} else {
			if (createRoom(userName, password, roomName)) {
				return joinMultiUserChat(userName, password, roomName);
			} else {
				return null;
			}
		}

	}

	/**
	 * 创建房间
	 * 
	 * @param userName
	 * @param password
	 * @param roomName
	 * @return
	 */
	public boolean createRoom(String userName, String password, String roomName) {
		try {
			String roomService = roomName + "@conference."
					+ mXMPPConnection.getServiceName();// 房间域名

			MultiUserChat multiUserChat = new MultiUserChat(mXMPPConnection,
					roomService);
			// 创建聊天室
			multiUserChat.create(roomName);
			Log.d("wxl", "聊天室创建");
			// 获得聊天室的配置表单
			Form form = multiUserChat.getConfigurationForm();
			// 根据原始表单创建一个要提交的新表单。
			Form submitForm = form.createAnswerForm();
			// 向要提交的表单添加默认答复
			for (Iterator<?> fields = form.getFields(); fields.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					// 设置默认值作为答复
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			// 设置当前用户为聊天室的创建者
			List<String> roomOwner = new ArrayList<String>();
			roomOwner.add(mXMPPConnection.getUser());
			submitForm.setAnswer("muc#roomconfig_roomowners", roomOwner);

			submitForm.setAnswer("muc#roomconfig_persistentroom", true); // 持久聊天室
			submitForm.setAnswer("muc#roomconfig_membersonly", false); // 仅对成员开放
			submitForm.setAnswer("muc#roomconfig_allowinvites", true); // 允许邀请其他人

			submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);
			submitForm.setAnswer("muc#roomconfig_roomsecret", "password");

			submitForm.setAnswer("muc#roomconfig_enablelogging", true); // 登录房间对话
			submitForm.setAnswer("x-muc#roomconfig_reservednick", true); // 仅允许注册的昵称登录
			submitForm.setAnswer("x-muc#roomconfig_canchangenick", true); // 允许使用者修改昵称
			submitForm.setAnswer("x-muc#roomconfig_registration", true); // 允许用户注册房间
			List<String> list = new ArrayList<String>();
			list.add("0");
			submitForm.setAnswer("muc#roomconfig_maxusers", list); // 设置房间人数

			multiUserChat.sendConfigurationForm(submitForm);// 发送已完成的表单（有默认值）到服务器来配置聊天室
			Log.d("wxl", "讨论室:" + multiUserChat.getNickname());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断聊天室是否存在
	 * 
	 * @param roomName
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public boolean isRoomExists(String roomName) {
		new ServiceDiscoveryManager(mXMPPConnection);// 不加此行代码抛空指针
		Collection<HostedRoom> hostrooms;
		try {
			hostrooms = MultiUserChat.getHostedRooms(mXMPPConnection,
					"conference." + mXMPPConnection.getServiceName());
			for (HostedRoom entry : hostrooms) {
				if (TextUtils.equals(roomName.toLowerCase(), entry.getName())) {
					return true;
				}
				Log.i("wxl",
						"名字：" + entry.getName() + " - ID:" + entry.getJid());
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 加入聊天室
	 * 
	 * @param userName
	 * @param password
	 * @param roomName
	 * @return
	 */

	public MultiUserChat joinMultiUserChat(String userName, String password,
			String roomName) {
		String roomService = roomName + "@conference."
				+ mXMPPConnection.getServiceName();// 房间域名
		try {
			// // 使用XMPPConnection创建一个MultiUserChat窗口
			// 聊天室服务将会决定要接受的历史记录数量
			DiscussionHistory history = new DiscussionHistory();
			history.getMaxStanzas();
			// history.setSince(new Date());
			// history.getSince();

			MultiUserChat mMultiUserChat = new MultiUserChat(mXMPPConnection,
					roomService);
			mMultiUserChat.join(userName, password, history,
					SmackConfiguration.getPacketReplyTimeout());
			Log.i("wxl", userName + "会议室【" + roomName + "】加入成功........");
			return mMultiUserChat;
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("wxl", "异常:会议室【" + roomName + "】加入失败........");
			return null;
		}

	}

	public VCard vCard(String userId) throws Exception {
		VCard vCard = new VCard();
		// 加入这句代码，解决No VCard for
		ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
				new org.jivesoftware.smackx.provider.VCardProvider());
		vCard.load(mXMPPConnection,
				userId + "@" + mXMPPConnection.getServiceName());
		return vCard;
	}

	public VCard vCard() throws Exception {
		VCard vCard = new VCard();
		// 加入这句代码，解决No VCard for
		ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
				new org.jivesoftware.smackx.provider.VCardProvider());

		vCard.load(mXMPPConnection);
		return vCard;
	}

	/**
	 * 获取用户头像信息
	 * 
	 * @param user
	 * @return
	 */
	public Drawable getUserImage(String user) {
		ByteArrayInputStream bais = null;
		try {
			VCard vcard;
			if (TextUtils.equals(user, "")) {
				vcard = vCard();
			} else {
				vcard = vCard(user);
			}

			if (vcard == null || vcard.getAvatar() == null)
				return null;
			bais = new ByteArrayInputStream(vcard.getAvatar());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (bais == null)
			return null;

		return FormatTools.getInstance().InputStream2Drawable(bais);
	}

	/**
	 * 修改用户头像
	 * 
	 * @param connection
	 * @param f
	 * @throws XMPPException
	 * @throws IOException
	 */
	public void changeImage(File file) throws Exception {

		VCard vCard = vCard();

		byte[] bytes;

		bytes = FormatTools.getInstance().getBytesFromFile(file);
		String encodedImage = StringUtils.encodeBase64(bytes);
		vCard.setAvatar(bytes, encodedImage);
		vCard.setEncodedImage(encodedImage);
		vCard.setField("PHOTO", "<TYPE>image/jpg</TYPE><BINVAL>" + encodedImage
				+ "</BINVAL>", true);
		vCard.setNickName(vCard.getNickName());
		vCard.save(mXMPPConnection);
	}

	public void changeNickName(String nickName) throws Exception {

		VCard vCard = vCard();

		vCard.setNickName(nickName);
		vCard.setAvatar(vCard.getAvatar());
		vCard.save(mXMPPConnection);
	}

	// 发送消息
	// public void sendMessage(String user, String message) {
	// if (mSmackable != null)
	// mSmackable.sendMessage(user, message);
	// else
	// SmackImpl.sendOfflineMessage(getContentResolver(), user, message);
	// }

	// 是否连接上服务器
	public boolean isAuthenticated() {
		if (mXMPPConnection != null) {
			return (mXMPPConnection.isConnected() && mXMPPConnection
					.isAuthenticated());
		}
		return false;
	}

	/**
	 * a添加b
	 * 
	 * @param toUserName
	 * @param nickname
	 */

	public void createEntry(String userJid, String nickname) {
		try {

			Roster roster = mXMPPConnection.getRoster();
			// // 默认添加到【我的好友】分组
			String groupName = "Friends";
			roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
			roster.createEntry(userJid, nickname, new String[] { groupName });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(String toJid) {
		Presence presence = new Presence(Presence.Type.subscribed);

		String fromJid = mXMPPConnection.getUser().substring(0,
				mXMPPConnection.getUser().lastIndexOf("/"));
		// presence.setFrom(fromJid);
		presence.setTo(toJid);
		Log.d("wxl", "to=" + toJid);
		Log.d("wxl", "from=" + fromJid);
		mXMPPConnection.sendPacket(presence);//
		// connection是你自己的XMPPConnection链接
	}

	public void agree(String toJid) {
		Presence presence = new Presence(Presence.Type.subscribed);

		// String fromJid = mXMPPConnection.getUser().substring(0,
		// mXMPPConnection.getUser().lastIndexOf("/"));
		// presence.setFrom(fromJid);
		presence.setTo(toJid);
		Log.d("wxl", "to=" + toJid);
		// Log.d("wxl", "from=" + fromJid);
		mXMPPConnection.sendPacket(presence);//
		// connection是你自己的XMPPConnection链接
	}

	public void addSubscriptionListener() {
		PacketFilter packetFilter = new PacketTypeFilter(Presence.class);
		PacketListener subscribeListener = new PacketListener() {

			// 服务器返回给客户端的信息

			@Override
			public void processPacket(Packet packet) {

				Log.i("AllPacketListener", packet.toXML());

				if (packet instanceof org.jivesoftware.smack.packet.Presence) {// 好友相关

					Presence presence = (Presence) packet;

					// 登陆时判断好友是否在线

					if (presence.isAvailable()) {

						// 将在线好友添加进集合

					}

					org.jivesoftware.smack.packet.Presence.Type type = presence
							.getType();

					String from = presence.getFrom();
					Log.i("wxl", "addSubscriptionListener type:" + from
							+ "==============" + presence.getType());
					if (type.equals(Presence.Type.subscribe)) {// 好友申请
						showNotify(from, "请求添加您为好友", 1);
						// 如果已经发送过好友申请，接收到对方的好友申请，说明对方同意，直接回复同意

						// 如果没发送过，需要其他界面来判断是否同意

					} else if (type.equals(Presence.Type.unsubscribe)) {// 删除好友

						// 通知当前用户被对方删除========================

					} else if (type.equals(Presence.Type.subscribed)) {// 同意添加好友
						showNotify(from, "同意添加您为好友", 2);
						// 加对方为好友

						// 存入数据库（好友表+分组表）

						// 存入集合

					} else if (type.equals(Presence.Type.unsubscribed)) {// 拒绝添加好友

						// 发通知告诉用户对方拒绝添加好友========================

					} else if (type.equals(Presence.Type.unavailable)) {// 好友下线要更新好友列表，可以在这收到包后，发广播到指定页面更新列表

						// 更新在线好友集合

						// 好友头像变灰色

					} else if (type.equals(Presence.Type.available)) {// 好友上线

						// 更新在线好友集合

						// 好友头像变亮

					}

				}

			}

		};
		mXMPPConnection.addPacketListener(subscribeListener, packetFilter);
	}

	public String getUserJid(String userId) {

		return userId + "@" + mXMPPConnection.getServiceName();
	}

	int notify = 1;
	private RosterListener mRosterListener;

	private void registerRosterListener() {
		mRoster = mXMPPConnection.getRoster();
		mRosterListener = new RosterListener() {

			@Override
			public void presenceChanged(Presence presence) {
				Log.i("wxl", "presenceChanged(" + presence.getFrom() + "): "
						+ presence.getXmlns());
				String jabberID = AppUtils.getJabberID(presence.getFrom());
				RosterEntry rosterEntry = mRoster.getEntry(jabberID);
			}

			@Override
			public void entriesUpdated(Collection<String> entries) {
				// TODO Auto-generated method stub
				Log.i("wxl", "entriesUpdated(" + entries + ")");
				for (String entry : entries) {
					RosterEntry rosterEntry = mRoster.getEntry(entry);
				}
			}

			@Override
			public void entriesDeleted(Collection<String> entries) {
				Log.i("wxl", "entriesDeleted(" + entries + ")");
				for (String entry : entries) {

				}
			}

			@Override
			public void entriesAdded(Collection<String> entries) {
				Log.i("wxl", "entriesAdded(" + entries + ")");
				for (String entry : entries) {

					RosterEntry rosterEntry = mRoster.getEntry(entry);

					Log.i("wxl", "entriesAdded(" + rosterEntry.getUser() + ")");
					// String userName = PreferenceUtils
					// .getPrefString(XMPPService.this,
					// PreferenceConstants.USER_NAME, "");
					// if (entry.contains(userName))
					// return;
					// showNotify(entry);
					// Presence presence = new
					// Presence(Presence.Type.subscribed);
					// presence.setTo(entry);
					// String from = mXMPPConnection.getUser().substring(0,
					// mXMPPConnection.getUser().lastIndexOf("/"));
					// presence.setFrom(from);
					// Log.d("wxl", "to=" + entry);
					// Log.d("wxl", "from=" + mXMPPConnection.getUser());
					// mXMPPConnection.sendPacket(presence);//
					// connection是你自己的XMPPConnection链接

				}
			}
		};
		mRoster.addRosterListener(mRosterListener);
	}

	/**
	 * 显示通知栏
	 */
	@SuppressLint("NewApi")
	public void showNotify(String fromJid, String msg, int notify) {
		//
		String fromNickName = "";
		try {
			VCard vCard = vCard(fromJid.split("@")[0]);
			if (vCard.getNickName() != null) {
				fromNickName = vCard.getNickName();
			} else {
				fromNickName = fromJid.split("@")[0];
			}
		} catch (Exception e) {
			fromNickName = fromJid.split("@")[0];
			e.printStackTrace();
		}

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.system_notification))
				.setContentText(
						Html.fromHtml("<font color=#40659c>" + fromNickName
								+ "</font>" + msg)).setAutoCancel(true);

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, AddFriendsActivity.class);
		resultIntent.putExtra("fromJid", fromJid);
		resultIntent.putExtra("fromNickName", fromNickName);

		// resultIntent.putExtra("to", to);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(AddFriendsActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(notify, mBuilder.build());
	}

	private PacketListener subscriptionPacketListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			String userName = PreferenceUtils.getPrefString(XMPPService.this,
					PreferenceConstants.USER_NAME, "");
			Log.d("wxl", "registerSubscriptionListener packet.getFrom()=="
					+ packet.getFrom());
			Log.d("wxl", " registerSubscriptionListener userName" + userName);
			if (packet.getFrom().contains(userName))
				return;

			// 如果是自动接收所有请求，则回复一个添加信息
			if (Roster.getDefaultSubscriptionMode().equals(
					SubscriptionMode.accept_all)) {
				Presence subscription = new Presence(Presence.Type.subscribe);
				subscription.setTo(packet.getFrom());
				mXMPPConnection.sendPacket(subscription);
			} else {
				// showNotify(packet.getFrom());

			}
		}
	};

	/**
	 * 添加一个监听，监听好友添加请求。
	 */
	private void registerSubscriptionListener() {
		Log.d("wxl", "registerSubscriptionListener");
		PacketFilter filter = new PacketFilter() {

			@Override
			public boolean accept(Packet packet) {
				if (packet instanceof Presence) {
					Presence presence = (Presence) packet;
					if (presence.getType().equals(Presence.Type.subscribe)) {
						return true;
					}
				}
				return false;
			}
		};
		mXMPPConnection.addPacketListener(subscriptionPacketListener, filter);
	}

	/**
	 * 非UI线程连接失败反馈
	 * 
	 * @param reason
	 */
	public void postConnectionFailed(final String reason) {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionFailed(reason);
			}
		});
	}

	// 设置连接状态
	public void setStatusFromConfig() {
		mSmackImpl.setStatusFromConfig();
	}

	/**
	 * UI线程反馈连接失败
	 * 
	 * @param reason
	 */
	private void connectionFailed(String reason) {
		L.i(XMPPService.class, "connectionFailed: " + reason);
		mConnectedState = DISCONNECTED;// 更新当前连接状态
		if (TextUtils.equals(reason, LOGOUT)) {// 如果是手动退出
			((AlarmManager) getSystemService(Context.ALARM_SERVICE))
					.cancel(mPAlarmIntent);
			return;
		}
		// 回调
		if (mConnectionStatusCallback != null) {
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,
					reason);
			if (mIsFirstLoginAction)// 如果是第一次登录,就算登录失败也不需要继续
				return;
		}

		// 无网络连接时,直接返回
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
			((AlarmManager) getSystemService(Context.ALARM_SERVICE))
					.cancel(mPAlarmIntent);
			return;
		}

		String userName = PreferenceUtils.getPrefString(XMPPService.this,
				PreferenceConstants.USER_NAME, "");
		String userPassword = PreferenceUtils.getPrefString(XMPPService.this,
				PreferenceConstants.PASSWORD, "");
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPassword))
			// 无保存的帐号密码时，也直接返回
			if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPassword)) {
				L.d("account = null || password = null");
				return;
			}
		// 如果不是手动退出并且需要重新连接，则开启重连闹钟
		// if (true) {
		L.d("connectionFailed(): registering reconnect in " + mReconnectTimeout
				+ "s");
		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ mReconnectTimeout * 1000, mPAlarmIntent);
		mReconnectTimeout = mReconnectTimeout * 2;
		if (mReconnectTimeout > RECONNECT_MAXIMUM)
			mReconnectTimeout = RECONNECT_MAXIMUM;
		// } else {
		// ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
		// .cancel(mPAlarmIntent);
		// }

	}

	private void postConnectionScuessed() {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionScuessed();
			}

		});
	}

	private void connectionScuessed() {
		mConnectedState = CONNECTED;// 已经连接上
		mReconnectTimeout = RECONNECT_AFTER;// 重置重连的时间

		if (mConnectionStatusCallback != null)
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,
					"");
	}

	// 连接中，通知界面线程做一些处理
	private void postConnecting() {
		mMainHandler.post(new Runnable() {
			public void run() {
				connecting();
			}
		});
	}

	private void connecting() {
		mConnectedState = CONNECTING;// 连接中
		if (mConnectionStatusCallback != null)
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,
					"");
	}

	// 判断程序是否在后台运行的任务
	Runnable monitorStatus = new Runnable() {
		public void run() {
			try {
				L.i("monitorStatus is running... " + mPackageName);
				mMainHandler.removeCallbacks(monitorStatus);
				// 如果在后台运行并且连接上了
				if (!isAppOnForeground()) {
					L.i("app run in background...");
					// if (isAuthenticated())
					// updateServiceNotification(getString(R.string.run_bg_ticker));
					return;
				} else {
					stopForeground(true);
				}
				// mMainHandler.postDelayed(monitorStatus, 1000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public boolean isAppOnForeground() {
		List<RunningTaskInfo> taskInfos = mActivityManager.getRunningTasks(1);
		if (taskInfos.size() > 0
				&& TextUtils.equals(getPackageName(),
						taskInfos.get(0).topActivity.getPackageName())) {
			return true;
		}

		// List<RunningAppProcessInfo> appProcesses = mActivityManager
		// .getRunningAppProcesses();
		// if (appProcesses == null)
		// return false;
		// for (RunningAppProcessInfo appProcess : appProcesses) {
		// // L.i("liweiping", appProcess.processName);
		// // The name of the process that this object is associated with.
		// if (appProcess.processName.equals(mPackageName)
		// && appProcess.importance ==
		// RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
		// return true;
		// }
		// }
		return false;
	}

	// 自动重连广播
	private class ReconnectAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			L.d("Alarm received.");
			// if (!PreferenceUtils.getPrefBoolean(XMPPService.this,
			// PreferenceConstants.AUTO_RECONNECT, true)) {
			// return;
			// }
			if (mConnectedState != DISCONNECTED) {
				L.d("Reconnect attempt aborted: we are connected again!");
				return;
			}
			String userName = PreferenceUtils.getPrefString(XMPPService.this,
					PreferenceConstants.USER_NAME, "");
			String userPassword = PreferenceUtils.getPrefString(
					XMPPService.this, PreferenceConstants.PASSWORD, "");
			if (!TextUtils.isEmpty(userName)
					&& !TextUtils.isEmpty(userPassword))
				login(userName, userPassword);
		}
	}

	@Override
	public void onNetChange() {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {// 如果是网络断开，不作处理
			connectionFailed(NETWORK_ERROR);
			return;
		}
		if (isAuthenticated())// 如果已经连接上，直接返回
			return;
		String userName = PreferenceUtils.getPrefString(XMPPService.this,
				PreferenceConstants.USER_NAME, "");
		String userPassword = PreferenceUtils.getPrefString(XMPPService.this,
				PreferenceConstants.PASSWORD, "");
		if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPassword))
			login(userName, userPassword);// 重连
	}

	@Override
	public void activityOnResume() {
		L.i("activity onResume ...");
		mMainHandler.post(monitorStatus);
	}

	@Override
	public void activityOnPause() {
		L.i("activity onPause ...");
		mMainHandler.postDelayed(monitorStatus, 1000L);
	}

	private void registerAllListener() {
		// actually, authenticated must be true now, or an exception must have
		// been thrown.fli
		if (isAuthenticated()) {
			// registerRosterListener();// 监听联系人动态变化
			// registerMessageListener();
			// registerMessageSendFailureListener();
			registerPongListener();
			// registerSubscriptionListener();
			addSubscriptionListener();
			// sendOfflineMessages();
			if (XMPPService.this == null) {
				mXMPPConnection.disconnect();
				return;
			}
			// we need to "ping" the service to let it know we are actually
			// connected, even when no roster entries will come in
			// mService.rosterChanged();
		}
	}

	/***************** start 处理ping服务器消息 ***********************/
	private void registerPongListener() {
		// reset ping expectation on new connection
		mPingID = null;

		if (mPongListener != null)
			mXMPPConnection.removePacketListener(mPongListener);

		mPongListener = new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				if (packet == null)
					return;

				if (packet.getPacketID().equals(mPingID)) {
					L.i(String.format(
							"Ping: server latency %1.3fs",
							(System.currentTimeMillis() - mPingTimestamp) / 1000.));
					mPingID = null;
					((AlarmManager) XMPPService.this
							.getSystemService(Context.ALARM_SERVICE))
							.cancel(mPongTimeoutAlarmPendIntent);
				}
			}

		};

		mXMPPConnection.addPacketListener(mPongListener, new PacketTypeFilter(
				IQ.class));
		mPingAlarmPendIntent = PendingIntent.getBroadcast(
				XMPPService.this.getApplicationContext(), 0, mPingAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mPongTimeoutAlarmPendIntent = PendingIntent.getBroadcast(
				XMPPService.this.getApplicationContext(), 0,
				mPongTimeoutAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		XMPPService.this.registerReceiver(mPingAlarmReceiver, new IntentFilter(
				PING_ALARM));
		XMPPService.this.registerReceiver(mPongTimeoutAlarmReceiver,
				new IntentFilter(PONG_TIMEOUT_ALARM));
		((AlarmManager) XMPPService.this
				.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ AlarmManager.INTERVAL_FIFTEEN_MINUTES,
				AlarmManager.INTERVAL_FIFTEEN_MINUTES, mPingAlarmPendIntent);
	}

	/**
	 * BroadcastReceiver to trigger reconnect on pong timeout.
	 */
	private class PongTimeoutAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			L.d("Ping: timeout for " + mPingID);
			postConnectionFailed(XMPPService.PONG_TIMEOUT);
			logout();// 超时就断开连接
		}
	}

	/**
	 * BroadcastReceiver to trigger sending pings to the server
	 */
	private class PingAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			if (mXMPPConnection.isAuthenticated()) {
				sendServerPing();
			} else
				L.d("Ping: alarm received, but not connected to server.");
		}
	}

	public void sendServerPing() {
		if (mPingID != null) {
			L.d("Ping: requested, but still waiting for " + mPingID);
			return; // a ping is still on its way
		}
		Ping ping = new Ping();
		ping.setType(Type.GET);
		ping.setTo(AppConfig.XMPP_HOST);
		mPingID = ping.getPacketID();
		mPingTimestamp = System.currentTimeMillis();
		L.d("Ping: sending ping " + mPingID);
		mXMPPConnection.sendPacket(ping);

		// register ping timeout handler: PACKET_TIMEOUT(30s) + 3s
		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ PACKET_TIMEOUT + 3000, mPongTimeoutAlarmPendIntent);
	}

	/***************** end 处理ping服务器消息 ***********************/

	public void registerPacketListener() {
		Log.i("wxl", "registerPacketListener");
		// 理解为条件过滤器 过滤出Presence包
		PacketFilter filter = new AndFilter(
				new PacketTypeFilter(Presence.class));
		PacketListener listener = new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				Log.i("wxl", "PresenceService------" + packet.toXML());
				// 看API可知道 Presence是Packet的子类
				if (packet instanceof Presence) {
					Presence presence = (Presence) packet;
					// Presence还有很多方法，可查看API
					String from = presence.getFrom();// 发送方
					String to = presence.getTo();// 接收方
					Log.d("wxl", "registerPacketListener to=" + to);
					Log.d("wxl", "registerPacketListener from=" + from);
					// Presence.Type有7中状态
					Log.i("wxl", "registerPacketListener presence.getType()="
							+ presence.getType());
					if (presence.getType().equals(Presence.Type.subscribe)) {// 好友申请
						Log.d("wxl", "好友申请");
					} else if (presence.getType().equals(
							Presence.Type.subscribed)) {// 同意添加好友
						Log.d("wxl", "同意添加好友");
					} else if (presence.getType().equals(
							Presence.Type.unsubscribe)) {// 拒绝添加好友 和 删除好友
						Log.d("wxl", "他取消订阅别人，请求删除某好友");
					} else if (presence.getType().equals(
							Presence.Type.unsubscribed)) {// 拒绝被别人订阅，即拒绝对放的添加请求
						Log.d("wxl", "拒绝被别人订阅，即拒绝对放的添加请求");
					} else if (presence.getType().equals(
							Presence.Type.unavailable)) {// 好友下线
						// 要更新好友列表，可以在这收到包后，发广播到指定页面
						Log.d("wxl", "好友下线"); // 更新列表

					} else {// 好友上线
						Log.d("wxl", "好友上线");
					}
				}
			}
		};

		mXMPPConnection.addPacketListener(listener, filter);
	}

	private void registerSmackProviders() {
		ProviderManager pm = ProviderManager.getInstance();

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
		}

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
	}
}
