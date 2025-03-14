package io.openim.android.ouiapplet;

import android.os.Bundle;

import androidx.lifecycle.Observer;

import io.openim.android.ouiapplet.databinding.ActivityAppletBinding;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.vm.UserLogic;

public class AppletActivity extends BasicActivity<ActivityAppletBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding(ActivityAppletBinding.inflate(getLayoutInflater()));
        Easy.find(UserLogic.class).discoverPageURL.observe(this,
            s -> view.webView.loadUrl(s));
    }
}
