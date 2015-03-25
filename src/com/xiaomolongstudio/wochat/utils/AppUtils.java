package com.xiaomolongstudio.wochat.utils;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class AppUtils {
	/**
	 * 获取用户名+@+域名
	 * 
	 * @param from
	 * @return
	 */
	public static String getJabberID(String from) {
		String[] res = from.split("/");
		return res[0].toLowerCase();
	}

	/**
	 * 根据user拼成ujid
	 * 
	 * @param user
	 * @return
	 */
	public static String splitJabberID(String user) {
		return user + "@" + AppConfig.XMPP_HOST;
	}
}
