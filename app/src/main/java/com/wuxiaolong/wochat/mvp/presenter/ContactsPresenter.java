package com.wuxiaolong.wochat.mvp.presenter;

import com.wuxiaolong.wochat.mvp.model.ContactsModel;
import com.wuxiaolong.wochat.mvp.view.ContactsView;
import com.wuxiaolong.wochat.xmpp.XMPPRoster;

import java.util.List;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public class ContactsPresenter implements Presenter<ContactsView> {
    ContactsView mContactsView;

    @Override
    public void attachView(ContactsView view) {
        this.mContactsView = view;
    }

    @Override
    public void detachView() {
        this.mContactsView = null;
    }

    public void getRoster() {
        mContactsView.progressShow();
        XMPPRoster xmppRoster = XMPPRoster.getInstance();
        List<ContactsModel> rosterList = xmppRoster.getRoster();
        mContactsView.rosterList(rosterList);
        mContactsView.progressDismiss();
    }
}
