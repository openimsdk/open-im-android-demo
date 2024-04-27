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
        vm.pwd.observe(this,v->{
            setEnabled( vm.nickName.val());
        });
      view.surePassword.addTextChangedListener(new TextWatcher() {
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
      });
    }

    private void setEnabled(String v) {
        String surePassword=view.surePassword.getText().toString();
        view.submit.setEnabled(!TextUtils.isEmpty(v)&&vm.pwd.val().length()>=6&&vm.pwd.val()
            .equals(surePassword));
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
