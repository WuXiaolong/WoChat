package com.xiaomolongstudio.wochat.xmpp;

public interface IConnectionStatusCallback {
	public void connectionStatusChanged(int connectedState, String reason);
}
