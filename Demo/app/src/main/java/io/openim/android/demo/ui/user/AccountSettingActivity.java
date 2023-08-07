package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityAccountSettingBinding;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ExtendUserInfo;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.SlideButton;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

public class AccountSettingActivity extends BaseActivity<PersonalVM, ActivityAccountSettingBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PersonalVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAccountSettingBinding.inflate(getLayoutInflater()));
        sink();
        vm.getSelfUserInfo();
        listener();
    }

    private void listener() {
        vm.userInfo.observe(this, extendUserInfo -> {
            if (null == extendUserInfo) return;
            view.slideButton.setCheckedWithAnimation(extendUserInfo.getGlobalRecvMsgOpt() == 2);
        });
        view.slideButton.setOnSlideButtonClickListener(isChecked -> vm.setGlobalRecvMessageOpt(isChecked));
        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_all_record);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                OpenIMClient.getInstance().messageManager.deleteAllMsgFromLocalAndSvr(null);
                toast(getString(io.openim.android.ouicore.R.string.cleared));
            });
            commonDialog.show();
        });
        view.blackList
            .setOnClickListener(view1 -> startActivity(new Intent(this, BlackListActivity.class)));
    }
}
