package io.openim.android.demo.ui.user;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import io.openim.android.demo.databinding.ActivityBlackListBinding;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.sdk.models.UserInfo;

public class BlackListActivity extends BaseActivity<FriendVM, ActivityBlackListBinding> {

    private RecyclerViewAdapter<UserInfo, ViewHol.ContactItemHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(FriendVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityBlackListBinding.inflate(getLayoutInflater()));
        initView();
        vm.getBlacklist();
        listener();
    }

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<UserInfo, ViewHol.ContactItemHolder>(ViewHol.ContactItemHolder.class) {
            @Override
            public void onBindView(@NonNull ViewHol.ContactItemHolder holder, UserInfo data, int position) {
                holder.viewBinding.bottom.setVisibility(View.GONE);
                holder.viewBinding.expand.setVisibility(View.GONE);

                holder.viewBinding.avatar.load(data.getFaceURL());
                holder.viewBinding.nickName.setText(data.getNickname());
            }
        });
    }

    private void listener() {
        vm.blackListUser.observe(this, userInfos -> {
            adapter.setItems(userInfos);
        });
    }
}
