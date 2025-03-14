package io.openim.android.demo.ui.user;

import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

import io.openim.android.demo.databinding.ActivityAccountSettingBinding;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.AllowType;

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
        view.languageSetting.setOnClickListener(v -> {
            startActivity(new Intent(this,LanguageSettingActivity.class));
        });
        view.blackList
            .setOnClickListener(view1 -> startActivity(new Intent(this, BlackListActivity.class)));
    }
}
