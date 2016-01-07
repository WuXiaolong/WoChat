package com.avoscloud.leanchatlib.controller;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMClientEventHandler;
import com.avoscloud.leanchatlib.event.ConnectionChangeEvent;

import de.greenrobot.event.EventBus;


/**
 * Created by wli on 15/12/16.
 * 与网络相关的 handler
 * 注意，此 handler 并不是网络状态通知，而是当前 client 的连接状态
 */
public class LeanchatClientEventHandler extends AVIMClientEventHandler {

  private static LeanchatClientEventHandler eventHandler;

  public static synchronized LeanchatClientEventHandler getInstance() {
    if (null == eventHandler) {
      eventHandler = new LeanchatClientEventHandler();
    }
    return eventHandler;
  }

  private LeanchatClientEventHandler() {}


  private volatile boolean connect = false;

  /**
   * 是否连上聊天服务
   *
   * @return
   */
  public boolean isConnect() {
    return connect;
  }

  public void setConnectAndNotify(boolean isConnect) {
    connect = isConnect;
    EventBus.getDefault().post(new ConnectionChangeEvent(connect));
  }

  @Override
  public void onConnectionPaused(AVIMClient avimClient) {
    setConnectAndNotify(false);
  }

  @Override
  public void onConnectionResume(AVIMClient avimClient) {
    setConnectAndNotify(true);
  }

  @Override
  public void onClientOffline(AVIMClient avimClient, int i) {

  }
}
