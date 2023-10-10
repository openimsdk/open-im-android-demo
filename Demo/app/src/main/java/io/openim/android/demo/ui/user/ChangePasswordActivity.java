package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityChangePasswordBinding;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.RegexValid;

public class ChangePasswordActivity extends BaseActivity<LoginVM, ActivityChangePasswordBinding> implements LoginVM.ViewAction {

    private LoginVM loginVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginVM = Easy.installVM(this, LoginVM.class);
        bindViewDataBinding(ActivityChangePasswordBinding.inflate(getLayoutInflater()));

        loginVM.setIView(this);
        loginVM.setContext(this);

    view.submit.setOnClickListener(new OnDedrepClickListener() {
        @Override
        public void click(View v) {
           String oldPassword= view.oldPassword.getText().toString();
           String newPassword= view.newPassword.getText().toString();
           String surePassword= view.surePassword.getText().toString();
           if (TextUtils.isEmpty(oldPassword)||TextUtils.isEmpty(surePassword)){
               toast(getString(io.openim.android.ouicore.R.string.please_input_complete));
               return;
           }
           if (!RegexValid.isValidPassword(newPassword)){
                toast(getString(io.openim.android.ouicore.R.string.password_valid_tips));
                return;
           }
           if (!newPassword.equals(surePassword)){
               toast(getString(io.openim.android.ouicore.R.string.password_valid_tips2));
               return;
           }
           loginVM.changePassword(oldPassword,newPassword);
        }
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
        toast(getString(io.openim.android.ouicore.R.string.edit_succ));
        finish();
    }



    @Override
    public void initDate() {

    }
}
