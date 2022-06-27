package io.openim.android.ouigroup.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupBulletinBinding;
import io.openim.android.ouigroup.vm.GroupVM;

public class GroupBulletinActivity extends BaseActivity<GroupVM, ActivityGroupBulletinBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupBulletinBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        sink();

        click();
    }

    private void click() {
        view.edit.setOnClickListener(v -> {
            view.content.setEnabled(true);
            view.content.requestFocus();
            Common.pushKeyboard(this);
            v.setVisibility(View.GONE);
            view.finish.setVisibility(View.VISIBLE);
        });
        view.finish.setOnClickListener(v -> {
            view.content.setEnabled(false);
            Common.hideKeyboard(this, view.edit);
            v.setVisibility(View.GONE);
            view.edit.setVisibility(View.VISIBLE);

            vm.updataGroup(vm.groupId, null, null, vm.groupsInfo.getValue().getNotification(), null, null);
        });
    }
}
