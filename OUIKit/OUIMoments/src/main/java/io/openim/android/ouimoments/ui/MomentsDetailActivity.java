package io.openim.android.ouimoments.ui;

import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.databinding.ActivityMomentsDetailBinding;
import io.openim.android.ouimoments.ui.fragment.MomentsDetailFragment;

public class MomentsDetailActivity extends BaseActivity {

    private String momentsID;
    private MomentsDetailFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMomentsDetailBinding.inflate(getLayoutInflater()));
        sink();
        momentsID = getIntent().getStringExtra(Constant.K_ID);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment = MomentsDetailFragment.newInstance(momentsID);
        transaction.add(R.id.contentFl, fragment)
            .show(fragment).commit();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
    }

}
