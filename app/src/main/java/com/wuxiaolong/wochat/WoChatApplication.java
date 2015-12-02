package com.wuxiaolong.wochat;

import android.app.Application;
import android.content.Context;

import com.alibaba.wxlib.util.SysUtil;

/**
 * Created by WuXiaolong on 2015/12/2.
 */
public class WoChatApplication extends Application {
    //云旺OpenIM的DEMO用到的Application上下文实例
    private static Context sContext;
    public static String TAG = "wxl";

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Application.onCreate中，首先执行这部分代码, 因为，如果在":TCMSSevice"进程中，无需进行openIM和app业务的初始化，以节省内存
        //特别注意:这段代码不能封装到其他方法中，必须在onCreate顶层代码中!
        //以下代码固定在此处，不要改动
        SysUtil.setApplication(this);
        if (SysUtil.isTCMSServiceProcess(this)) {
            return;  //特别注意：此处return是退出onCreate函数，因此不能封装到其他任何方法中!
        }
        //以上代码固定在这个位置，不要改动

        sContext = getApplicationContext();
        //初始化云旺SDK
        InitHelper.initYWSDK(this);
    }
}
