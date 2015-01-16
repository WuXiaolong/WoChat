package com.xiaomolongstudio.wochat;

public interface IConnectionStatusCallback {
	public void connectionStatusChanged(int connectedState, String reason);
}
