package io.openim.android.demo.ui.user;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;

import io.openim.android.demo.databinding.ActivityBlackListBinding;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.sdk.models.UserInfo;

public class BlackListActivity extends BaseActivity<FriendVM, ActivityBlackListBinding> {

    private RecyclerViewAdapter<UserInfo, ViewHol.ContactItemHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(FriendVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityBlackListBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        vm.getBlacklist();
        listener();
    }

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SwipeMenuCreator mSwipeMenuCreator = (leftMenu, rightMenu, position) -> {
            SwipeMenuItem delete = new SwipeMenuItem(this);
            delete.setText(io.openim.android.ouicore.R.string.remove);
            delete.setHeight(MATCH_PARENT);
            delete.setWidth(Common.dp2px(73));
            delete.setTextSize(16);
            delete.setTextColor(this.getResources().getColor(android.R.color.white));
            delete.setBackgroundColor(Color.parseColor("#FFAB41"));

            rightMenu.addMenuItem(delete);
        };
        view.recyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        view.recyclerView.setOnItemMenuClickListener((menuBridge, adapterPosition) -> {
            menuBridge.closeMenu();
            UserInfo u =adapter.getItems().get(adapterPosition);
            vm.removeBlacklist(u.getUserID());
        });

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
            if (userInfos.isEmpty()){
                view.empty.setVisibility(View.VISIBLE);
                view.recyclerView.setVisibility(View.GONE);
            }else {
                view.recyclerView.setVisibility(View.VISIBLE);
                adapter.setItems(userInfos);

                view.empty.setVisibility(View.GONE);
            }

        });
    }
}
