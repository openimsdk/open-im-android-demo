package io.openim.android.demo.ui.login;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.TextView;

import io.openim.android.demo.R;

import io.openim.android.demo.databinding.ActivitySupplementInfoBinding;
import io.openim.android.demo.ui.main.MainActivity;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;


public class SupplementInfoActivity extends BaseActivity<LoginVM, ActivitySupplementInfoBinding> implements LoginVM.ViewAction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(LoginVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySupplementInfoBinding.inflate(getLayoutInflater()));
        sink();
        view.setLoginVM(vm);

        vm.pwd.setValue("");
        initView();
    }

    private void initView() {
        vm.nickName.observe(this, this::setEnabled);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setEnabled(vm.nickName.val());
            }
        };
        view.password.addTextChangedListener(textWatcher);
        view.surePassword.addTextChangedListener(textWatcher);
        view.submit.setOnClickListener(v -> {
            vm.pwd.setValue(view.password.getText().toString());
            vm.register();
        });
    }

    private void setEnabled(String nickname) {
        String surePassword=view.surePassword.getText().toString();
        String password = view.password.getText().toString();
        view.submit.setEnabled(!TextUtils.isEmpty(nickname) &&
            !TextUtils.isEmpty(surePassword) &&
            !TextUtils.isEmpty(password) &&
            password.length() >= 6 &&
            TextUtils.equals(password, surePassword));
    }

    @Override
    public void jump() {
        removeCacheVM();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void err(String msg) {

    }

    @Override
    public void succ(Object o) {

    }

    @Override
    public void initDate() {

    }
}
