package io.openim.android.ouicontact.vm;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.sdk.models.GroupInfo;

public class SearchGroup extends BaseViewModel {
    //我加入的群
    public MutableLiveData<List<GroupInfo>> groups = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<GroupInfo>> searchGroups = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> searchKey = new MutableLiveData<>();

    public void search(String key) {
        searchGroups.getValue().clear();
        if (!TextUtils.isEmpty(key)) {
            for (GroupInfo groupInfo : groups.getValue()) {
                if (groupInfo.getGroupName().toUpperCase().contains(key.toUpperCase())) {
                    searchGroups.getValue().add(groupInfo);
                }
            }
        }
        searchGroups.setValue(searchGroups.getValue());
    }
}
