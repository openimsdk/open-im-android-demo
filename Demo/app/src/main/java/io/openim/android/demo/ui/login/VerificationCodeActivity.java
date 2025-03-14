package io.openim.android.demo.ui.login;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityVerificationCodeBinding;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CodeEditText;

public class VerificationCodeActivity extends BaseActivity<LoginVM,
    ActivityVerificationCodeBinding> implements LoginVM.ViewAction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(LoginVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityVerificationCodeBinding.inflate(getLayoutInflater()));
        sink();
        view.setLoginVM(vm);
        vm.countdown();

        initView();
    }

    private void initView() {
        vm.countdown.observe(this, v -> {
            view.resend.setTextColor(v == 0 ? Color.parseColor("#ff1d6bed") : Color.parseColor(
                "#ff333333"));
        });
        view.resend.setOnClickListener(v -> {
            if (vm.countdown.getValue() != 0) return;
            vm.countdown.setValue(vm.MAX_COUNTDOWN);
            vm.countdown();
            vm.getVerificationCode(vm.isFindPassword ? 2 : 1);
        });

        view.codeEditText.setOnTextFinishListener((text, length) -> {
            vm.checkVerificationCode(text.toString(), vm.isFindPassword ? 2 : 1);

            view.submit.setEnabled(length==6);
        });

        view.submit.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.checkVerificationCode( Objects.requireNonNull(view.codeEditText.getText()).toString(),
                    vm.isFindPassword ? 2 : 1);
            }
        });

    }

    public void back(View view) {
        finish();
    }

    @Override
    public void jump() {

    }

    @Override
    public void err(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void succ(Object o) {
        if (o.equals("checkVerificationCode")) {
            if (vm.isFindPassword) {
                startActivity(new Intent(this,
                    ResetPasswordActivity.class));
            } else {
                startActivity(new Intent(this, SupplementInfoActivity.class));
            }
        }
    }

    @Override
    public void initDate() {

    }
}
