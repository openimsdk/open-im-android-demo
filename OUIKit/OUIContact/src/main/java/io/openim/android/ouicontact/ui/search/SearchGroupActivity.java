package io.openim.android.ouicontact.ui.search;

import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.List;

import io.openim.android.ouicontact.databinding.ActivitySearchGroupBinding;
import io.openim.android.ouicontact.ui.MyGroupActivity;
import io.openim.android.ouicontact.vm.SearchGroup;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.sdk.models.GroupInfo;

public class SearchGroupActivity extends BaseActivity<SearchGroup, ActivitySearchGroupBinding> {

    private MyGroupActivity.ContentAdapter adapter;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(SearchGroup.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchGroupBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
    }

    private void listener() {
        view.cancel.setOnClickListener(v -> finish());
        vm.searchGroups.observe(this, groupInfos -> adapter.setItems(groupInfos));
        vm.searchKey.observe(this, s -> {
            vm.search(s);
        });
        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) vm.searchKey.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    vm.searchKey.setValue(input);
                }, 300);
            }
        });
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyGroupActivity.ContentAdapter(ViewHol.GroupViewHo.class);
        view.recyclerview.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

    public static void jumpThis(Context ctx, List<GroupInfo> groupInfos) {
        SearchGroup searchGroup = new SearchGroup();
        searchGroup.groups.setValue(groupInfos);
        BaseApp.inst().putVM(searchGroup);
        ctx.startActivity(new Intent(ctx, SearchGroupActivity.class));
    }
}
