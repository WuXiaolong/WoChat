package com.xiaomolongstudio.wochat.utils;

import android.graphics.drawable.Drawable;

public class IMMessage {
	public static final String IMMESSAGE_KEY = "immessage.key";
	public static final String KEY_TIME = "immessage.time";
	public static final String CHAT_CONTENT = "chat_content";
	public static final String MSG_FROM = "msg_from";
	public static final String NICKNAME = "nickname";
	public static final String MSG_TIME = "msg_time";
	public static final String AVATAR = "avatar";
	public static final String ROOMID = "roomId";
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	private String chatContent;
	private String msgFrom;
	private String roomId;
	private String nickname;
	private String msgTime;
	private byte[] avatar;
	private Drawable drawableAvatar;

	public IMMessage() {
		super();
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public Drawable getDrawableAvatar() {
		return drawableAvatar;
	}

	public void setDrawableAvatar(Drawable drawableAvatar) {
		this.drawableAvatar = drawableAvatar;
	}

	public byte[] getAvatar() {
		return avatar;
	}

	public void setAvatar(byte[] avatar) {
		this.avatar = avatar;
	}


	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getChatContent() {
		return chatContent;
	}

	public void setChatContent(String chatContent) {
		this.chatContent = chatContent;
	}

	public String getMsgFrom() {
		return msgFrom;
	}

	public void setMsgFrom(String msgFrom) {
		this.msgFrom = msgFrom;
	}

	public String getMsgTime() {
		return msgTime;
	}

	public void setMsgTime(String msgTime) {
		this.msgTime = msgTime;
	}


	// @Override
	// public String toString() {
	// return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
	// }

}
