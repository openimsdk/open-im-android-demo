package io.openim.android.demo.ui.search;


import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.demo.databinding.ActivitySendVerifyBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;

@Route(path = Routes.Main.SEND_VERIFY)
public class SendVerifyActivity extends BaseActivity<SearchVM, ActivitySendVerifyBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySendVerifyBinding.inflate(getLayoutInflater()));
        view.setSearchVM(vm);

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());

        vm.searchContent = getIntent().getStringExtra(Constant.K_ID);
        vm.isPerson = getIntent().getBooleanExtra(Constant.K_IS_PERSON, true);
        listener();
        click();
    }

    private void click() {
        view.send.setOnClickListener(v -> {
            vm.addFriend();
        });
    }

    private void listener() {
        vm.hail.observe(this, v -> {
            if (v.equals("-1"))
                finish();
        });
    }
}
