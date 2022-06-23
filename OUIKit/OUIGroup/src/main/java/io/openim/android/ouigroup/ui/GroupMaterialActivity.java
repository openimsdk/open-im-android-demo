package io.openim.android.ouigroup.ui;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.SpacesItemDecoration;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupDetailBinding;
import io.openim.android.ouigroup.databinding.ActivityGroupMaterialBinding;
import io.openim.android.ouigroup.vm.GroupVM;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.MATERIAL)
public class GroupMaterialActivity extends BaseActivity<GroupVM, ActivityGroupMaterialBinding> {
    int spanCount = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class, true);
        vm.groupId = getIntent().getStringExtra(Constant.GROUP_ID);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupMaterialBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);
        vm.getGroupsInfo();

        sink();

        initView();
        click();
    }

    private void click() {
        view.groupMember.setOnClickListener(v -> {
            startActivity(new Intent(this, GroupMemberActivity.class));
        });
        view.groupName.setOnClickListener(v -> {
            if (vm.isOwner()){
                startActivity(new Intent(this,));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new GridLayoutManager(this, spanCount));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<GroupMembersInfo, ImageTxtViewHolder>(ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ImageTxtViewHolder holder, GroupMembersInfo data, int position) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.view.img.getLayoutParams();
                if (TextUtils.isEmpty(data.getUserID())) {
                    layoutParams.width = Common.dp2px(36);
                    layoutParams.height = Common.dp2px(36);
                    layoutParams.topMargin = Common.dp2px(3);
                    holder.view.img.load(data.getRoleLevel());
                } else {
                    layoutParams.topMargin = 0;
                    layoutParams.width = Common.dp2px(42);
                    layoutParams.height = Common.dp2px(42);
                    holder.view.img.load(data.getFaceURL());
                }
                holder.view.txt.setVisibility(View.GONE);
                holder.view.getRoot().setOnClickListener(v -> startActivity(new Intent(GroupMaterialActivity.this, GroupMemberActivity.class)));
            }

        };
        view.recyclerview.setAdapter(adapter);
        vm.groupsInfo.observe(this, groupInfo -> {
            vm.getGroupMemberList();
        });

        vm.groupMembers.observe(this, groupMembersInfos -> {
            boolean owner;
            int end = (owner = vm.isOwner()) ? spanCount - 2 : spanCount - 1;
            if (groupMembersInfos.size() < end) {
                end = groupMembersInfos.size();
            }
            List<GroupMembersInfo> groupMembersInfos1 = new ArrayList<>(groupMembersInfos.subList(0, end));
            GroupMembersInfo add = new GroupMembersInfo();
            add.setRoleLevel(R.mipmap.ic_group_add);
            groupMembersInfos1.add(add);

            if (owner) {
                GroupMembersInfo reduce = new GroupMembersInfo();
                reduce.setRoleLevel(R.mipmap.ic_group_reduce);
                groupMembersInfos1.add(reduce);
            }

            adapter.setItems(groupMembersInfos1);

        });

    }
}
