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
import io.openim.android.ouigroup.ui.MemberPermissionActivity;
import io.openim.android.ouigroup.ui.SuperGroupMemberActivity;
import io.openim.android.sdk.enums.GroupStatus;
import io.openim.android.sdk.enums.GroupVerification;

public class GroupManageActivity extends BaseActivity<GroupVM, ActivityGroupManageBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = Easy.find(GroupVM.class);
        bindViewDataBinding(ActivityGroupManageBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        click();
        listener();
    }

    private void listener() {
        vm.groupsInfo.observe(this, groupInfo -> {
            view.totalSilence.setCheckedWithAnimation(groupInfo.getStatus() == GroupStatus.GROUP_MUTED);
            view.describe.setText(getJoinGroupOption(groupInfo.getNeedVerification()));
        });
    }

    String getJoinGroupOption(int value) {
        if (value == GroupVerification.ALL_NEED_VERIFICATION) {
            return getString(io.openim.android.ouicore.R.string.needVerification);
        } else if (value == GroupVerification.DIRECTLY) {
            return getString(io.openim.android.ouicore.R.string.allowAnyoneJoinGroup);
        }
        return getString(io.openim.android.ouicore.R.string.inviteNotVerification);
    }

    private void click() {
        view.memberPermissions.setOnClickListener(v -> {
            startActivity(new Intent(this, MemberPermissionActivity.class));
        });
        view.joinValidation.setOnClickListener(v -> {
            BottomPopDialog dialog = new BottomPopDialog(this);
            dialog.show();
            dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
            dialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.allowAnyoneJoinGroup);
            dialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.inviteNotVerification);
            dialog.getMainView().menu4.setVisibility(View.VISIBLE);
            dialog.getMainView().menu4.setText(io.openim.android.ouicore.R.string.needVerification);

            dialog.getMainView().menu1.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(GroupVerification.DIRECTLY, data -> {
                    vm.groupsInfo.getValue().setNeedVerification(GroupVerification.DIRECTLY);
                    vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                });
            });
            dialog.getMainView().menu2.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(GroupVerification.APPLY_NEED_VERIFICATION_INVITE_DIRECTLY
                    , data -> {
                        vm.groupsInfo.getValue().setNeedVerification(GroupVerification.APPLY_NEED_VERIFICATION_INVITE_DIRECTLY);
                        vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                    });
            });
            dialog.getMainView().menu4.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(GroupVerification.ALL_NEED_VERIFICATION, data -> {
                    vm.groupsInfo.getValue().setNeedVerification(GroupVerification.ALL_NEED_VERIFICATION);
                    vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                });
            });
        });
        view.totalSilence.setOnSlideButtonClickListener(isChecked -> {
            vm.changeGroupMute(isChecked, data -> {
                view.totalSilence.setCheckedWithAnimation(isChecked);
            });
        });

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
