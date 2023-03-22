package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMoreDataBinding;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.TimeUtil;

public class MoreDataActivity extends BaseActivity<PersonalVM, ActivityMoreDataBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(PersonalVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMoreDataBinding.inflate(getLayoutInflater()));
        init();
    }

    void init() {
        view.avatar.load(vm.exUserInfo.getValue().userInfo.getFaceURL());
        view.nickName.setText(vm.exUserInfo.getValue().userInfo.getNickname());
        view.gender.setText(vm.exUserInfo.getValue().userInfo.getGender() == 1 ? io.openim.android.ouicore.R.string.male
            : io.openim.android.ouicore.R.string.girl);
        long birth = vm.exUserInfo.getValue().userInfo.getBirth();
        if (birth != 0) {
            view.birthday.setText(TimeUtil.getTime(birth * 1000,
                TimeUtil.yearMonthDayFormat));
        }
        view.phoneTv.setText(vm.exUserInfo.getValue().userInfo.getPhoneNumber());
        view.mailTv.setText(vm.exUserInfo.getValue().userInfo.getEmail());
    }
}
