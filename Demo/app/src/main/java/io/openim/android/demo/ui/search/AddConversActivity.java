package io.openim.android.demo.ui.search;

import android.content.Intent;
import android.os.Bundle;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityAddFriendBinding;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.SinkHelper;

public class AddConversActivity extends BaseActivity<SearchVM, ActivityAddFriendBinding> {


    public static final String IS_PERSON = "is_person";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class,true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAddFriendBinding.inflate(getLayoutInflater()));
        vm.isPerson=getIntent().getBooleanExtra(IS_PERSON,true);

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());

        initView();
    }

    private void initView() {
        view.input.getEditText().setHint(vm.isPerson? io.openim.android.ouicore.R.string.search_by_id:R.string.search_group_by_id);
        view.back.back.setOnClickListener(v->finish());
        view.input.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchConversActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }
}
