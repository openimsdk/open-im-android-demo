package io.openim.android.ouimoments;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouimoments.ui.fragment.CircleFragment;
@Route(path = Routes.Moments.HOME)
public class MomentsActivity extends BaseActivity {
    CircleFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) actionBar.hide();
        setContentView(R.layout.activity_moments_home);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = CircleFragment.newInstance(null);
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
