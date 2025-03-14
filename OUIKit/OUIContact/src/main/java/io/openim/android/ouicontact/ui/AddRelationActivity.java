package io.openim.android.ouicontact.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;

import java.util.List;

import io.openim.android.ouicontact.databinding.ActivityAddRelationBinding;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.sdk.models.FriendInfo;

public class AddRelationActivity extends BasicActivity<ActivityAddRelationBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding(ActivityAddRelationBinding.inflate(getLayoutInflater()));
        click();
    }

    private void click() {
        view.addFriend.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(Routes.Main.ADD_CONVERS).navigation();
            }
        });
        view.createGroup.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                SelectTargetVM targetVM = Easy.installVM(SelectTargetVM.class)
                    .setIntention(SelectTargetVM.Intention.isCreateGroup);
                targetVM.setOnFinishListener(() -> {
                    GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
                    if (null == groupVM)
                        groupVM = new GroupVM();
                    groupVM.selectedFriendInfo.getValue().clear();
                    List<MultipleChoice> multipleChoices = targetVM.metaData.getValue();
                    for (int i = 0; i < multipleChoices.size(); i++) {
                        MultipleChoice us = multipleChoices.get(i);
                        FriendInfo friendInfo = new FriendInfo();
                        friendInfo.setUserID(us.key);
                        friendInfo.setNickname(us.name);
                        friendInfo.setFaceURL(us.icon);
                        groupVM.selectedFriendInfo.getValue().add(friendInfo);
                    }
                    BaseApp.inst().putVM(groupVM);
                    ARouter.getInstance().build(Routes.Group.CREATE_GROUP2).navigation();
                });
                ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
            }
        });
        view.addGroup.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(Routes.Main.ADD_CONVERS).withBoolean(Constants.K_RESULT,
                    false).navigation();
            }
        });
    }
}
