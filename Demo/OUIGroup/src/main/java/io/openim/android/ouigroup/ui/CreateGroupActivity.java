package io.openim.android.ouigroup.ui;

import android.os.Bundle;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouigroup.databinding.ActivityCreateGroupBinding;
import io.openim.android.ouigroup.vm.GroupVM;

public class CreateGroupActivity extends BaseActivity<GroupVM,ActivityCreateGroupBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityCreateGroupBinding.inflate(getLayoutInflater()));
    }
}
