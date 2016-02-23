package com.wuxiaolong.xmpp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by WuXiaolong
 * Date on 2016/2/23.
 */
public class XMPPHandler extends Handler {
    public XMPPClickListener mXMPPClickListener;
    private static final String TAG = "wxl";

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String text = (String) msg.obj;
        switch (msg.what) {
            case AppConstants.CONNECT:
                mXMPPClickListener.connect(text);
                break;
            case AppConstants.LOGIN:

                break;
            case AppConstants.REGISTER:

                break;
        }
    }


    public void setXMPPClickListener(XMPPClickListener xmppClickListener) {
        Log.i(TAG, "setXMPPClickListener=" + xmppClickListener);
        this.mXMPPClickListener = xmppClickListener;
    }
}
