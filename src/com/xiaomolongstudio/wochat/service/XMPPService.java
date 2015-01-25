package com.xiaomolongstudio.wochat.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomolongstudio.wochat.R;
import com.xiaomolongstudio.wochat.smack.SmackImpl;
import com.xiaomolongstudio.wochat.ui.BaseActivity;
import com.xiaomolongstudio.wochat.ui.BaseActivity.BackPressHandler;
import com.xiaomolongstudio.wochat.ui.LoginActivity;
import com.xiaomolongstudio.wochat.ui.MainActivity;
import com.xiaomolongstudio.wochat.utils.AppConfig;
import com.xiaomolongstudio.wochat.utils.AppUtils;
import com.xiaomolongstudio.wochat.utils.L;
import com.xiaomolongstudio.wochat.utils.NetUtil;
import com.xiaomolongstudio.wochat.utils.PreferenceConstants;
import com.xiaomolongstudio.wochat.utils.PreferenceUtils;
import com.xiaomolongstudio.wochat.xmpp.IConnectionStatusCallback;
import com.xiaomolongstudio.wochat.xmpp.XMPPBroadcastReceiver;
import com.xiaomolongstudio.wochat.xmpp.XMPPBroadcastReceiver.EventHandler;
import com.xiaomolongstudio.wochat.xmpp.XXException;

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
	String xmpp_service_name = "gmail.com";
	int xmpp_port = 5222;
	// String xmpp_host = "192.168.2.8";
	String xmpp_host = "192.168.1.102";
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
		this.mXMPPConfig = new ConnectionConfiguration(xmpp_host, xmpp_port,
				xmpp_service_name);

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
	 * 添加好友
	 * 
	 * @param roster
	 * @param toUserName
	 * @param name
	 * @return
	 */
	public boolean addUser(String toUserName, String nickname) {
		try {

			Roster roster = mXMPPConnection.getRoster();
			String toUser = toUserName + "@" + mXMPPConnection.getServiceName();
			// // 默认添加到【我的好友】分组
			String groupName = "cntv";
			Log.d("wxl", "toUser==" + toUser);
			roster.createEntry(toUser, nickname, null);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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

	// 默认显示的的Notification http://www.oschina.net/question/234345_40111
	public void showDefaultNotification() { // 新建状态栏通知

	}

	// 清除通知栏
	public void clearNotifications(String Jid) {
		clearNotification(Jid);
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
			registerRosterListener();// 监听联系人动态变化
			// registerMessageListener();
			// registerMessageSendFailureListener();
			registerPongListener();
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
		ping.setTo(xmpp_host);
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

	private RosterListener mRosterListener;

	private void registerRosterListener() {
		mRoster = mXMPPConnection.getRoster();
		mRosterListener = new RosterListener() {
			private boolean isFristRoter;

			@Override
			public void presenceChanged(Presence presence) {
				Log.i("wxl", "presenceChanged(" + presence.getFrom() + "): "
						+ presence);
				Log.i("wxl", "registerRosterListener presence.getType()="
						+ presence.getType());
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
					Log.i("wxl", "entriesAdded(" + entry + ")");

					Presence presence = new Presence(Presence.Type.subscribed);
					presence.setTo(entry);
					String from = mXMPPConnection.getUser().substring(0,
							mXMPPConnection.getUser().lastIndexOf("/"));
					presence.setFrom(from);
					Log.d("wxl", "to=" + entry);
					Log.d("wxl", "from=" + mXMPPConnection.getUser());
					mXMPPConnection.sendPacket(presence);// connection是你自己的XMPPConnection链接

				}
			}
		};
		mRoster.addRosterListener(mRosterListener);
	}

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
		// add IQ handling
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// add delayed delivery notifications
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
		// add carbons and forwarding
		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
				new Forwarded.Provider());
		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
		pm.addExtensionProvider("received", Carbon.NAMESPACE,
				new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());

		ServiceDiscoveryManager.setIdentityName(XMPP_IDENTITY_NAME);
		ServiceDiscoveryManager.setIdentityType(XMPP_IDENTITY_TYPE);
	}
}
