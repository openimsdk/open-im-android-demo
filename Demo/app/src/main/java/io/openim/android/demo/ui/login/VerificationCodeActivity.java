package io.openim.android.demo.ui.login;

import static io.openim.android.ouicore.utils.Constant.VM;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import io.openim.android.demo.R;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.Routes;

public class VerificationCodeActivity extends BaseActivity<LoginVM> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        vm= (LoginVM) getIntent().getSerializableExtra(VM);

    }

    public void back(View view) {
        finish();
    }
}
