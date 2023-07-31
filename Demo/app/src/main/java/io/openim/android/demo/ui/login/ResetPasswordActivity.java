package io.openim.android.demo.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityResetPasswordBinding;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;

public class ResetPasswordActivity extends BaseActivity<LoginVM, ActivityResetPasswordBinding> implements LoginVM.ViewAction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(LoginVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityResetPasswordBinding.inflate(getLayoutInflater()));
        sink();
        listener();
    }

    private void listener() {
        view.edt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.eyes.setOnCheckedChangeListener((buttonView, isChecked) ->
            view.edt2.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()));
    }

    private void submitEnabled() {
        String password = view.edt2.getText().toString();
        view.submit.setEnabled(!password.isEmpty());

        view.submit.setOnClickListener(v -> {
            if (password.length() < 6 || password.length() > 20) {
                toast(getString(io.openim.android.ouicore.R.string.login_tips3));
                return;
            }
            vm.resetPassword(password);
        });
    }

    // 禁止返回
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void jump() {

    }

    @Override
    public void err(String msg) {
        toast(msg);
    }

    @Override
    public void succ(Object o) {
        toast(getString(io.openim.android.ouicore.R.string.set_succ));
        finish();
        startActivity(new Intent(this,LoginActivity.class));
    }

    @Override
    public void initDate() {

    }
}
