package io.openim.android.demo.ui.search;

import static io.openim.android.ouicontact.utils.Constant.K_USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivitySendVerifyBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.SinkHelper;

public class SendVerifyActivity extends BaseActivity<SearchVM> {

    private ActivitySendVerifyBinding view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = ActivitySendVerifyBinding.inflate(getLayoutInflater());
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        setContentView(view.getRoot());

        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);

        vm.searchContent = getIntent().getStringExtra(K_USER_ID);
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