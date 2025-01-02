package io.openim.android.ouigroup.ui.v3;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.vm.GroupMemberVM;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouigroup.databinding.ActivityGroupManageBinding;
import io.openim.android.ouigroup.ui.SuperGroupMemberActivity;

public class GroupManageActivity extends BaseActivity<GroupVM, ActivityGroupManageBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = Easy.find(GroupVM.class);
        bindViewDataBinding(ActivityGroupManageBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        click();
    }

    private void click() {
        view.transferPermissions.setOnClickListener(v -> {
            gotoMemberList();
        });
    }

    private void gotoMemberList() {
        GroupMemberVM memberVM = Easy.installVM(GroupMemberVM.class);
        memberVM.groupId = vm.groupId;
        memberVM.setIntention(GroupMemberVM.Intention.SELECT_SINGLE);
        memberVM.setOnFinishListener(activity -> {
            MultipleChoice choice = memberVM.choiceList.val().get(0);
            CommonDialog commonDialog = new CommonDialog(activity);
            commonDialog.getMainView().tips.setText(String.format(BaseApp.inst().getString(io.openim.android.ouicore.R.string.transfer_permission), choice.name));
            commonDialog.getMainView().cancel.setOnClickListener(v2 -> {
                commonDialog.dismiss();
            });
            commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                commonDialog.dismiss();
                vm.transferGroupOwner(choice.key, data1 -> {
                    toast(getString(io.openim.android.ouicore.R.string.transfer_succ));
                    activity.finish();
                    finish();
                });
            });
            commonDialog.show();
        });
        startActivity(new Intent(this, SuperGroupMemberActivity.class));
    }
}
