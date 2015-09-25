package com.wuxiaolong.wochat.xmpp;

import android.graphics.drawable.Drawable;

import com.wuxiaolong.wochat.utils.FormatTools;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.ByteArrayInputStream;

/**
 * Created by WuXiaolong on 2015/9/25.
 */
public class XMPPVCard {
    static XMPPVCard mXMPPVCard;

    public static XMPPVCard getInstance() {
        if (mXMPPVCard == null) {
            mXMPPVCard = new XMPPVCard();
        }
        return mXMPPVCard;
    }

    /**
     * 获取用户头像信息
     *
     * @return
     */
    public Drawable getUserAvatar(String userJID) {
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            VCard vcard;
            vcard = getVCard(userJID);
            if (vcard != null && vcard.getAvatar() == null) {
                byteArrayInputStream = new ByteArrayInputStream(vcard.getAvatar());
                return FormatTools.getInstance().InputStream2Drawable(byteArrayInputStream);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserNickName(String userJID) {
        try {
            VCard vcard;
            vcard = getVCard(userJID);
            if (vcard != null) {
                return vcard.getNickName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public VCard getVCard(String userJID) {
        XMPPService xmppService = XMPPService.getInstance();
        XMPPTCPConnection mXMPPTCPConnection = xmppService.getXmppTcpConnection();
        VCard vCard = null;
        try {
            vCard = new VCard();
            vCard.load(mXMPPTCPConnection, userJID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vCard;
    }
}
