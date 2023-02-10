package io.openim.android.ouimoments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouimoments.bean.User;
import io.openim.android.ouimoments.ui.CircleFragment;

public class ToUserMomentsActivity extends AppCompatActivity {

    CircleFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User user= (User) getIntent().getSerializableExtra(Constant.K_RESULT);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = CircleFragment.newInstance(user);
        if (!fragment.isAdded()) {
            transaction.add(R.id.frameLayout, fragment);
            transaction.show(fragment).commit();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
    }
}
