package io.openim.android.ouimoments.ui;

import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.bean.User;
import io.openim.android.ouimoments.databinding.ActivityMomentsHomeBinding;
import io.openim.android.ouimoments.ui.fragment.CircleFragment;

public class ToUserMomentsActivity extends BaseActivity {

    CircleFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMomentsHomeBinding.inflate(getLayoutInflater()));
        sink(null);
        User user= (User) getIntent().getSerializableExtra(Constant.K_RESULT);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = CircleFragment.newInstance(user);
        if (!fragment.isAdded()) {
            transaction.add(R.id.contentFl, fragment);
            transaction.show(fragment).commit();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
    }
}
