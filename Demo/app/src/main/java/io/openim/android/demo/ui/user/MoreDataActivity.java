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
        vm.getUserInfo(vm.uid);
        listener();
    }

    private void listener() {
        vm.userInfo.observe(this,v->{
            if (v==null)return;
            init();
        });
    }

    void init() {
        view.avatar.load(vm.userInfo.val().getFaceURL());
        view.nickName.setText(vm.userInfo.val().getNickname());
        view.gender.setText(vm.userInfo.val().getGender() == 1 ? io.openim.android.ouicore.R.string.male
            : io.openim.android.ouicore.R.string.girl);
        long birth = vm.userInfo.val().getBirth();
        if (birth != 0) {
            view.birthday.setText(TimeUtil.getTime(birth ,
                TimeUtil.yearMonthDayFormat));
        }
        view.phoneTv.setText(vm.userInfo.val().getPhoneNumber());
        view.mailTv.setText(vm.userInfo.val().getEmail());
    }
    
}
