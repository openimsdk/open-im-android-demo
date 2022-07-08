package io.openim.android.ouicontact.vm;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

public class FriendVM extends BaseViewModel {

    //封装过的好友信息 用于字母导航
    public MutableLiveData<List<ExUserInfo>> exUserInfo = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<String>> letters = new MutableLiveData<>(new ArrayList<>());

    public void getAllFriend() {
        exUserInfo.getValue().clear();
        letters.getValue().clear();
        OpenIMClient.getInstance().friendshipManager.getFriendList(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;

                List<ExUserInfo> exInfos = new ArrayList<>();
                List<ExUserInfo> otInfos = new ArrayList<>();
                for (UserInfo datum : data) {
                    ExUserInfo exUserInfo = new ExUserInfo();
                    exUserInfo.userInfo = datum;
                    String letter = "";
                    try {
                        letter = Pinyin.toPinyin(exUserInfo.userInfo.getFriendInfo().getNickname().charAt(0));
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
                letters.setValue(letters.getValue());

                exUserInfo.getValue().addAll(exInfos);
                exUserInfo.getValue().addAll(otInfos);

                exUserInfo.setValue(exUserInfo.getValue());
            }
        });
    }
}
