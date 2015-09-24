package com.wuxiaolong.wochat.xmpp;

import android.util.Log;

import com.wuxiaolong.wochat.mvp.model.LoginModel;
import com.wuxiaolong.wochat.utils.Constant;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * Created by WuXiaolong on 2015/9/24.
 */
public class XMPPLogin {
    static XMPPLogin mXMPPLogin;

    public static XMPPLogin getInstance() {
        if (mXMPPLogin == null) {
            mXMPPLogin = new XMPPLogin();
        }
        return mXMPPLogin;
    }
    /**
     * 登录
     */
    public String xmppLogin(final String userName, final String psd) {
        XMPPService xmppService = XMPPService.getInstance();
        xmppService.initXmppTcpConnection();
        XMPPTCPConnection mXMPPTCPConnection = xmppService.getXmppTcpConnection();
        Log.d("wxl", "xmppLogin isConnected=" + mXMPPTCPConnection.isConnected());
        if (mXMPPTCPConnection.isConnected()) {
            try {
                if (!mXMPPTCPConnection.isAuthenticated()) {
                    mXMPPTCPConnection.login(userName, psd);
                    setUserPresence(mXMPPTCPConnection);
                    mXMPPTCPConnection.addConnectionListener(new LoginConnectionListener());
                    LoginModel loginModel = new LoginModel();
                    loginModel.setUserName(userName);
                    loginModel.setPsd(psd);
                    return Constant.LOGIN_SUCCESS;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("wxl", "login fail===" + e.getMessage());
//                if (TextUtils.equals(e.getMessage(), "SASLError using DIGEST-MD5: not-authorized")) {
                return e.getMessage();
            }
        } else {
            Log.i("wxl", "onLoginFail=no Connect");
            return Constant.NOT_CONNECTED_SERVER;
        }
        return Constant.NOT_CONNECTED_SERVER;
    }

    /**
     * 设置用户状态
     */
    public void setUserPresence(XMPPTCPConnection xmppTcpConnection) {
        Presence presence = new Presence(Presence.Type.available);
        // Mode mode = Mode.valueOf(statusMode);
        // presence.setMode(mode);
        // presence.setStatus(statusMessage);
        // presence.setPriority(priority);
        try {
            xmppTcpConnection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    class LoginConnectionListener implements ConnectionListener {
        @Override
        public void connected(XMPPConnection xmppConnection) {
            Log.d("wxl", "login=connected");
        }

        @Override
        public void authenticated(XMPPConnection xmppConnection) {
            Log.d("wxl", "login=authenticated");
            mOnStatusCallbackListener.authenticated();
        }

        @Override
        public void connectionClosed() {
            Log.d("wxl", "login=connectionClosed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.d("wxl", "login=connectionClosedOnError");
            mOnStatusCallbackListener.disconnect(e.getMessage());
        }

        @Override
        public void reconnectingIn(int i) {
            Log.d("wxl", "login=reconnectingIn===" + i);
        }

        @Override
        public void reconnectionSuccessful() {
            Log.d("wxl", "login=reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.d("wxl", "login=reconnectionFailed");
        }

    }

    OnStatusCallbackListener mOnStatusCallbackListener;

    public void setOnStatusCallbackListener(OnStatusCallbackListener listener) {
        mOnStatusCallbackListener = listener;
    }

    public interface OnStatusCallbackListener {


        void disconnect(String reason);

        void connecting();


        void authenticated();
    }
}
