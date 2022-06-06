package io.openim.android.ouicontact.ui;


import android.os.Bundle;

import io.openim.android.ouicontact.databinding.ActivityGroupNoticeDetailBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.base.BaseActivity;


public class GroupNoticeDetailActivity extends BaseActivity<ContactVM, ActivityGroupNoticeDetailBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ContactVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupNoticeDetailBinding.inflate(getLayoutInflater()));
        sink();
        view.setContactVM(vm);

        initView();
    }

    private void initView() {
        view.avatar.load(vm.groupDetail.getValue().getUserFaceURL());
    }

    @Override
    public void onSuccess(String body) {
        finish();
    }
}
