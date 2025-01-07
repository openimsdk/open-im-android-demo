package io.openim.android.ouicontact.ui;


import android.content.Intent;
import android.os.Bundle;

import io.openim.android.ouicontact.databinding.ActivityGroupNoticeDetailBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;


public class GroupNoticeDetailActivity extends BaseActivity<ContactVM, ActivityGroupNoticeDetailBinding> {
    private int index = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vm= Easy.find(ContactVM.class);
        vm.setContext(this);
        vm.setIView(this);
        index = getIntent().getIntExtra("index", -1);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupNoticeDetailBinding.inflate(getLayoutInflater()));
        sink();
        view.setContactVM(vm);

        initView();
    }

    private void initView() {
        if (null != vm.groupDetail.getValue()) {
            view.avatar.load(vm.groupDetail.getValue().getUserFaceURL());
        }
    }

    @Override
    public void onSuccess(Object body) {
        try {
            Boolean state = (Boolean) body;
            setResult(RESULT_OK, new Intent()
                .putExtra(GroupNoticeListActivity.CALLBACK_STATE, state != null ? state : false)
                .putExtra("index", index));
        } catch (Exception e) {}
        finish();
    }
}
