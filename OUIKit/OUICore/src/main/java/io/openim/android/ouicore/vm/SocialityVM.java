package io.openim.android.ouicore.vm;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.CommEx;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.entity.SortLetter;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;


public class SocialityVM extends BaseViewModel {
    //封装过的好友信息 用于字母导航
    public MutableLiveData<List<ExUserInfo>> exUserInfo = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<String>> letters = new MutableLiveData<>(new ArrayList<>());
    //我加入的群
    public MutableLiveData<List<GroupInfo>> groups = new MutableLiveData<>(new ArrayList<>());
    //我创建的群
    public MutableLiveData<List<GroupInfo>> ownGroups = new MutableLiveData<>(new ArrayList<>());


    public void getAllGroup() {
        OpenIMClient.getInstance().groupManager.getJoinedGroupList(new OnBase<List<GroupInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + "(" + code + ")");
            }

            @Override
            public void onSuccess(List<GroupInfo> data) {
                if (data.isEmpty()) return;
                groups.setValue(data);
                for (GroupInfo datum : data) {
                    if (datum.getCreatorUserID().equals(BaseApp.inst()
                        .loginCertificate.userID)) {
                        ownGroups.getValue().add(datum);
                    }
                }
                if (!ownGroups.getValue().isEmpty())
                    ownGroups.setValue(ownGroups.getValue());
            }
        });
    }


    public void getAllFriend() {
        exUserInfo.getValue().clear();
        letters.getValue().clear();
        OpenIMClient.getInstance().friendshipManager.getFriendList(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                getIView().toast(error + "(" + code + ")");
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;

                List<ExUserInfo> exInfos = new ArrayList<>();
                //不在24个字母中的开头
                List<ExUserInfo> otInfos = new ArrayList<>();
                for (UserInfo datum : data) {
                    ExUserInfo exUserInfo = new ExUserInfo();
                    exUserInfo.userInfo = datum;
                    String letter = "";
                    try {
                        letter = String.valueOf(Pinyin.toPinyin(exUserInfo.userInfo
                            .getFriendInfo().getNickname().charAt(0)).charAt(0));
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
                    if (!letters.getValue().contains(userInfo.sortLetter))
                        letters.getValue().add(userInfo.sortLetter);
                }
                if (!otInfos.isEmpty())
                    letters.getValue().add("#");

                Collections.sort(letters.getValue(), new LettersPinyinComparator());
                letters.setValue(letters.getValue());

                exUserInfo.getValue().addAll(exInfos);
                exUserInfo.getValue().addAll(otInfos);
                Collections.sort(exUserInfo.getValue(), new PinyinComparator());
                exUserInfo.setValue(exUserInfo.getValue());
            }
        });
    }


    public static class PinyinComparator implements Comparator<CommEx> {

        public int compare(CommEx o1, CommEx o2) {
            //根据ABCDEFG...来排序
            if (o1.sortLetter.equals("#")) {
                return 1;
            } else if (o2.sortLetter.equals("#")) {
                return -1;
            } else {
                return o1.sortLetter.compareTo(o2.sortLetter);
            }
        }
    }

    public static  class LettersPinyinComparator implements Comparator<String> {

        public int compare(String o1, String o2) {
            //根据ABCDEFG...来排序
            if (o1.equals("#")) {
                return 1;
            } else if (o2.equals("#")) {
                return -1;
            } else {
                return o1.compareTo(o2);
            }
        }
    }

}
