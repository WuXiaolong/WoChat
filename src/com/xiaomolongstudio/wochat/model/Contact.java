package com.xiaomolongstudio.wochat.model;

import android.graphics.drawable.Drawable;

public class Contact {

	String contactNick;
	String jid;
	Drawable avatar;

	public String getContactNick() {
		return contactNick;
	}

	public void setContactNick(String contactNick) {
		this.contactNick = contactNick;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Drawable getAvatar() {
		return avatar;
	}

	public void setAvatar(Drawable avatar) {
		this.avatar = avatar;
	}

}
