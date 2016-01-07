package com.avoscloud.leanchatlib.event;

/**
 * Created by wli on 15/12/16.
 */
public class ConnectionChangeEvent {
  public boolean isConnect;
  public ConnectionChangeEvent(boolean isConnect) {
    this.isConnect = isConnect;
  }
}
