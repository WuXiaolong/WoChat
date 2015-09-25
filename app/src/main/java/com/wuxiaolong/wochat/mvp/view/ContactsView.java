package com.wuxiaolong.wochat.mvp.view;

import com.wuxiaolong.wochat.mvp.model.ContactsModel;

import java.util.List;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public interface ContactsView {
    void rosterList(List<ContactsModel> rosterList);

    void progressShow();

    void progressDismiss();
}
