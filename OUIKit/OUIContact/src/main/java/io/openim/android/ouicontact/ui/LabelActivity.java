package io.openim.android.ouicontact.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityLabelBinding;
import io.openim.android.ouicontact.vm.LabelVM;
import io.openim.android.ouicore.base.BaseActivity;

public class LabelActivity extends BaseActivity<LabelVM, ActivityLabelBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(LabelVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityLabelBinding.inflate(getLayoutInflater()));
        sink();

        init();
    }
    void init(){
        vm.getUserTags();
    }
}
