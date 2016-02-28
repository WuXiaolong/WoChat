package com.wuxiaolong.xmpp;

import android.os.Message;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by WuXiaolong
 * Date on 2016/2/23.
 */
public class XMPPService {

    private final String TAG = "wxl";
    public final String HOST = "yax.im";
    private final String SERVICE_NAME = "yax.im"; //sunchunlei.local labsun.com
    public final int PORT = 5222;
    public AbstractXMPPConnection mXMPPTCPConnection;
    private SSLContext mSSLContext;

    public AbstractXMPPConnection initXMPPTCPConnection() {
        SmackConfiguration.DEBUG = true;
        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[]{new MyTrustManager()}, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("connecting failed", e);
        }

        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
//        builder.setUsernameAndPassword(USER_NAME, PASSWORD);
        builder.setHost(HOST);
        builder.setServiceName(SERVICE_NAME);//此处只能使用域名或PC机器名称
        builder.setPort(PORT);
        builder.setCompressionEnabled(false);//连接套将使用流压缩。
//        builder.setConnectTimeout(5000);
//        builder.setDebuggerEnabled(true);
//        builder.setSendPresence(true);//上线通知系统
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);//安全模式
        builder.setCustomSSLContext(mSSLContext);////https不验证证书方式（信任所有证书）
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");
        mXMPPTCPConnection = new XMPPTCPConnection(builder.build());
        return mXMPPTCPConnection;
    }

    public void connect() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    message.what = AppConstants.CONNECT;
                    Log.i(TAG, "connect front");
                    if (mXMPPTCPConnection.isConnected()) {
                        Log.i(TAG, "connect successfull");
                        message.obj = "connect successfull";
                    } else {
                        mXMPPTCPConnection.connect();
                        if (mXMPPTCPConnection.isConnected()) {
                            message.obj = "connect successfull";
                        } else {
                            message.obj = "connect failed";
                        }

                    }

                    Log.i(TAG, "connect end=" + mXMPPTCPConnection.isConnected());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "connect e=" + e.getMessage());
                }
            }
        }

        );
        thread.start();
    }

    public void login(final String userName, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Message message = new Message();
                message.what = AppConstants.LOGIN;
                Log.i("wxl", "XMPPService login connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {
                        mXMPPTCPConnection.login(userName, password);

                        if (mXMPPTCPConnection.isAuthenticated()) {
                            Log.i(TAG, "登录成功");
                        } else {
                            Log.i(TAG, "登录失败");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "登录异常=" + e.getMessage());
                    }
                } else {
                    Log.i(TAG, "connect failed");
                }
                mXMPPClickListener.xmppCallback();
            }
        }).start();
    }

    public void register(final String userName, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Message message = new Message();
                message.what = AppConstants.REGISTER;
                Log.i("wxl", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    String serviceName = mXMPPTCPConnection.getServiceName();
                    try {
                        AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
                        Log.i("wxl", "Register supportsAccountCreation=" + accountManager.supportsAccountCreation());
                        if (accountManager.supportsAccountCreation()) {
                            accountManager.createAccount(userName + "@" + serviceName, password);
                            Log.i(TAG, "注册成功");
                        } else {
                            Log.i(TAG, "服务端不能注册");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "注册异常=" + e.getMessage());
                    }
                } else {
                    Log.i(TAG, "connect failed");
                }
            }
        }).start();
    }

    public void changePassword(final String newPassword) {
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("wxl", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {

                        if (mXMPPTCPConnection.isAuthenticated()) {
                            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
                            accountManager.changePassword(newPassword);
                            Log.i(TAG, "修改密码成功");
                        } else {
                            Log.i(TAG, "请先登录");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "修改密码异常=" + e.getMessage());
                    }
                } else {
                    Log.i(TAG, "connect failed");
                }
            }
        }).start();
    }

    public void setAvatar() {
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    if (!mXMPPTCPConnection.isConnected())
                        mXMPPTCPConnection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    mXMPPTCPConnection.disconnect();
                }
                Log.i("wxl", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
                if (mXMPPTCPConnection.isConnected()) {
                    try {
                        if (mXMPPTCPConnection.isAuthenticated()) {
//                            VCard vCard = VCardManager.loadVCard();

                        } else {
                            Log.i(TAG, "请先登录");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();//
                        Log.i(TAG, "修改密码异常=" + e.getMessage());
                    }
                } else {
                    Log.i(TAG, "connect failed");
                }
            }
        }).start();
    }

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
            Log.i(TAG, "checkClientTrusted:" + s);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
            Log.i(TAG, "checkServerTrusted:" + s);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            Log.i(TAG, "getAcceptedIssuers");
            return new X509Certificate[0];
        }

    }

    public XMPPClickListener mXMPPClickListener;

    public void setXMPPClickListener(XMPPClickListener xmppClickListener) {
        this.mXMPPClickListener = xmppClickListener;
    }

}
