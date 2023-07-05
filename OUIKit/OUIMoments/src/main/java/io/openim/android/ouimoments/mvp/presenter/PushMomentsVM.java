package io.openim.android.ouimoments.mvp.presenter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.net.RXRetrofit.N;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.NiService;
import io.openim.android.ouicore.services.OneselfService;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.api.MomentsService;
import io.openim.android.ouimoments.bean.MomentsBean;
import io.openim.android.ouimoments.bean.MomentsContent;
import io.openim.android.ouimoments.bean.MomentsData;
import io.openim.android.ouimoments.ui.SelectDataActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.GroupInfo;

public class PushMomentsVM extends BaseViewModel {
    public static final String TAG = PushMomentsVM.class.getSimpleName();
    public MutableLiveData<List<Object>> selectMedia = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<PushMomentsParam> param = new MutableLiveData<>(new PushMomentsParam());
    //已选择的RuleDataList
    public List<SelectDataActivity.RuleData> selectedGroupRuleDataList;
    public List<SelectDataActivity.RuleData> selectedUserRuleDataList;

    //是否是发布照片
    public boolean isPhoto=true;
    public int addID = io.openim.android.ouicore.R.mipmap.ic_add3;


    public void init() {
        param.getValue().content = new MomentsContent();
        param.getValue().content.metas = new ArrayList<>();
        param.getValue().content.type = isPhoto ? 0 : 1;

        selectMedia.getValue().add(io.openim.android.ouicore.R.mipmap.ic_add3);
        selectMedia.setValue(selectMedia.getValue());
    }

    @NonNull
    public List<SelectDataActivity.RuleData> buildGroupRuleData(List<GroupInfo> groupInfos) {
        List<String> selectedIds = new ArrayList<>();
        if (null != selectedGroupRuleDataList) {
            for (SelectDataActivity.RuleData ruleData : selectedGroupRuleDataList) {
                selectedIds.add(ruleData.id);
            }
        }
        List<SelectDataActivity.RuleData> ruleDataList = new ArrayList<>();
        for (GroupInfo groupInfo : groupInfos) {
            SelectDataActivity.RuleData ruleData = new SelectDataActivity.RuleData();
            ruleData.id = groupInfo.getGroupID();
            if (selectedIds.contains(groupInfo.getGroupID())) ruleData.isSelect = true;
            ruleData.name = groupInfo.getGroupName();
            ruleData.icon = groupInfo.getFaceURL();
            ruleDataList.add(ruleData);
        }
        return ruleDataList;
    }

    public String getRuleDataNames(List<SelectDataActivity.RuleData> ruleDataList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (SelectDataActivity.RuleData ruleData : ruleDataList) {
            stringBuilder.append(ruleData.name);
            stringBuilder.append("、");
        }
        String res = stringBuilder.toString();
        return res.substring(0, res.length() - 1);
    }

    /**
     * 添加资源
     *
     * @param path
     */
    public void addRes(String path) {
        List<Object> res = selectMedia.getValue();
        int addIndex = res.indexOf(addID);
        if (addIndex != -1) res.remove(addIndex);

        if (isPhoto) {
            int size = res.size();
            selectMedia.getValue().add(path);
            if (size < 8) {
                res.add(addID);
            }
            if (res.size() > 9) {
                res.remove(0);
            }
        } else {
            if (res.size() == 0) {
                res.add(path);
            }
        }

        selectMedia.setValue(res);
    }

    /**
     * 移除资源
     *
     * @param position
     */
    public void removeRes(int position) {
        List<Object> res = selectMedia.getValue();
        res.remove(position);
        int addIndex = res.indexOf(addID);
        if (addIndex == -1) res.add(addID);
        selectMedia.setValue(res);
    }

    public void selectAuth(int index) {
        selectedUserRuleDataList = null;
        selectedGroupRuleDataList = null;
        param.getValue().permissionUserList = null;
        param.getValue().permissionGroupList = null;

        param.getValue().permission = index;
        param.setValue(param.getValue());
    }

    public void getRuleDataIDs(List<SelectDataActivity.RuleData> ruleDataList, boolean isGroup) {
        List<UserOrGroup> userOrGroups = new ArrayList<>();
        for (SelectDataActivity.RuleData ruleData : ruleDataList) {
            UserOrGroup userOrGroup = new UserOrGroup();
            if (isGroup) {
                userOrGroup.groupID = ruleData.id;
                userOrGroup.groupName = ruleData.name;
            } else {
                userOrGroup.userID = ruleData.id;
                userOrGroup.userName = ruleData.name;
            }
            userOrGroups.add(userOrGroup);
        }
        if (isGroup) param.getValue().permissionGroupList = userOrGroups;
        else param.getValue().permissionUserList = userOrGroups;
    }

    public List<SelectDataActivity.RuleData> buildUserRuleData(List<ExUserInfo> exUserInfo,
                                                               List<SelectDataActivity.RuleData> selected) {
        List<String> selectedIds = new ArrayList<>();
        if (null != selected) {
            for (SelectDataActivity.RuleData ruleData : selected) {
                selectedIds.add(ruleData.id);
            }
        }
        List<SelectDataActivity.RuleData> ruleDataList = new ArrayList<>();
        for (ExUserInfo userInfo : exUserInfo) {
            SelectDataActivity.RuleData ruleData = new SelectDataActivity.RuleData();
            ruleData.id = userInfo.userInfo.getUserID();
            if (selectedIds.contains(ruleData.id)) ruleData.isSelect = true;
            ruleData.name = userInfo.userInfo.getNickname();
            ruleData.icon = userInfo.userInfo.getFaceURL();
            ruleDataList.add(ruleData);
        }
        return ruleDataList;
    }

    public List<String> getMediaPaths() {
        List<String> paths = new ArrayList<>();
        for (Object o : selectMedia.getValue()) {
            if (o instanceof String) paths.add((String) o);
        }
        return paths;
    }

    public void pushMoments() {
        N.API(MomentsService.class)
                .pushMoments(Parameter.buildJsonBody(
                    JSONArray.toJSONString(param.getValue())))
                .compose(N.IOMain())
            .map(OneselfService.turn(Object.class))
            .subscribe(new NetObserver<Object>(TAG) {
            @Override
            public void onSuccess(Object o) {
                getIView().onSuccess(o);
            }

            @Override
            protected void onFailure(Throwable e) {
                getIView().toast(e.getMessage());
            }
        });
    }

    public static class PushMomentsParam {
        public MomentsContent content;
        //0/1/2/3, 公开/私密/部分可见/不给谁看
        public int permission;
        public List<UserOrGroup> permissionUserList;
        public List<UserOrGroup> permissionGroupList;
        public List<UserOrGroup> atUserList;
    }

    public static class UserOrGroup {
        public String userID;
        public String userName;
        public String groupID;
        public String groupName;
    }
}
