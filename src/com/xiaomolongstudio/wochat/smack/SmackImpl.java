package com.xiaomolongstudio.wochat.smack;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.ping.packet.Ping;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.xiaomolongstudio.wochat.service.XMPPService;
import com.xiaomolongstudio.wochat.utils.L;
import com.xiaomolongstudio.wochat.xmpp.XXException;

public class SmackImpl implements Smack {
	public static final String XMPP_IDENTITY_NAME = "xx";
	public static final String XMPP_IDENTITY_TYPE = "phone";
	private static final int PACKET_TIMEOUT = 30000;
	private ConnectionConfiguration mXMPPConfig;
	private XMPPConnection mXMPPConnection;
	private XMPPService mService;
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

	static {
		registerSmackProviders();
	}

	static void registerSmackProviders() {
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

	// ping-pong服务器
	// String xmpp_host = "192.168.1.103";
	String xmpp_service_name = "gmail.com";
	int xmpp_port = 5222;
	String xmpp_host = "192.168.2.8";

	public SmackImpl(XMPPService service) {

		this.mXMPPConfig = new ConnectionConfiguration(xmpp_host, xmpp_port,
				xmpp_service_name);

		this.mXMPPConfig.setReconnectionAllowed(false);
		this.mXMPPConfig.setSendPresence(false);
		this.mXMPPConfig.setCompressionEnabled(false); // disable for now
		this.mXMPPConfig.setDebuggerEnabled(true);
		this.mXMPPConfig
				.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
		this.mService = service;
	}

	@Override
	public boolean login(String account, String password) throws XXException {
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
			// registerRosterListener();// 监听联系人动态变化
			mXMPPConnection.connect();
			if (!mXMPPConnection.isConnected()) {
				throw new XXException("SMACK connect failed without exception!");
			}
			mXMPPConnection.addConnectionListener(new ConnectionListener() {
				public void connectionClosedOnError(Exception e) {
					mService.postConnectionFailed(e.getMessage());
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

		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage(),
					e.getWrappedThrowable());
		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			L.e(SmackImpl.class, "login(): " + Log.getStackTraceString(e));
			throw new XXException(e.getLocalizedMessage(), e.getCause());
		}
		registerAllListener();// 注册监听其他的事件，比如新消息
		return mXMPPConnection.isAuthenticated();
	}

	private void registerAllListener() {
		// actually, authenticated must be true now, or an exception must have
		// been thrown.
		if (isAuthenticated()) {
			// registerMessageListener();
			// registerMessageSendFailureListener();
			registerPongListener();
			// sendOfflineMessages();
			if (mService == null) {
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
					((AlarmManager) mService
							.getSystemService(Context.ALARM_SERVICE))
							.cancel(mPongTimeoutAlarmPendIntent);
				}
			}

		};

		mXMPPConnection.addPacketListener(mPongListener, new PacketTypeFilter(
				IQ.class));
		mPingAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPingAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mPongTimeoutAlarmPendIntent = PendingIntent.getBroadcast(
				mService.getApplicationContext(), 0, mPongTimeoutAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mService.registerReceiver(mPingAlarmReceiver, new IntentFilter(
				PING_ALARM));
		mService.registerReceiver(mPongTimeoutAlarmReceiver, new IntentFilter(
				PONG_TIMEOUT_ALARM));
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
				.setInexactRepeating(AlarmManager.RTC_WAKEUP,
						System.currentTimeMillis()
								+ AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						AlarmManager.INTERVAL_FIFTEEN_MINUTES,
						mPingAlarmPendIntent);
	}

	/**
	 * BroadcastReceiver to trigger reconnect on pong timeout.
	 */
	private class PongTimeoutAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			L.d("Ping: timeout for " + mPingID);
			mService.postConnectionFailed(XMPPService.PONG_TIMEOUT);
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

	/***************** end 处理ping服务器消息 ***********************/

	/***************** start 发送离线消息 ***********************/
	// public void sendOfflineMessages() {
	// Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI,
	// SEND_OFFLINE_PROJECTION, SEND_OFFLINE_SELECTION, null, null);
	// final int _ID_COL = cursor.getColumnIndexOrThrow(ChatConstants._ID);
	// final int JID_COL = cursor.getColumnIndexOrThrow(ChatConstants.JID);
	// final int MSG_COL = cursor.getColumnIndexOrThrow(ChatConstants.MESSAGE);
	// final int TS_COL = cursor.getColumnIndexOrThrow(ChatConstants.DATE);
	// final int PACKETID_COL = cursor
	// .getColumnIndexOrThrow(ChatConstants.PACKET_ID);
	// ContentValues mark_sent = new ContentValues();
	// mark_sent.put(ChatConstants.DELIVERY_STATUS,
	// ChatConstants.DS_SENT_OR_READ);
	// while (cursor.moveToNext()) {
	// int _id = cursor.getInt(_ID_COL);
	// String toJID = cursor.getString(JID_COL);
	// String message = cursor.getString(MSG_COL);
	// String packetID = cursor.getString(PACKETID_COL);
	// long ts = cursor.getLong(TS_COL);
	// L.d("sendOfflineMessages: " + toJID + " > " + message);
	// final Message newMessage = new Message(toJID, Message.Type.chat);
	// newMessage.setBody(message);
	// DelayInformation delay = new DelayInformation(new Date(ts));
	// newMessage.addExtension(delay);
	// newMessage.addExtension(new DelayInfo(delay));
	// newMessage.addExtension(new DeliveryReceiptRequest());
	// if ((packetID != null) && (packetID.length() > 0)) {
	// newMessage.setPacketID(packetID);
	// } else {
	// packetID = newMessage.getPacketID();
	// mark_sent.put(ChatConstants.PACKET_ID, packetID);
	// }
	// Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
	// + ChatProvider.TABLE_NAME + "/" + _id);
	// mContentResolver.update(rowuri, mark_sent, null, null);
	// mXMPPConnection.sendPacket(newMessage); // must be after marking
	// // delivered, otherwise it
	// // may override the
	// // SendFailListener
	// }
	// cursor.close();
	// }

	// private void updateRosterEntryInDB(final RosterEntry entry) {
	// final ContentValues values = getContentValuesForRosterEntry(entry);
	//
	// if (mContentResolver.update(RosterProvider.CONTENT_URI, values,
	// RosterConstants.JID + " = ?", new String[] { entry.getUser() }) == 0)
	// addRosterEntryToDB(entry);
	// }

	// private void addRosterEntryToDB(final RosterEntry entry) {
	// ContentValues values = getContentValuesForRosterEntry(entry);
	// Uri uri = mContentResolver.insert(RosterProvider.CONTENT_URI, values);
	// L.i("addRosterEntryToDB: Inserted " + uri);
	// }

	// private void deleteRosterEntryFromDB(final String jabberID) {
	// int count = mContentResolver.delete(RosterProvider.CONTENT_URI,
	// RosterConstants.JID + " = ?", new String[] { jabberID });
	// L.i("deleteRosterEntryFromDB: Deleted " + count + " entries");
	// }

	// private ContentValues getContentValuesForRosterEntry(final RosterEntry
	// entry) {
	// final ContentValues values = new ContentValues();
	//
	// values.put(RosterConstants.JID, entry.getUser());
	// values.put(RosterConstants.ALIAS, getName(entry));
	//
	// Presence presence = mRoster.getPresence(entry.getUser());
	// values.put(RosterConstants.STATUS_MODE, getStatusInt(presence));
	// values.put(RosterConstants.STATUS_MESSAGE, presence.getStatus());
	// values.put(RosterConstants.GROUP, getGroup(entry.getGroups()));
	//
	// return values;
	// }

	public void setStatusFromConfig() {
		// boolean messageCarbons = PreferenceUtils.getPrefBoolean(mService,
		// PreferenceConstants.MESSAGE_CARBONS, true);
		// String statusMode = PreferenceUtils.getPrefString(mService,
		// PreferenceConstants.STATUS_MODE, PreferenceConstants.AVAILABLE);
		// String statusMessage = PreferenceUtils.getPrefString(mService,
		// PreferenceConstants.STATUS_MESSAGE,
		// mService.getString(R.string.status_online));
		// int priority = PreferenceUtils.getPrefInt(mService,
		// PreferenceConstants.PRIORITY, 0);
		// if (messageCarbons)
		CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(true);

		Presence presence = new Presence(Presence.Type.available);
		// Mode mode = Mode.valueOf(statusMode);
		// presence.setMode(mode);
		// presence.setStatus(statusMessage);
		// presence.setPriority(priority);
		mXMPPConnection.sendPacket(presence);
	}

	/******************************* end 联系人数据库事件处理 **********************************/

	@Override
	public boolean isAuthenticated() {
		if (mXMPPConnection != null) {
			return (mXMPPConnection.isConnected() && mXMPPConnection
					.isAuthenticated());
		}
		return false;
	}

	// @Override
	// public void sendMessage(String toJID, String message) {
	// TODO Auto-generated method stub
	// final Message newMessage = new Message(toJID, Message.Type.chat);
	// newMessage.setBody(message);
	// newMessage.addExtension(new DeliveryReceiptRequest());
	// if (isAuthenticated()) {
	// addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
	// ChatConstants.DS_SENT_OR_READ, System.currentTimeMillis(),
	// newMessage.getPacketID());
	// mXMPPConnection.sendPacket(newMessage);
	// } else {
	// // send offline -> store to DB
	// addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
	// ChatConstants.DS_NEW, System.currentTimeMillis(),
	// newMessage.getPacketID());
	// }
	// }

	@Override
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
		((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ PACKET_TIMEOUT + 3000, mPongTimeoutAlarmPendIntent);
	}

	@Override
	public String getNameForJID(String jid) {
		if (null != this.mRoster.getEntry(jid)
				&& null != this.mRoster.getEntry(jid).getName()
				&& this.mRoster.getEntry(jid).getName().length() > 0) {
			return this.mRoster.getEntry(jid).getName();
		} else {
			return jid;
		}
	}

	@Override
	public boolean logout() {
		L.d("unRegisterCallback()");
		// remove callbacks _before_ tossing old connection
		try {
			mXMPPConnection.removePacketListener(mPongListener);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPingAlarmPendIntent);
			((AlarmManager) mService.getSystemService(Context.ALARM_SERVICE))
					.cancel(mPongTimeoutAlarmPendIntent);
			mService.unregisterReceiver(mPingAlarmReceiver);
			mService.unregisterReceiver(mPongTimeoutAlarmReceiver);
		} catch (Exception e) {
			// ignore it!
			return false;
		}
		if (mXMPPConnection.isConnected()) {
			// work around SMACK's #%&%# blocking disconnect()
			new Thread() {
				public void run() {
					L.d("shutDown thread started");
					mXMPPConnection.disconnect();
					L.d("shutDown thread finished");
				}
			}.start();
		}
		// setStatusOffline();
		this.mService = null;
		return true;
	}

}
