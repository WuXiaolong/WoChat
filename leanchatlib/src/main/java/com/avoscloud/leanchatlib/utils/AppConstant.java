package com.avoscloud.leanchatlib.utils;

/**
 * Created by WuXiaolong on 2015/12/11：16：27.
 */
public class AppConstant {
    public static final String FRAGMENT_TAG_HOTCHAT = "HotChatFragment";
    public static final String FRAGMENT_TAG_FRIEND = "FriendFragment";
    public static final String FRAGMENT_TAG_LISTENING = "ListeningFragment";
    public static final String FRAGMENT_TAG_PROGRAM = "ProgramFragment";
    public static final String FRAGMENT_TAG_SET = "SetFragment";
    public static final String FRAGMENT_TAG_LOGIN = "LoginFragment";
    public static final String FRAGMENT_TAG_REGISTER = "RegisterFragment";
    public static final String[] FRAGMENT_TAGS = new String[]{FRAGMENT_TAG_HOTCHAT, FRAGMENT_TAG_FRIEND,
            FRAGMENT_TAG_LISTENING, FRAGMENT_TAG_PROGRAM, FRAGMENT_TAG_SET};
    public static final String SMS_APPKEY = "d67e1787d4ee";
    public static final String SMS_APPSECRET = "f0b135a7af367e8d9fea15519f3260a0";
    public static final String REGISTER_ID = "id";
    public static final String REGISTER_PHONE = "phone";
    public static final String REGISTER_PASSWORD = "password";
    public static final String USER_ID = "id";
    public static final String ISTHIRD = "isThird";
    public static final String PHONE = "phone";
    public static final String PASSWORD = "password";
    public static final String REGISTER_THIRDPARTYID = "thirdpartyid";
    public static final String OBJECT_ID = "objectId";
    private static final String LEANMESSAGE_CONSTANTS_PREFIX = "com.leancloud.im.guide";
    public static final String CONVERSATION_ID = getPrefixConstant("conversation_id");
    public static final String ACTIVITY_TITLE = getPrefixConstant("activity_title");
    public static final String ROOM_ID ="roomId";
    public static final String MEMBER_ID = getPrefixConstant("member_id");
    public static final String SQUARE_CONVERSATION_ID = "567395b74d85f3e543bc62d4";

    private static String getPrefixConstant(String str) {
        return LEANMESSAGE_CONSTANTS_PREFIX + str;
    }

    public static final String ROOM_NAME = "name";
    public static final String IS_ONLY_FRIEND = "isOnlyFriend";
    public static final int IMAGE_PICK_REQUEST = 1;
    public static final int CROP_REQUEST = 2;
    public static final String NOTOFICATION_TAG = getPrefixConstant("notification_tag");
    public static final String NOTIFICATION_SINGLE_CHAT = AppConstant.getPrefixConstant("notification_single_chat");
    public static final String NOTIFICATION_GROUP_CHAT = AppConstant.getPrefixConstant("notification_group_chat");
    public static final String NOTIFICATION_SYSTEM = AppConstant.getPrefixConstant("notification_system_chat");
    /**
     * HotChatFragment
     */
    public static final String HOT_LIST = "hotProgramDomainList";
    public static final String HOT_CHANNEL = "channel";
    public static final String HOT_TITLE = "title";
    public static final String HOT_COUNT = "hotspot";
    public static final String HOT_CHANNELID = "channelID";
    public static final String HOT_KEY_CHANNEL = "hot_channel";
    public static final String HOT_KEY_TITLE = "hot_title";
    public static final String HOT_KEY_COUNT = "hot_count";
    public static final String HOT_KEY_CHANNELID = "hot_channel_id";
    public static final String FRIEND_DATE = "date";
    public static final String FRIEND_CHANNEL = "channel";
    public static final String FRIEND_CHANNEL_ID = "channelId";
    public static final String FRIEND_PROGRAM_ID = "programID";
    public static final String FRIEND_PROGRAM_NAME = "programname";
    public static final String FRIEND_USER_ID = "userid";
    public static final String FRIEND_IS_TRAILER = "istrailer";
    public static final String PHONE_NUMBER_NAME = "phoneNumberName";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String PHONE_CONTACT_AVATAR = "phoneContactAvatar";
    public static final String PHONE_USER = "phoneUser";
    public static final String PHONE_IS_FRIEND = "isFriend";
}
