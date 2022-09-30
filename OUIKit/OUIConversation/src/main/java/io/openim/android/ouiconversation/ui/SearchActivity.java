package io.openim.android.ouiconversation.ui;


import android.os.Bundle;

import io.openim.android.ouiconversation.databinding.ActivitySearchBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.vm.SearchVM;

public class SearchActivity extends BaseActivity<SearchVM, ActivitySearchBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchBinding.inflate(getLayoutInflater()));
        sink();


    }
}
