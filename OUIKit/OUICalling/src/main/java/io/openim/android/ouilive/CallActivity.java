package io.openim.android.ouilive;

import android.os.Bundle;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Calling.CALL)
public class CallActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call);
    }
}
