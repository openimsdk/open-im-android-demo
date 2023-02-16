package io.openim.android.ouimoments.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.databinding.ActivityWhoSeeBinding;
import io.openim.android.ouimoments.mvp.presenter.PushMomentsVM;

public class WhoSeeActivity extends BaseActivity<PushMomentsVM, ActivityWhoSeeBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(PushMomentsVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityWhoSeeBinding.inflate(getLayoutInflater()));
        sink();
        initView();
    }

    private void initView() {
        view.setPushMoments(vm);

    }
}
