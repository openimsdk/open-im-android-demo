package io.openim.android.ouigroup.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.databinding.LayoutMemberActionBinding;
import io.openim.android.ouigroup.databinding.ActivitySuperGroupMemberBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.GroupMembersInfo;

public class SuperGroupMemberActivity extends BaseActivity<GroupVM, ActivitySuperGroupMemberBinding> {
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySuperGroupMemberBinding.inflate(getLayoutInflater()));
        sink();

        init();
        initView();
        listener();
    }

    void init() {
        if (vm.superGroupMembers.getValue().size() >
            GroupMaterialActivity.SUPER_GROUP_LIMIT) {
            vm.superGroupMembers.getValue().clear();
            vm.page = 0;
            vm.pageSize = 20;
        }
    }

    private void listener() {
        view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getItems().size() - 1
                    && adapter.getItems().size() >= vm.pageSize) {
                    vm.page++;
                    loadMember();
                }
            }
        });
        view.more.setOnClickListener(v -> {
            PopupWindow popupWindow = new PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutMemberActionBinding view = LayoutMemberActionBinding.inflate(getLayoutInflater());
            view.deleteFriend.setVisibility(vm.isOwner() ? View.VISIBLE : View.GONE);
            view.addFriend.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, InitiateGroupActivity.class)
                    .putExtra(InitiateGroupActivity.IS_INVITE_TO_GROUP, true));
            });
            view.deleteFriend.setOnClickListener(v1 -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, InitiateGroupActivity.class)
                    .putExtra(InitiateGroupActivity.IS_REMOVE_GROUP, true));
            });
            //设置PopupWindow的视图内容
            popupWindow.setContentView(view.getRoot());
            //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
            popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            popupWindow.setOutsideTouchable(true);

            //设置PopupWindow消失监听
            popupWindow.setOnDismissListener(() -> {

            });
            //PopupWindow在targetView下方弹出
            popupWindow.showAsDropDown(v);
        });

        vm.superGroupMembers.observe(this, v -> {
            if (v.isEmpty()) return;
            adapter.notifyItemRangeInserted(vm.exGroupManagement.getValue().size() - vm.pageSize,
                vm.exGroupManagement.getValue().size());
        });
    }

    private void loadMember() {
        vm.getSuperGroupMemberList();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<GroupMembersInfo, RecyclerView.ViewHolder>() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, GroupMembersInfo data, int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                itemViewHo.view.avatar.load(data.getFaceURL());
                itemViewHo.view.nickName.setText(data.getNickname());
                itemViewHo.view.select.setVisibility(View.GONE);
                if (data.getRoleLevel() == 2) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_fddfa1);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.lord);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#ffff8c00"));
                } else if (data.getRoleLevel() == 3) {
                    itemViewHo.view.identity.setVisibility(View.VISIBLE);
                    itemViewHo.view.identity.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_8_a2c9f8);
                    itemViewHo.view.identity.setText(io.openim.android.ouicore.R.string.lord);
                    itemViewHo.view.identity.setTextColor(Color.parseColor("#2691ED"));
                } else
                    itemViewHo.view.identity.setVisibility(View.GONE);
            }
        };
        adapter.setItems(vm.superGroupMembers.getValue());
        view.recyclerview.setAdapter(adapter);

        loadMember();
    }

}
