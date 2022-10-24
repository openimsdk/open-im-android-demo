package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityAboutUsBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.OpenIMClient;

public class AboutUsActivity extends BaseActivity<BaseViewModel, ActivityAboutUsBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAboutUsBinding.inflate(getLayoutInflater()));
        sink();

        view.version.setText(Common.getAppVersionName(this));
        view.update.setOnClickListener(v -> {

        });
    }
}
