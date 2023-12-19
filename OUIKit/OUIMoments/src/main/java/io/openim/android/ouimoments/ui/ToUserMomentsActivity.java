package io.openim.android.ouimoments.ui;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.Map;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.bean.User;
import io.openim.android.ouimoments.databinding.ActivityMomentsHomeBinding;
import io.openim.android.ouimoments.ui.fragment.CircleFragment;

@Route(path = Routes.Moments.ToUserMoments)
public class ToUserMomentsActivity extends BaseActivity {

    CircleFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moments_home);
        sink(null);
        User user = null;
        try {
            user =
                (User) getIntent().getSerializableExtra(Constant.K_RESULT);
        } catch (Exception ignore) {}
        try {
            Map<String,String> map =
                ( Map<String,String>) getIntent().getSerializableExtra(Constant.K_RESULT);
            user=new User(map.get("id"),
                map.get("name"), map.get("headUrl"));
        } catch (Exception ignore) {}

        if (null!=user){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment = CircleFragment.newInstance(user);
            transaction.add(R.id.contentFl, fragment);
            transaction.show(fragment).commit();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
    }
}
