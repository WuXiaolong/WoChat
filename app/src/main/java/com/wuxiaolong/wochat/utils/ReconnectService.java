package com.wuxiaolong.wochat.utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;


/**
 * Created by WuXiaolong on 2015/6/16.
 */
public class ReconnectService extends Service {
    private ReconnectBinder mBinder = new ReconnectBinder();
    private Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //要做的事情
            String user_name = PreferenceUtils.getPrefString(ReconnectService.this, PreferenceConstants.USER_NAME, "");
            String password = PreferenceUtils.getPrefString(ReconnectService.this, PreferenceConstants.PASSWORD, "");
//            Log.d("wxl", "isAuthenticated=" + XMPPService.getXmppTcpConnection().isAuthenticated());
//            mOnStatusCallbackListener.disconnect(XMPPService.getXmppTcpConnection().isAuthenticated() + "");
            handler.postDelayed(this, 3000);
        }
    };

    /**
     * 只会在Service第一次被创建的时候调用
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("wxl", "onCreate() executed");
//        mXMPPBroadcastReceiver = new XMPPBroadcastReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(mXMPPBroadcastReceiver, filter);
//
//        mXMPPBroadcastReceiver.setOnEventHandlerListener(new XMPPBroadcastReceiver.OnEventHandlerListener() {
//            @Override
//            public void onNetChange() {
//                mOnStatusCallbackListener.connected();
//            }
//        });
    }

    XMPPBroadcastReceiver mXMPPBroadcastReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("wxl", "onStartCommand() executed");
//        handler.postDelayed(runnable, 3000);

        return super.onStartCommand(intent, flags, startId);
    }


    static OnStatusCallbackListener mOnStatusCallbackListener;

    public static void setOnStatusCallbackListener(OnStatusCallbackListener listener) {
        mOnStatusCallbackListener = listener;
    }

    public interface OnStatusCallbackListener {


        void disconnect(String reason);

        void connecting();

        void connected();

        void authenticated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("wxl", "onDestroy() executed");
        unregisterReceiver(new XMPPBroadcastReceiver());
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    public class ReconnectBinder extends Binder {

        public ReconnectService getService() {
            return ReconnectService.this;
        }

    }
}
