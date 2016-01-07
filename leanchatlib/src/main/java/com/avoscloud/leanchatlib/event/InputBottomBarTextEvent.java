package com.avoscloud.leanchatlib.event;

/**
 * Created by WuXiaolong on 2015/12/18.
 */
public class InputBottomBarTextEvent extends InputBottomBarEvent {

    /**
     * 发送的文本内容
     */
    public String sendContent;

    public InputBottomBarTextEvent(int action, String content, Object tag) {
        super(action, tag);
        sendContent = content;
    }


}
