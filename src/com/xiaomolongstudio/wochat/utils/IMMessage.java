package com.xiaomolongstudio.wochat.utils;

import android.graphics.drawable.Drawable;

public class IMMessage {
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String KEY_TIME = "immessage.time";
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	private String chat_content;
	private String msg_from;
	private String nickname;
	private String msg_time;
	private Drawable avatar;

	public IMMessage() {
		super();
	}

	public Drawable getAvatar() {
		return avatar;
	}

	public void setAvatar(Drawable avatar) {
		this.avatar = avatar;
	}

	public String getChat_content() {
		return chat_content;
	}

	public void setChat_content(String chat_content) {
		this.chat_content = chat_content;
	}

	public String getMsg_from() {
		return msg_from;
	}

	public void setMsg_from(String msg_from) {
		this.msg_from = msg_from;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getMsg_time() {
		return msg_time;
	}

	public void setMsg_time(String msg_time) {
		this.msg_time = msg_time;
	}

	// @Override
	// public String toString() {
	// return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
	// }

}
