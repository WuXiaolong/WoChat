package com.wuxiaolong.wochat.xmpp;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.wuxiaolong.wochat.utils.AppConfig;
import com.wuxiaolong.wochat.utils.Constant;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.jivesoftware.smackx.xevent.provider.MessageEventProvider;
import org.jivesoftware.smackx.xhtmlim.provider.XHTMLExtensionProvider;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WuXiaolong on 2015/6/12.
 */
public class XMPPService {
    //    MainHandler mMainHandler = new MainHandler();
    static XMPPService mXMPPService;
    XMPPTCPConnection mXMPPTCPConnection = null;

    public static XMPPService getInstance() {
        if (mXMPPService == null) {
            mXMPPService = new XMPPService();
        }
        return mXMPPService;
    }

    public void initXmppTcpConnection() {
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(AppConfig.XMPP_HOST,
                AppConfig.XMPP_PORT, AppConfig.XMPP_SERVICE_NAME);
        connectionConfiguration.setReconnectionAllowed(true);
        connectionConfiguration.setSendPresence(true);
        connectionConfiguration.setCompressionEnabled(false); // disable for now
        connectionConfiguration.setDebuggerEnabled(true);
        connectionConfiguration
                .setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
        SASLAuthentication.supportSASLMechanism("PLAIN", 0);
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, null, new SecureRandom());
//            connectionConfiguration.setCustomSSLContext(sc);
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalStateException(e);
//        } catch (KeyManagementException e) {
//            throw new IllegalStateException(e);
//        }
        mXMPPTCPConnection = new XMPPTCPConnection(connectionConfiguration);

        try {
            mXMPPTCPConnection.connect();
            Log.d("wxl", "initXmppTcpConnection isConnected=" + mXMPPTCPConnection.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
        }
        registerSmackProviders();

    }

    public XMPPTCPConnection getXmppTcpConnection() {
        return mXMPPTCPConnection;
    }


    public boolean xmppTCPConnectionConnect() {
        if (!mXMPPTCPConnection.isConnected()) {
            try {
                mXMPPTCPConnection.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mXMPPTCPConnection.isConnected();
    }

    public String xmppRegister(String account, String password) {
        initXmppTcpConnection();
        if (mXMPPTCPConnection.isConnected()) {
            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
            try {
                accountManager.createAccount(account, password);
                return Constant.REGISTER_SUCCESS;
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                Log.d("wxl", "xmppRegisterNoResponseException=" + e.getMessage());
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                Log.d("wxl", "xmppRegisterXMPPErrorException=" + e.getMessage());
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                Log.d("wxl", "xmppRegisterNotConnectedException=" + e.getMessage());
            }

        } else {
            Log.d("wxl", "isConnected=" + mXMPPTCPConnection.isConnected());
            return Constant.NOT_CONNECTED_SERVER;
        }
        return Constant.REGISTER_SUCCESS;
    }

    public void xmppDeleteAccount() {
        Log.d("wxl", "xmppDeleteAccount isConnected=" + mXMPPTCPConnection.isConnected());
        if (mXMPPTCPConnection.isConnected()) {
            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
            try {
                accountManager.deleteAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取用户头像信息
     *
     * @return
     */
    public Drawable getUserImage(String userId) {
        ByteArrayInputStream bais = null;
        try {
            VCard vcard = new VCard();
            // 加入这句代码，解决No VCard for
//            ProviderManager.addIQProvider("vCard", "vcard-temp",
//                    new VCardProvider());
            vcard.load(mXMPPTCPConnection,
                    userId + "@" + mXMPPTCPConnection.getServiceName());
            Log.d("wxl", "mXMPPTCPConnection=" + mXMPPTCPConnection);
            Log.d("wxl", "vcard=" + vcard);
            Log.d("wxl", "getUserImage=" + vcard.getAvatar());
            if (vcard == null || vcard.getAvatar() == null)
                return null;
            bais = new ByteArrayInputStream(vcard.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 获取用户头像信息
     *
     * @return
     */
    public void getUserAvatar(String user) {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            VCard vcard;
            if (TextUtils.equals(user, "")) {
                vcard = getVCard();
            } else {
                vcard = getVCard(user);
            }
            Log.d("wxl", "xmppservice vcard=" + vcard);
            Log.d("wxl", "xmppservice vcard.getAvatar()=" + vcard.getAvatar());
            if (vcard == null || vcard.getAvatar() == null)
                byteArrayInputStream = new ByteArrayInputStream(vcard.getAvatar());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public VCard getVCard() {
        Log.d("wxl", "getVCard mXMPPTCPConnection=" + mXMPPTCPConnection.isConnected());
        VCard vCard = null;
        try {
            vCard = new VCard();
            vCard.load(mXMPPTCPConnection);
            Log.d("wxl", "getVCard xmppservice getServiceName=" + vCard);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vCard;
    }

    public VCard getVCard(String userId) {
        Log.d("wxl", "mXMPPTCPConnection=" + mXMPPTCPConnection.isConnected());
        VCard vCard = null;
        try {
            vCard = new VCard();
            vCard.load(mXMPPTCPConnection,
                    userId + "@" + mXMPPTCPConnection.getServiceName());
            Log.d("wxl", "xmppservice getServiceName=" + vCard);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vCard;
    }

    public MultiUserChat getMultiUserChat(String roomName) {
        String roomService = roomName + "@conference."
                + mXMPPTCPConnection.getServiceName();// 房间域名
        MultiUserChat multiUserChat = new MultiUserChat(mXMPPTCPConnection,
                roomService);
        return multiUserChat;
    }

    /**
     * 创建房间
     * return -1:fail；
     * 0 create success；
     * 1 room exist
     */
    public int createRoom(String userName, String password,
                          String roomName, String roomDesc) {
        try {
            MultiUserChat mMultiUserChat = getMultiUserChat(roomName);
            // 创建聊天室
            mMultiUserChat.create(roomName);

            // 获得聊天室的配置表单
            Form form = mMultiUserChat.getConfigurationForm();
            // 根据原始表单创建一个要提交的新表单。
            Form submitForm = form.createAnswerForm();
            // 向要提交的表单添加默认答复
//            for (Iterator<?> fields = form.getFields(); fields.hasNext(); ) {
//                FormField field = (FormField) fields.next();
//                if (!FormField.TYPE_HIDDEN.equals(field.getType())
//                        && field.getVariable() != null) {
//                    // 设置默认值作为答复
//                    submitForm.setDefaultAnswer(field.getVariable());
//                }
//            }
            // 设置当前用户为聊天室的创建者
            List<String> roomOwner = new ArrayList<String>();
            roomOwner.add(mXMPPTCPConnection.getUser());
            submitForm.setAnswer("muc#roomconfig_roomowners", roomOwner);

            submitForm.setAnswer("muc#roomconfig_persistentroom", true); // 持久聊天室
            submitForm.setAnswer("muc#roomconfig_roomdesc", roomDesc); // 房间描述
            submitForm.setAnswer("muc#roomconfig_membersonly", false); // 仅对成员开放
            submitForm.setAnswer("muc#roomconfig_allowinvites", true); // 允许邀请其他人

            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);// 密码进去房间
            submitForm.setAnswer("muc#roomconfig_roomsecret", "password");

            submitForm.setAnswer("muc#roomconfig_enablelogging", true); // 登录房间对话
            submitForm.setAnswer("x-muc#roomconfig_reservednick", false); // 仅允许注册的昵称登录
            submitForm.setAnswer("x-muc#roomconfig_canchangenick", true); // 允许使用者修改昵称
            submitForm.setAnswer("x-muc#roomconfig_registration", true); // 允许用户注册房间
            List<String> list = new ArrayList<String>();
            list.add("0");
            submitForm.setAnswer("muc#roomconfig_maxusers", list); // 设置房间人数

            mMultiUserChat.sendConfigurationForm(submitForm);// 发送已完成的表单（有默认值）到服务器来配置聊天室
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("wxl", "======" + e.getMessage());
            if (TextUtils.equals(e.getMessage(), "Creation failed - Missing acknowledge of room creation.")) {
                return 1;
            }
        }
        return -1;
    }

    /**
     * 加入聊天室
     *
     * @param userName
     * @param password
     * @param roomName
     * @return
     */

    public MultiUserChat joinRoom(String userName, String password,
                                  String roomName) {
        try {
            // // 使用XMPPConnection创建一个MultiUserChat窗口
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.getMaxStanzas();
//            history.setMaxStanzas(0);
            // history.getSince();
            MultiUserChat mMultiUserChat = getMultiUserChat(roomName);
            mMultiUserChat.join(userName, password, history,
                    SmackConfiguration.getDefaultPacketReplyTimeout());
            Log.i("wxl", "会议室加入成功........");
            return mMultiUserChat;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("wxl", "会议室加入失败........" + e.getMessage());
            return null;
        }

    }


    static void registerSmackProviders() {
        ProviderManager pm = new ProviderManager();

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
//        pm.addExtensionProvider("x", "jabber:x:roster",
//                new RosterExchangeProvider());
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


//    class MainHandler extends Handler {
//        public MainHandler() {
//            super();
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case AppConfig.LOGIN_RECONNECT:
//                    String reconnectMsg = (String) msg.obj;
//                    if (TextUtils.equals(reconnectMsg, AppConfig.LOGIN_RECONNECTINGIN)) {
//                        mOnStatusCallbackListener.connecting();
//                    } else if (TextUtils.equals(reconnectMsg, AppConfig.LOGIN_AUTHENTICATED)) {
//                        mOnStatusCallbackListener.authenticated();
//                    }
//                    break;
//            }
//        }
//    }


}
