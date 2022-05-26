package io.openim.android.demo.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import java.io.IOException;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityRegisterBinding;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.SinkHelper;
import okhttp3.ResponseBody;

public class RegisterActivity extends BaseActivity<LoginVM, ActivityRegisterBinding> implements LoginVM.ViewAction, TextWatcher {


    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindVMByCache(LoginVM.class);
        bindViewDataBinding(ActivityRegisterBinding.inflate(getLayoutInflater()));
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        view.setLoginVM(vm);


        initView();
        listener();
    }


    private void listener() {
        view.edt1.addTextChangedListener(this);
        view.protocol.setOnCheckedChangeListener((buttonView, isChecked) -> submitEnabled());
        view.clear.setOnClickListener(v -> view.edt1.setText(""));

        view.submit.setOnClickListener(v -> {
            vm.getVerificationCode();
        });
    }

    private void initView() {
        view.tips.setText(vm.isPhone.getValue() ? getString(R.string.phone_register) : getString(R.string.mail_register));
        view.edt1.setHint(vm.isPhone.getValue() ? getString(R.string.input_phone) : getString(R.string.input_mail));
    }

    private void submitEnabled() {
        id = view.edt1.getText().toString();
        view.submit.setEnabled(!id.isEmpty() && view.protocol.isChecked());

        view.submit.setOnClickListener(v -> {
            vm.getVerificationCode();
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
        Toast.makeText(this, R.string.send_succ, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, VerificationCodeActivity.class));
    }

    @Override
    public void initDate() {
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        view.edt1.removeTextChangedListener(this);
    }

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
}
