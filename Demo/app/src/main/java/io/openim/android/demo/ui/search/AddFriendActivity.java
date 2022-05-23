package io.openim.android.demo.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityAddFriendBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.SinkHelper;

public class AddFriendActivity extends BaseActivity {
    ActivityAddFriendBinding view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view= ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(view.getRoot());
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        view.input.getEditText().setHint(R.string.search_by_id);
        view.back.back.setOnClickListener(v->finish());
        view.input.setOnClickListener(v -> {
            startActivity(new Intent(this,SearchPersonActivity.class));
        });
    }


}