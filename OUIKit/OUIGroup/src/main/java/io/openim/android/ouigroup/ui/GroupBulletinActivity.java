package io.openim.android.ouigroup.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouigroup.databinding.ActivityGroupBulletinBinding;
import io.openim.android.ouicore.vm.GroupVM;

@Route(path = Routes.Group.GROUP_BULLETIN)
public class GroupBulletinActivity extends BaseActivity<GroupVM, ActivityGroupBulletinBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupBulletinBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        sink();
        vm.isOwnerOrAdmin.observe(this, aBoolean -> view.edit.setVisibility(aBoolean ?
            View.VISIBLE : View.GONE));

        click();
    }

    private void click() {
        view.content.setKeyListener(null);
        view.content.addTextChangedListener(textWatcher);

        view.edit.setOnClickListener(v -> {
            view.content.setKeyListener(new TextKeyListener(TextKeyListener.Capitalize.NONE,false));
            Common.pushKeyboard(this);
            v.setVisibility(View.GONE);
            view.finish.setVisibility(View.VISIBLE);
        });
        view.finish.setOnClickListener(v -> {
            view.content.setKeyListener(null);
            Common.hideKeyboard(this, view.edit);
            v.setVisibility(View.GONE);
            view.edit.setVisibility(View.VISIBLE);

            vm.UPDATEGroup(vm.groupId, null, null, vm.groupsInfo.getValue().getNotification(),
                null, null);

        });
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            view.maxTips.setText(vm.groupsInfo.val().getNotification().length() + "/250");
        }
    };

}
