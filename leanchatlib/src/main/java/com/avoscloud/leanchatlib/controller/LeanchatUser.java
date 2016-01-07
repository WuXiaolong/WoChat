package com.avoscloud.leanchatlib.controller;


import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by wli on 15/9/30.
 */
public class LeanchatUser extends AVUser {

  public static final String USERNAME = "username";
  public static final String AVATAR = "avatar";
  public static final String LOCATION = "location";
  public static final String INSTALLATION = "installation";

  public static String getCurrentUserId () {
    LeanchatUser currentUser = getCurrentUser(LeanchatUser.class);
    return (null != currentUser ? currentUser.getObjectId() : null);
  }


  public String getAvatarUrl() {
    AVFile avatar = getAVFile(AVATAR);
    if (avatar != null) {
      return avatar.getUrl();
    } else {
      return null;
    }
  }


  public void saveAvatar(String path, final SaveCallback saveCallback) {
    final AVFile file;
    try {
      file = AVFile.withAbsoluteLocalPath(getUsername(), path);
      put(AVATAR, file);
      file.saveInBackground(new SaveCallback() {
        @Override
        public void done(AVException e) {
          if (null == e) {
            saveInBackground(saveCallback);
          } else {
            if (null != saveCallback) {
              saveCallback.done(e);
            }
          }
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static LeanchatUser getCurrentUser() {
    return getCurrentUser(LeanchatUser.class);
  }

  public void updateUserInfo() {
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    if (installation != null) {
      put(INSTALLATION, installation);
      saveInBackground();
    }
  }

  public AVGeoPoint getGeoPoint() {
    return getAVGeoPoint(LOCATION);
  }

  public void setGeoPoint(AVGeoPoint point) {
    put(LOCATION, point);
  }

  public static void signUpByNameAndPwd(String name, String password, SignUpCallback callback) {
    AVUser user = new AVUser();
    user.setUsername(name);
    user.setPassword(password);
    user.signUpInBackground(callback);
  }

  public void removeFriend(String friendId, final SaveCallback saveCallback) {
    unfollowInBackground(friendId, new FollowCallback() {
      @Override
      public void done(AVObject object, AVException e) {
        if (saveCallback != null) {
          saveCallback.done(e);
        }
      }
    });
  }

  public void findFriendsWithCachePolicy(AVQuery.CachePolicy cachePolicy, FindCallback<LeanchatUser>
      findCallback) {
    AVQuery<LeanchatUser> q = null;
    try {
      q = followeeQuery(LeanchatUser.class);
    } catch (Exception e) {
    }
    q.setCachePolicy(cachePolicy);
    q.setMaxCacheAge(TimeUnit.MINUTES.toMillis(1));
    q.findInBackground(findCallback);
  }
}
