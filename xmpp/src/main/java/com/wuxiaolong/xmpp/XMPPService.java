package com.wuxiaolong.xmpp;

import android.os.Message;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

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

    private static final String TAG = "wxl";
    public final static String HOST = "yax.im";
    private static final String SERVICE_NAME = "yax.im"; //sunchunlei.local labsun.com
    public final static int PORT = 5222;
    public static AbstractXMPPConnection mXMPPTCPConnection;
    private static SSLContext mSSLContext;
    private XMPPHandler mXMPPHandler;

    public XMPPService(XMPPHandler xmppHandler) {
        this.mXMPPHandler = xmppHandler;
    }

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
                    mXMPPHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "connect e=" + e.getMessage());
                }
            }
        }

        );
        thread.start();
    }

//    public void login(final String phone, final String password) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                Message message = new Message();
//                try {
//                    if (!mXMPPTCPConnection.isConnected())
//                        mXMPPTCPConnection.connect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    mXMPPTCPConnection.disconnect();
//                }
//                Log.i("wxl", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
//                if (mXMPPTCPConnection.isConnected()) {
//                    try {
//                        mXMPPTCPConnection.login(phone, password);
//                        message.what = AppConstants.LOGIN;
//                        if (mXMPPTCPConnection.isAuthenticated()) {
//                            message.obj = true;
//                            mXMPPHandler.sendMessage(message);
//                        } else {
//                            message.obj = false;
//                            mXMPPHandler.sendMessage(message);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();//
//                        message.obj = false;
//                        mXMPPHandler.sendMessage(message);
//                    }
//                    Log.i(TAG, "login=" + mXMPPTCPConnection.isAuthenticated());
//                } else {
//                    Log.i(TAG, "connect failed");
//                    message.obj = false;
//                    mXMPPHandler.handleMessage(message);
//                }
//
//            }
//        }).start();
//    }

    //    public void register(final String phone, final String password) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                Message message = new Message();
//                try {
//                    if (!mXMPPTCPConnection.isConnected())
//                        mXMPPTCPConnection.connect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    mXMPPTCPConnection.disconnect();
//                }
//                Log.i("wxl", "XMPPService connected=" + mXMPPTCPConnection.isConnected());
//                if (mXMPPTCPConnection.isConnected()) {
//                    String serviceName = mXMPPTCPConnection.getServiceName();
//                    try {
//                        AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
//                        Log.i("wxl", "Register supportsAccountCreation=" + accountManager.supportsAccountCreation());
//                        if (accountManager.supportsAccountCreation()) {
//                            accountManager.createAccount(phone + "@" + serviceName, password);
//                            message.what = AppConstants.REGISTER;
//                            message.obj = true;
//                            mXMPPHandler.sendMessage(message);
//                            Log.i("wxl", "注册成功");
//                        } else {
//                            Log.i("wxl", "服务端不能注册");
//                        }
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();//
//                        message.obj = false;
//                        mXMPPHandler.sendMessage(message);
//                    }
//                    Log.i(TAG, "login=" + mXMPPTCPConnection.isAuthenticated());
//                } else {
//                    Log.i(TAG, "connect failed");
//                    message.obj = false;
//                    mXMPPHandler.handleMessage(message);
//                }
//
//            }
//        }).start();
//    }
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


}
