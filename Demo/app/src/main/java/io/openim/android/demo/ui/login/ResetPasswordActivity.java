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
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.RegexValid;

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
        TextWatcher textWatcher=   new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                submitEnabled();
            }
        };
        view.edt2.addTextChangedListener(textWatcher);
        view.edt3.addTextChangedListener(textWatcher);

        view.eyes.setOnCheckedChangeListener((buttonView, isChecked) ->
            view.edt2.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()));
        view.eyes2.setOnCheckedChangeListener((buttonView, isChecked) ->
            view.edt3.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()));

    }

    private void submitEnabled() {
        String password = view.edt2.getText().toString();
        String surePassword = view.edt3.getText().toString();
        view.submit.setEnabled(!password.isEmpty() && password.equals(surePassword));

        view.submit.setOnClickListener(v -> {
            if (!RegexValid.isValidPassword(password)) {
                toast(BaseApp.inst().getString(
                    io.openim.android.ouicore.R.string.password_valid_tips));
                return;
            }
            vm.resetPassword(password);
        });
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
