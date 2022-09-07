package io.openim.android.demo.ui.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.ouicore.base.BaseActivity;

public class PersonInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding();
        setContentView(R.layout.activity_person_info);
    }
}
