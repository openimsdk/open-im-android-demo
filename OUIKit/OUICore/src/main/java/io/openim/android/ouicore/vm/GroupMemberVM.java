package io.openim.android.ouicore.vm;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupRole;
import io.openim.android.sdk.models.GroupMembersInfo;

public class GroupMemberVM extends BaseVM {

    private Intention intention;

    public enum Intention {
        /**
         * 查看群成员
         * 不显示勾选按钮、显示右上角菜单
         */
        CHECK,
        /**
         * @人员/移除群成员 显示勾选按钮、
         * 根据条件显示@所有人、隐藏右上角菜单
         */
        AT,
        /**
         * 单选
         * 不显示勾选按钮、隐藏右上角菜单
         */
        SELECT_SINGLE,
        /**
         * 多选
         * 显示勾选按钮、隐藏右上角菜单
         */
        SELECT_MULTIPLE
    }

    public void setIntention(Intention intention) {
        this.intention = intention;
    }

    public boolean isCheck() {
        return intention == Intention.CHECK;
    }

    public boolean isAt() {
        return intention == Intention.AT;
    }

    public boolean isSingle() {
        return intention == Intention.SELECT_SINGLE;
    }

    public boolean isMultiple() {
        return intention == Intention.SELECT_MULTIPLE;
    }


    public void onFinish(Activity context) {
        if (null != onFinishListener) onFinishListener.onFinish(context);
    }

    private OnFinishListener onFinishListener;

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface OnFinishListener {
        void onFinish(Activity context);
    }


    public int maxNum = 9;
    public boolean isOwnerOrAdmin = false;
    //搜索出的用户是否单选
    public boolean isSearchSingle = false;
    //结果中是否移除管理员或群主
    public boolean isRemoveOwnerAndAdmin = false;
    public String groupId;


    public State<List<MultipleChoice>> choiceList = new State<>(new ArrayList<>());
    public State<List<GroupMembersInfo>> superGroupMembers = new State<>(new ArrayList<>());

    public int page = 0, pageSize = 100, firstPageSize = 500;

    public void addChoice(MultipleChoice choice) {
        if (!choiceList.val().contains(choice)) {
            choiceList.val().add(choice);
        }
    }

    public void removeChoice(String key) {
        choiceList.val().remove(new MultipleChoice(key));
    }

    public void getSuperGroupMemberList() {
        final int finalPage = page;
        int start = finalPage > 0 ? (firstPageSize + finalPage * pageSize) : 0;
        OpenIMClient.getInstance().groupManager
            .getGroupMemberList(new IMUtil.IMCallBack<List<GroupMembersInfo>>() {
                @Override
                public void onSuccess(List<GroupMembersInfo> data) {
                    if (data.isEmpty()) return;
                    if (isRemoveOwnerAndAdmin) {
                        removeOwnerAndAdmin(data);
                    }

                    if (finalPage == 0)
                        superGroupMembers.setValue(data);
                    else
                        superGroupMembers.val().addAll(data);
                    superGroupMembers.update();
                }
            }, groupId, 0, start, finalPage == 0 ? firstPageSize : pageSize);
    }

    public   void removeOwnerAndAdmin(List<GroupMembersInfo> data) {
        Iterator<GroupMembersInfo> iterator = data.iterator();
        while (iterator.hasNext()) {
            GroupMembersInfo info = iterator.next();
            if (info.getRoleLevel() != GroupRole.MEMBER)
                iterator.remove();
        }
    }

    public boolean removeSelf(List<GroupMembersInfo> v) {
        Iterator<GroupMembersInfo> iterator = v.iterator();
        while (iterator.hasNext()) {
            GroupMembersInfo memberInfo = iterator.next();
            if (memberInfo.getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}
