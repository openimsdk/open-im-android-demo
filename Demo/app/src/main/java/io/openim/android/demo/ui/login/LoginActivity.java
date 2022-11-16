package io.openim.android.demo.ui.login;

import android.content.Intent;

import io.openim.android.demo.ui.ServerConfigActivity;
import io.openim.android.demo.ui.main.MainActivity;

import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import com.yanzhenjie.permission.AndPermission;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityLoginBinding;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.widget.WaitDialog;

public class LoginActivity extends BaseActivity<LoginVM, ActivityLoginBinding> implements LoginVM.ViewAction {

    public static final String FORM_LOGIN = "form_login";
    private WaitDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndPermission.with(this).overlay().start();
        super.onCreate(savedInstanceState);
        bindVM(LoginVM.class, true);
        bindViewDataBinding(ActivityLoginBinding.inflate(getLayoutInflater()));
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(null);

        waitDialog = new WaitDialog(this);
        view.loginContent.setLoginVM(vm);
        click();
        listener();
    }


    private void listener() {
        vm.isPhone.observe(this, v -> {
            if (v) {
                view.phoneTv.setTextColor(Color.parseColor("#1D6BED"));
                view.mailTv.setTextColor(Color.parseColor("#333333"));
                view.phoneVv.setVisibility(View.VISIBLE);
                view.mailVv.setVisibility(View.INVISIBLE);
            } else {
                view.mailTv.setTextColor(Color.parseColor("#1D6BED"));
                view.phoneTv.setTextColor(Color.parseColor("#333333"));
                view.mailVv.setVisibility(View.VISIBLE);
                view.phoneVv.setVisibility(View.INVISIBLE);
            }
            view.loginContent.edt1.setText("");
            view.loginContent.edt2.setText("");
            submitEnabled();
            view.loginContent.edt1.setHint(v ? getString(R.string.input_phone) : getString(R.string.input_mail));
            view.loginContent.registerTv.setText(v ? getString(R.string.phone_register) : getString(R.string.mail_register));
        });
        vm.account.observe(this, v -> submitEnabled());
        vm.pwd.observe(this, v -> submitEnabled());
    }

    private final GestureDetector gestureDetector =
        new GestureDetector(BaseApp.inst(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            startActivity(new Intent(LoginActivity.this, ServerConfigActivity.class));
            return super.onDoubleTap(e);
        }
    });

    private void click() {
        view.welcome.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        view.phoneTv.setOnClickListener(v -> {
            vm.isPhone.setValue(true);
        });
        view.mailTv.setOnClickListener(v -> {
            vm.isPhone.setValue(false);
        });
        view.loginContent.clear.setOnClickListener(v -> view.loginContent.edt1.setText(""));
        view.loginContent.eyes.setOnCheckedChangeListener((buttonView, isChecked) -> view.loginContent.edt2.setTransformationMethod(isChecked ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance()));
        view.protocol.setOnCheckedChangeListener((buttonView, isChecked) -> submitEnabled());
        view.loginContent.registerTv.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        view.submit.setOnClickListener(v -> {
            waitDialog.show();
            vm.login();
        });
    }

    private void submitEnabled() {
        view.submit.setEnabled(!vm.account.getValue().isEmpty() && !vm.pwd.getValue().isEmpty() && view.protocol.isChecked());
    }

    @Override
    public void jump() {
        startActivity(new Intent(this, MainActivity.class).putExtra(FORM_LOGIN,
            true));
        waitDialog.dismiss();
        finish();
    }

    @Override
    public void err(String msg) {
        waitDialog.dismiss();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void succ(Object o) {

    }

    @Override
    public void initDate() {

    }
}
