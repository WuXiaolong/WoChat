package com.xiaomolongstudio.wochat.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * 常量
 * 
 * @author wxl
 * 
 */
public class AppConfig {
	public static final String XMPP_HOST = "192.168.2.8";
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String KEY_TIME = "immessage.time";
	public static List<Activity> TYPE_ACTIVITY = new ArrayList<Activity>();
	public static final int REGISTER = 100;
	public static final int CHAT_ROOM_MESSAGE = 200;
	public static final int GET_VCARD_MESSAGE = 300;
	public static final int USER_HEAD = 400;
	public static int PHOTO_CUTTING = 500;
	public static final String LOGIN_ACTION = "cn.cntv.tvt.action.LOGIN";
	public static final String REGISTER_ACTION = "cn.cntv.tvt.action.REGISTER";
	public static final String CHATROOM_ACTION = "cn.cntv.tvt.action.CHATROOM";
	public static final String FRIEND_MSG_ACTION = "cn.cntv.tvt.action.FRIEND_MSG";
	public static final String MAIN_ACTION = "cn.cntv.tvt.action.MAIN";
}
