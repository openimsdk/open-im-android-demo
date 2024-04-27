package io.openim.android.ouimoments.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimoments.databinding.ActivityWhoSeeBinding;
import io.openim.android.ouimoments.mvp.presenter.PushMomentsVM;
import io.openim.android.sdk.models.GroupInfo;

public class WhoSeeActivity extends BaseActivity<PushMomentsVM, ActivityWhoSeeBinding> {
    GroupVM groupVM = new GroupVM();
    private WaitDialog waitDialog;
    private boolean isGroup;
    private int lastPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(PushMomentsVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityWhoSeeBinding.inflate(getLayoutInflater()));
        initView();
        listener();
    }


    private ActivityResultLauncher<Intent> ruleDataLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;
            List<SelectDataActivity.RuleData> selectedRuleDataList =
                (List<SelectDataActivity.RuleData>) result.getData().getSerializableExtra(Constants.K_RESULT);

            int permission = vm.param.val().permission;
            bindData(isGroup, selectedRuleDataList, permission);

            vm.getRuleDataIDs(selectedRuleDataList, isGroup);
        });

    private void ruleDataLauncher(List<MultipleChoice> choices) {
        List<SelectDataActivity.RuleData> selectedRuleDataList = new ArrayList<>();
        for (MultipleChoice choice : choices) {
            SelectDataActivity.RuleData ruleData = new SelectDataActivity.RuleData();
            ruleData.id = choice.key;
            ruleData.name = choice.name;
            ruleData.icon = choice.icon;
            selectedRuleDataList.add(ruleData);
        }
        int permission = vm.param.val().permission;
        bindData(isGroup, selectedRuleDataList, permission);

        vm.getRuleDataIDs(selectedRuleDataList, isGroup);
    }

    private void bindData(boolean isGroup, List<SelectDataActivity.RuleData> selectedRuleDataList
        , int permission) {
        if (isGroup) {
            vm.selectedGroupRuleDataList = selectedRuleDataList;
            if (permission == 2) view.seeGroup.setText(vm.getRuleDataNames(selectedRuleDataList));
            if (permission == 3)
                view.notSeeGroup.setText(vm.getRuleDataNames(selectedRuleDataList));
        } else {
            vm.selectedUserRuleDataList = selectedRuleDataList;
            if (permission == 2) view.seeUser.setText(vm.getRuleDataNames(selectedRuleDataList));
            if (permission == 3) view.notSeeUser.setText(vm.getRuleDataNames(selectedRuleDataList));
        }
    }


    private OnDedrepClickListener selectGroupClick = new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            isGroup = true;
            if (groupVM.groups.getValue().isEmpty()) toGetData(true);
            else jumpSelectGroup(groupVM.groups.getValue());
        }
    };
    private OnDedrepClickListener selectUserClick = new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            isGroup = false;
            if (groupVM.exUserInfo.getValue().isEmpty()) toGetData(false);
            else jumpSelectUser(groupVM.exUserInfo.getValue());
        }
    };

    private void listener() {
        view.finish.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        vm.param.observe(this, pushMomentsParam -> {
            if (pushMomentsParam.permission != lastPermission) {
                view.seeGroup.setText("");
                view.notSeeGroup.setText("");
                view.seeUser.setText("");
                view.notSeeUser.setText("");
            }
            lastPermission = pushMomentsParam.permission;
        });
        view.selectGroup.setOnClickListener(selectGroupClick);
        view.selectUser.setOnClickListener(selectUserClick);
        view.selectGroup2.setOnClickListener(selectGroupClick);
        view.selectUser2.setOnClickListener(selectUserClick);

        groupVM.groups.observe(this, groupInfos -> {
            waitDialog.dismiss();
            jumpSelectGroup(groupInfos);
        });
        groupVM.exUserInfo.observe(this, exUserInfos -> {
            waitDialog.dismiss();
            jumpSelectUser(exUserInfos);
        });
    }

    private void jumpSelectUser(List<ExUserInfo> exUserInfo) {
        if (exUserInfo.isEmpty()) return;

        SelectTargetVM targetVM = Easy.installVM(SelectTargetVM.class)
            .setIntention(SelectTargetVM.Intention.multipleSelect);
        if ( null!=vm.selectedUserRuleDataList){
            for (SelectDataActivity.RuleData ruleData : vm.selectedUserRuleDataList) {
                MultipleChoice choice = new MultipleChoice(ruleData.id);
                choice.isSelect = true;
                choice.name = ruleData.name;
                choice.icon = ruleData.icon;
                if (!targetVM.contains(choice))
                    targetVM.metaData.val().add(choice);
            }
        }
        targetVM.metaData.update();
        targetVM.setOnFinishListener(() -> {
            Common.finishRoute(Routes.Group.SELECT_TARGET, Routes.Contact.ALL_FRIEND);
            ruleDataLauncher(targetVM.metaData.val());
        });
        ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
    }

    private void jumpSelectGroup(List<GroupInfo> groupInfos) {
        if (groupInfos.isEmpty()) return;
        List<SelectDataActivity.RuleData> ruleDataList = vm.buildGroupRuleData(groupInfos);
        ruleDataLauncher.launch(new Intent(this, SelectDataActivity.class)
            .putExtra(Constants.K_NAME, getString(io.openim.android.ouicore.R.string.select_group))
            .putExtra(Constants.K_RESULT, (Serializable) ruleDataList)
            .putExtra(Constants.K_FROM, isGroup)
            .putExtra(Constants.K_SIZE, 5));

    }

    private void toGetData(boolean isGroup) {
        waitDialog.show();
        if (isGroup) groupVM.getAllGroup();
        else groupVM.getAllFriend();
    }


    private void initView() {
        waitDialog = new WaitDialog(this);
        view.setPushMoments(vm);

        lastPermission = vm.param.getValue().permission;
        if (null != vm.selectedGroupRuleDataList)
            bindData(true, vm.selectedGroupRuleDataList, vm.param.getValue().permission);
        if (null != vm.selectedUserRuleDataList)
            bindData(false, vm.selectedUserRuleDataList, vm.param.getValue().permission);
    }
}
