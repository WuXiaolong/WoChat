package com.avoscloud.leanchatlib.controller;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WuXiaolong on 2015/12/21.
 */
public class ThirdPartUserUtils {


    private static ThirdPartUserUtils userUtils;
    private static ThirdPartDataProvider thirdPartDataProvider;

    private ThirdPartUserUtils() {}

    public static synchronized ThirdPartUserUtils getInstance() {
        if (null == userUtils) {
            userUtils = new ThirdPartUserUtils();
        }
        return userUtils;
    }

    public static void setThirdPartUserProvider(ThirdPartDataProvider provider) {
        thirdPartDataProvider = provider;
    }

    public ThirdPartUser getSelf() {
        checkDataProvider();
        return thirdPartDataProvider.getSelf();
    }

    public String getUserName(String userId) {
        ThirdPartUser user = getFriend(userId);
        return (null != user ? user.name : "");
    }

    public String getUserAvatar(String userId) {
        ThirdPartUser user = getFriend(userId);
        return (null != user ? user.avatarUrl : "");
    }

    private ThirdPartUser getFriend(String userId) {
        checkDataProvider();
        if (ThirdPartDataCache.getInstance().hasCachedUser(userId)) {
            return ThirdPartDataCache.getInstance().getCachedUser(userId);
        } else {
            refreshUserData(Arrays.asList(userId));
            return null;
        }
    }

    public void getFriends(int skip, int limit, final FetchUserCallBack callBack) {
        checkDataProvider();
        thirdPartDataProvider.getFriends(skip, limit, new FetchUserCallBack() {
            @Override
            public void done(List<ThirdPartUser> userList, Exception e) {
                if (null == e && null != userList) {
                    for (ThirdPartUser user : userList) {
                        ThirdPartDataCache.getInstance().cacheUser(user.userId, user);
                    }
                }
                callBack.done(userList, e);
            }
        });
    }

    public void refreshUserData(List<String> userList) {
        thirdPartDataProvider.getFriends(userList, new FetchUserCallBack() {
            @Override
            public void done(List<ThirdPartUser> userList, Exception e) {
                if (null == e && null != userList) {
                    for (ThirdPartUser user : userList) {
                        ThirdPartDataCache.getInstance().cacheUser(user.userId, user);
                    }
                }
            }
        });
    }

    private void checkDataProvider() {
        if (null == thirdPartDataProvider) {
            throw new NullPointerException("thirdPartDataProvider is null，please setThirdPartUserProvider first!");
        }
    }

    public static class ThirdPartUser {
        public String userId;
        public String avatarUrl;
        public String name;

        public ThirdPartUser(String id, String name, String avatar) {
            userId = id;
            this.name = name;
            avatarUrl = avatar;
        }
    }

    public interface ThirdPartDataProvider {
        //TODO need check self id
        public ThirdPartUser getSelf();
        public void getFriend(String userId, FetchUserCallBack callBack);
        public void getFriends(List<String> list, FetchUserCallBack callBack);
        public void getFriends(int skip, int limit, FetchUserCallBack callBack);
    }

    public interface FetchUserCallBack {
        public void done(List<ThirdPartUser> userList, Exception e);
    }
}
