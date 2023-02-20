package io.openim.android.ouimoments.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouimoments.R;
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
        sink();
        initView();
        listener();
    }


    private ActivityResultLauncher<Intent> ruleDataLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        List<SelectDataActivity.RuleData> selectedRuleDataList =
            (List<SelectDataActivity.RuleData>) result.getData().getSerializableExtra(Constant.K_RESULT);

        int permission = vm.param.getValue().permission;
        if (isGroup) {
            vm.selectedGroupRuleDataList = selectedRuleDataList;
            if (permission == 2) view.seeGroup.setText(vm.getRuleDataNames(selectedRuleDataList));
            if (permission == 3) view.notSeeGroup.setText(vm.getRuleDataNames(selectedRuleDataList));
        } else {
            vm.selectedUserRuleDataList = selectedRuleDataList;
            if (permission == 2) view.seeUser.setText(vm.getRuleDataNames(selectedRuleDataList));
            if (permission == 3) view.notSeeUser.setText(vm.getRuleDataNames(selectedRuleDataList));
        }

        vm.getRuleDataIDs(selectedRuleDataList, isGroup);
    });



    private OnDedrepClickListener selectGroupClick= new OnDedrepClickListener() {
        @Override
        public void click(View v) {
            isGroup = true;
            if (groupVM.groups.getValue().isEmpty()) toGetData(true);
            else jumpSelectGroup(groupVM.groups.getValue());
        }
    };
    private OnDedrepClickListener selectUserClick=new OnDedrepClickListener() {
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
        List<SelectDataActivity.RuleData> ruleDataList = vm.buildUserRuleData(exUserInfo);
        ruleDataLauncher.launch(new Intent(this, SelectDataActivity.class).putExtra(Constant.K_NAME, getString(io.openim.android.ouicore.R.string.select_user)).putExtra(Constant.K_RESULT, (Serializable) ruleDataList).putExtra(Constant.K_FROM, isGroup).putExtra(Constant.K_SIZE, 20));
    }

    private void jumpSelectGroup(List<GroupInfo> groupInfos) {
        if (groupInfos.isEmpty()) return;
        List<SelectDataActivity.RuleData> ruleDataList = vm.buildGroupRuleData(groupInfos);
        ruleDataLauncher.launch(new Intent(this, SelectDataActivity.class).putExtra(Constant.K_NAME, getString(io.openim.android.ouicore.R.string.select_group)).putExtra(Constant.K_RESULT, (Serializable) ruleDataList).putExtra(Constant.K_FROM, isGroup).putExtra(Constant.K_SIZE, 5));
    }

    private void toGetData(boolean isGroup) {
        waitDialog.show();
        if (isGroup) groupVM.getAllGroup();
        else groupVM.getAllFriend();
    }


    private void initView() {
        waitDialog = new WaitDialog(this);
        view.setPushMoments(vm);
    }
}
