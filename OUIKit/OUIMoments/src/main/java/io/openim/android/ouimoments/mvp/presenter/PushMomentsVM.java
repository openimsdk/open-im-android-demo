package io.openim.android.ouimoments.mvp.presenter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.bean.MomentsBean;
import io.openim.android.ouimoments.bean.MomentsContent;
import io.openim.android.sdk.OpenIMClient;

public class PushMomentsVM extends BaseViewModel {
    public MutableLiveData<List<Object>> selectMedia = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<PushMomentsParam> param = new MutableLiveData<>(new PushMomentsParam());


    //是否是发布照片
    boolean isPhoto = true;
    public int addID = io.openim.android.ouicore.R.mipmap.ic_add3;

    @Override
    protected void viewCreate() {

    }
    public void init(){
        selectMedia.getValue().add(io.openim.android.ouicore.R.mipmap.ic_add3);
        selectMedia.setValue(selectMedia.getValue());
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
        param.getValue().permission = index;
        param.setValue(param.getValue());
    }

    public  static class PushMomentsParam {
        public MomentsContent content;
        //0/1/2/3, 公开/私密/部分可见/不给谁看
        public int permission;
        public List<UserOrGroup> permissionUserList;
        public List<UserOrGroup> permissionGroupList;
    }

    public  static class UserOrGroup {
        public String userID;
        public String userName;
        public String groupID;
        public String groupName;
    }
}
