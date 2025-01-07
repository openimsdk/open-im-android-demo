package io.openim.android.ouicore.vm;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.ex.CommEx;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;


public class SocialityVM extends BaseViewModel {
    //封装过的好友信息 用于字母导航
    private static final String TAG = "SocialityVM";
    public MutableLiveData<List<ExUserInfo>> exUserInfo = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<String>> letters = new MutableLiveData<>(new ArrayList<>());
    //我加入的群
    public MutableLiveData<List<GroupInfo>> groups = new MutableLiveData<>(new ArrayList<>());
    //我创建的群
    public MutableLiveData<List<GroupInfo>> ownGroups = new MutableLiveData<>(new ArrayList<>());

    private List<ExUserInfo> tempExUserInfo = new ArrayList<>();
    private List<String> tempLetters = new ArrayList<>();
    private List<GroupInfo> tempGroups = new ArrayList<>();
    private List<GroupInfo> tempOwnGroups = new ArrayList<>();

    private int groupOffset = 0;
    private int friendOffset = 0;
    private final int groupCount = 300;
    private final int friendCount = 500;

    private void fetchGroups() {
        OpenIMClient.getInstance().groupManager.getJoinedGroupListPage(new OnBase<List<GroupInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + "(" + code + ")");
                Log.e(TAG, "fetchGroups onError:" + error + "(" + code + ")");
            }

            @Override
            public void onSuccess(List<GroupInfo> data) {
                if (data.isEmpty()) {
                    return;
                }
                tempGroups.addAll(data);
                for (GroupInfo datum : data) {
                    if (datum.getCreatorUserID().equals(BaseApp.inst().loginCertificate.userID)) {
                        tempOwnGroups.add(datum);
                    }
                }

                if (data.size() < groupCount) {
                    groups.setValue(tempGroups);
                    ownGroups.setValue(tempOwnGroups);
                } else {
                    groupOffset += groupCount;
                    fetchGroups();
                }
                Log.d(TAG, "fetchGroups data.size():" + data.size());
            }
        }, groupOffset, groupCount);
    }
    public void getAllGroup() {
        Log.d(TAG, "getAllGroup()");
        fetchGroups();
    }

    private void fetchFriends() {
        OpenIMClient.getInstance().friendshipManager.getFriendListPage(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + "(" + code + ")");
                Log.e(TAG, "fetchFriends onError:" + error + "(" + code + ")");
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) {
                    return;
                }

                List<ExUserInfo> exInfos = new ArrayList<>();
                List<ExUserInfo> otInfos = new ArrayList<>();
                for (UserInfo datum : data) {
                    ExUserInfo exUserInfo = new ExUserInfo();
                    exUserInfo.userInfo = datum;
                    if (!TextUtils.isEmpty(datum.getRemark())) {
                        exUserInfo.userInfo.setNickname(datum.getRemark());
                    }
                    String letter = "";
                    try {
                        letter = String.valueOf(Pinyin.toPinyin(exUserInfo.userInfo
                            .getNickname().charAt(0)).charAt(0));
                        letter=letter.toUpperCase(Locale.ROOT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(letter) || !Common.isAlpha(letter)) {
                        exUserInfo.sortLetter = "#";
                        otInfos.add(exUserInfo);
                    } else {
                        exUserInfo.sortLetter = letter;
                        exInfos.add(exUserInfo);
                    }
                }

                for (ExUserInfo userInfo : exInfos) {
                    if (!tempLetters.contains(userInfo.sortLetter)) {
                        tempLetters.add(userInfo.sortLetter);
                    }
                }
                if (!otInfos.isEmpty()) {
                    tempLetters.add("#");
                }

                tempExUserInfo.addAll(exInfos);
                tempExUserInfo.addAll(otInfos);

                if (data.size() < friendCount) {
                    Collections.sort(tempLetters, new LettersPinyinComparator());
                    Collections.sort(tempExUserInfo, new PinyinComparator());
                    exUserInfo.getValue().clear();
                    letters.getValue().clear();
                    letters.setValue(tempLetters);
                    exUserInfo.setValue(tempExUserInfo);
                } else {
                    friendOffset += friendCount;
                    fetchFriends();
                }
                Log.d(TAG, "fetchFriends data.size():" + data.size() + ",friendOffset:" + friendOffset);
            }
        }, friendOffset, friendCount, true);
    }
    public void getAllFriend() {
        Log.d(TAG, "getAllFriend()");
        fetchFriends();
    }

    public static class PinyinComparator implements Comparator<CommEx> {

        public int compare(CommEx o1, CommEx o2) {
            //根据ABCDEFG...来排序
            String sortLetter1 = o1.sortLetter;
            String sortLetter2 = o2.sortLetter;

            // 检查字符串是否为空，防止 charAt 异常
            if (sortLetter1 == null || sortLetter1.isEmpty()) {
                sortLetter1 = "#";
            }
            if (sortLetter2 == null || sortLetter2.isEmpty()) {
                sortLetter2 = "#";
            }

            if (sortLetter1.equals("#") && sortLetter2.equals("#")) {
                return 0;
            } else if (sortLetter1.equals("#")) {
                return 1;
            } else if (sortLetter2.equals("#")) {
                return -1;
            } else {
                return sortLetter1.compareTo(sortLetter2);
            }
        }
    }

    public static  class LettersPinyinComparator implements Comparator<String> {

        public int compare(String o1, String o2) {
            //根据ABCDEFG...来排序
            if (o1 == null || o1.isEmpty()) {
                o1 = "#";
            }
            if (o2 == null || o2.isEmpty()) {
                o2 = "#";
            }

            if (o1.equals("#") && o2.equals("#")) {
                return 0;
            } else if (o1.equals("#")) {
                return 1;
            } else if (o2.equals("#")) {
                return -1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }

}
