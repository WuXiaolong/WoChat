package com.wuxiaolong.wochat;

import android.app.Application;

import com.alibaba.mobileim.YWAPI;

/**
 * SDK 全局初始化
 * 
 * @author shuheng
 */
public class InitHelper {

	public static void initYWSDK(Application application){
		//TODO 注意：--------------------------------------
		//  以下步骤调用顺序有严格要求，请按照示例的步骤（todo step）
		// 的顺序调用！
		//TODO --------------------------------------------

		// ------[todo step1]-------------
		//［IM定制初始化］，如果不需要定制化，可以去掉此方法的调用
		//注意：由于增加全局初始化，该配置需最先执行

		CustomSampleHelper.initCustom();

		// ------[todo step2]-------------
		//SDK初始化
		LoginSampleHelper.getInstance().initSDK_Sample(application);

		//后期将使用Override的方式进行集中配置，请参照YWSDKGlobalConfigSample
		YWAPI.enableSDKLogOutput(true);

//		IYWContactService.enableBlackList();

//		YWAPI.setEnableAutoLogin(false);
	}
}
