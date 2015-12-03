package com.wuxiaolong.wochat;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.contact.IYWContact;
import com.alibaba.mobileim.contact.IYWContactHeadClickCallback;
import com.alibaba.mobileim.contact.IYWContactProfileCallback;
import com.alibaba.mobileim.contact.IYWContactService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 用户自定义昵称和头像
 *
 * @author zhaoxu
 */
public class UserProfileSampleHelper {

    private static final String TAG = UserProfileSampleHelper.class.getSimpleName();
    private static String
            json = "[" +
            " {\"name\":\"小羊\",\"avatar\":\"pic_1_03\"}," +
            " {\"name\":\"小汪\",\"avatar\":\"pic_1_05\"}," +
            " {\"name\":\"浅浅\",\"avatar\":\"pic_1_07\"}," +
            " {\"name\":\"赫赫\",\"avatar\":\"pic_1_09\"}," +
            " {\"name\":\"美美\",\"avatar\":\"pic_1_11\"}," +
            " {\"name\":\"衣衣\",\"avatar\":\"pic_1_18\"}," +
            " {\"name\":\"鱼鱼\",\"avatar\":\"pic_1_19\"}," +
            " {\"name\":\"诗诗\",\"avatar\":\"pic_1_20\"}," +
            " {\"name\":\"张张\",\"avatar\":\"pic_1_29\"}," +
            " {\"name\":\"大梅\",\"avatar\":\"pic_1_31\"}," +
            " {\"name\":\"老云\",\"avatar\":\"pic_1_32\"}," +
            " {\"name\":\"小任\",\"avatar\":\"pic_1_33\"}," +
            " {\"name\":\"兰兰\",\"avatar\":\"pic_1_34\"}," +
            " {\"name\":\"大师\",\"avatar\":\"pic_1_40\"}," +
            " {\"name\":\"大任\",\"avatar\":\"pic_1_41\"}," +
            " {\"name\":\"色色\",\"avatar\":\"pic_1_42\"}," +
            " {\"name\":\"鬼鬼\",\"avatar\":\"pic_1_50\"}," +
            " {\"name\":\"饱饱\",\"avatar\":\"pic_1_51\"}," +
            " {\"name\":\"多多\",\"avatar\":\"pic_1_52\"}," +
            " {\"name\":\"唬唬\",\"avatar\":\"pic_1_53\"}" +
            " ]";
    private static List<User> users = new ArrayList<User>();

    private static void init() {
        users.clear();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                User tempUser = new User();
                JSONObject tempJsonObj = array.getJSONObject(i);
                tempUser.setName(tempJsonObj.getString("name"));
                tempUser.setAvatar(tempJsonObj.getString("avatar"));
                users.add(tempUser);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

    }

    private static boolean enableUserProfile = true;

    //初始化，建议放在登录之前
    public static void initProfileCallback() {
        if (!enableUserProfile){
            //目前SDK会自动获取导入到OpenIM的帐户昵称和头像，如果用户设置了回调，则SDK不会从服务器获取昵称和头像
            return;
        }
        init();
        YWIMKit imKit = LoginSampleHelper.getInstance().getIMKit();
        IYWContactService contactManager = imKit.getIMCore().getContactService();
        //头像点击的回调（开发者可以按需设置）
        contactManager.setContactHeadClickCallback(new IYWContactHeadClickCallback() {
            @Override
            public Intent onShowProfileActivity(String userId, String appKey) {
                Toast.makeText(WoChatApplication.getContext(), String.format("你点击了 %s 的头像哦~", userId), Toast.LENGTH_SHORT).show();
                return null;
            }
        });

        contactManager.setContactProfileCallback(new IYWContactProfileCallback() {

            @Override
            public Intent onShowProfileActivity(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            //此方法会在SDK需要显示头像和昵称的时候，调用。同一个用户会被多次调用的情况。
            //比如显示会话列表，显示聊天窗口时同一个用户都会被调用到。
            @Override
            public IYWContact onFetchContactInfo(String arg0) {
                // TODO Auto-generated method stub
                // 开发者需要根据不同的用户ID显示不同的昵称和头像。
                try {
                    String userid = arg0;
                    return modifyUserInfo(userid);
                } catch (Exception e) {

                }
                return null;
            }
        });
    }

    /**
     * 设定希望显示的用户昵称和头像
     * 头像支持本地路径和URL路径以及资源ID号。重要：头像图片最好小于10K，否则第一次加载可能会有图像压缩的延时,URL路径还有下载的延时
     *
     * 注意本地路径，需要以pic_1_开头
     * @param userid   用户ID
     * @return
     */
    private static UserInfo modifyUserInfo(String userid) {
        long num;
        // 提取数字
        String characs = Pattern.compile("[1-9]\\d*").matcher(userid).replaceAll("");
        String shoreUserid = userid.substring(characs.length());
        //根据用户id末两位数字生成昵称和头像地址
        YWLog.d(TAG, "short Userid = " + shoreUserid);
        try {
            num = Long.valueOf(shoreUserid);
        } catch (Exception e) {
            //末尾不是数字，随机一个数字
            num = new Random().nextInt(20);
        }
        //模json的长度，来匹配json中储存的头像和昵称
        long modNum = num % 20;
        UserInfo userInfo = mUserInfo.get(userid);
        if (userInfo == null) {
            if (userid.startsWith("百川测试111")) { //客服账号特殊处理
                userInfo = new UserInfo("openim官方客服", "pic_1_cs");
            } else if (userid.startsWith("百川开发者大会小秘书")) {  //客服账号特殊处理
                userInfo = new UserInfo(userid, "pic_1_bc");
            } else if (userid.equals("云大旺") || userid.equals("云二旺") || userid.equals("云三旺") || userid.equals("云四旺") || userid.equals("云小旺")) {
                //支持资源ID号的方式
                userInfo = new UserInfo(userid, R.mipmap.ic_launcher);
            } else {
                // 为了方便体验，这里只做了一个简单的字符串连接作为用户的昵称。开发者可以根据自己的需要，用其他方式做映射（例如 app 的 profile服务器）
                userInfo = new UserInfo("昵称-" + userid,
                        /*new StringBuilder(users.get((int) modNum).getName()).append("_").append(WXUtil.getMD5Value(userid).substring(WXUtil.getMD5Value(userid).length() - 2).toUpperCase()).toString(),*/
                        new StringBuilder(users.get((int) modNum).getAvatar()).toString());
            }

            mUserInfo.put(userid, userInfo);
        }
        return userInfo;
    }

    // 这个只是个示例，开发者需要自己管理用户昵称和头像
    private static Map<String, UserInfo> mUserInfo = new HashMap<String, UserInfo>();

    private static class UserInfo implements IYWContact {

        private String mUserNick;    // 用户昵称
        private String mAvatarPath;    // 用户头像URL

        private int mLocalResId;//主要用于本地资源


        public UserInfo(String nickName, String avatarPath) {
            this.mUserNick = nickName;
            this.mAvatarPath = avatarPath;
        }

        public UserInfo(String nickName, int resId) {
            this.mUserNick = nickName;
            this.mLocalResId = resId;
        }

        @Override
        public String getAppKey() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getAvatarPath() {
            // TODO Auto-generated method stub
            if (mLocalResId != 0){
                return mLocalResId + "";
            }else{
                return mAvatarPath;
            }
        }

        @Override
        public String getShowName() {
            // TODO Auto-generated method stub
            return mUserNick;
        }

        @Override
        public String getUserId() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static class User {
        private String name;
        private String avatar;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
