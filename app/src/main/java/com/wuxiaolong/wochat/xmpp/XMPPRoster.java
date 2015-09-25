package com.wuxiaolong.wochat.xmpp;

import android.text.TextUtils;
import android.util.Log;

import com.wuxiaolong.wochat.mvp.model.ContactsModel;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public class XMPPRoster {
    static XMPPRoster mXMPPRoster;

    public static XMPPRoster getInstance() {
        if (mXMPPRoster == null) {
            mXMPPRoster = new XMPPRoster();
        }
        return mXMPPRoster;
    }


    public List<ContactsModel> getRoster() {
        XMPPService xmppService = XMPPService.getInstance();
        XMPPTCPConnection mXMPPTCPConnection = xmppService.getXmppTcpConnection();
        Roster roster = mXMPPTCPConnection.getRoster();
        Collection<RosterGroup> rosterGroups = roster.getGroups();
        List<ContactsModel> rosterList = new ArrayList<>();
        ContactsModel contactsModel = new ContactsModel();
        XMPPVCard xmppvCard = XMPPVCard.getInstance();
        for (RosterGroup rosterGroup : rosterGroups) {
            Log.d("wxl", "g==" + rosterGroup.getName());
            String groupName = rosterGroup.getName();
            Collection<RosterEntry> rosterEntries = rosterGroup.getEntries();
            for (RosterEntry rosterEntry : rosterEntries) {
                Log.d("wxl", "rosterEntry.getUser==" + rosterEntry.getUser());
                Log.d("wxl", "rosterEntry.getType().name()==" + rosterEntry.getType().name());

                if (TextUtils.equals("none", rosterEntry.getType().name())) {
                    String userJID = rosterEntry.getUser();
                    contactsModel.setGroupName(groupName);
                    contactsModel.setUserJID(userJID);
                    contactsModel.setUserNickName(xmppvCard.getUserNickName(userJID));
                    contactsModel.setUserAvatar(xmppvCard.getUserAvatar(userJID));
                    Log.d("wxl", "xmppvCard.getUserNickName(userJID)==" + xmppvCard.getUserNickName(userJID));
                    Log.d("wxl", "xmppvCard.getUserAvatar(userJID)==" + xmppvCard.getUserAvatar(userJID));

                    rosterList.add(contactsModel);
                }
            }
        }
        return rosterList;
    }


}
