package io.openim.android.demo.ui.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityPersonInfoBinding;
import io.openim.android.demo.databinding.ActivityPersonalInfoBinding;
import io.openim.android.demo.ui.main.EditTextActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.SlideButton;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class PersonDataActivity extends BaseActivity<PersonalVM, ActivityPersonInfoBinding> {

    private ChatVM chatVM;
    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PersonalVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPersonInfoBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
        chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
        vm.getUserInfo(chatVM.otherSideID);
    }

    private void init() {
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        friendVM.setContext(this);
        friendVM.setIView(this);
        friendVM.getBlacklist();
    }


    private void listener() {
        view.remark.setOnClickListener(view -> {
            if (null == vm.userInfo.getValue()) return;
            resultLauncher.launch(new Intent(this, EditTextActivity.class)
                .putExtra(EditTextActivity.TITLE, getString(io.openim.android.ouicore.R.string.remark))
                .putExtra(EditTextActivity.INIT_TXT, vm.userInfo.getValue().getRemark()));
        });
        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID()
                    .equals(chatVM.otherSideID)) {
                    isCon = true;
                    break;
                }
            }
            view.slideButton.setChecked(isCon);
        });
        view.joinBlackList.setOnClickListener(v -> {
            if (view.slideButton.isChecked())
                friendVM.removeBlacklist(chatVM.otherSideID);
            else {
                addBlackList();
            }
        });
        view.slideButton.setOnSlideButtonClickListener(isChecked -> {
            if (isChecked)
                addBlackList();
            else
                friendVM.removeBlacklist(chatVM.otherSideID);
        });
    }

    private void addBlackList() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setCanceledOnTouchOutside(false);
        commonDialog.setCancelable(false);
        commonDialog.getMainView().tips.setText("确认对" + vm.userInfo.getValue().getNickname() + "拉黑吗？");
        commonDialog.getMainView().cancel.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.blackListUser.setValue(friendVM.blackListUser.getValue());
        });
        commonDialog.getMainView().confirm.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.addBlacklist(chatVM.otherSideID);
        });
        commonDialog.show();
    }

    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        String resultStr = result.getData().getStringExtra(Constant.K_RESULT);

        waitDialog.show();
        OpenIMClient.getInstance().friendshipManager.setFriendRemark(new OnBase<String>() {
            @Override
            public void onError(int code, String error) {
                waitDialog.dismiss();
                toast(error + code);
            }

            @Override
            public void onSuccess(String data) {
                waitDialog.dismiss();
                vm.userInfo.getValue().setRemark(resultStr);
            }
        }, chatVM.otherSideID, resultStr);
    });
}
