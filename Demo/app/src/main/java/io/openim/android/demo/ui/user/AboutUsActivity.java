package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
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
        PackageInfo packageInfo=Common.getAppPackageInfo(this);
        if (null!=packageInfo){
            view.version.setText(packageInfo.versionName);
        }
        view.update.setOnClickListener(v -> {

        });
    }
}
