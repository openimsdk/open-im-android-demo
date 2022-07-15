package io.openim.android.ouigroup.ui;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SingleInfoModifyActivity;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupMaterialBinding;

import io.openim.android.ouigroup.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.MATERIAL)
public class GroupMaterialActivity extends BaseActivity<GroupVM, ActivityGroupMaterialBinding> {
    int spanCount = 7;
    private PhotographAlbumDialog albumDialog;
    private ActivityResultLauncher infoModifyLauncher;
    private int infoModifyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class, true);
        vm.groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupMaterialBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);
        vm.getGroupsInfo();

        sink();

        initView();
        click();

        infoModifyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;
            String var = result.getData().getStringExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA);
            if (infoModifyType == 1)
                vm.updataGroup(vm.groupId, var, null, null, null, null);
            if (infoModifyType == 2)
                vm.setGroupMemberNickname(vm.groupId, vm.loginCertificate.userID, var);
        });
    }

    private void click() {
        view.qrCode.setOnClickListener(v -> startActivity(new Intent(this, GroupShareActivity.class)));
        view.groupId.setOnClickListener(v -> startActivity(new Intent(this, GroupShareActivity.class).putExtra(GroupShareActivity.IS_QRCODE, false)));
        view.bulletin.setOnClickListener(v -> startActivity(new Intent(this, GroupBulletinActivity.class)));
        view.groupMember.setOnClickListener(v -> {
            startActivity(new Intent(this, GroupMemberActivity.class));
        });
        view.groupName.setOnClickListener(v -> {
            if (vm.isOwner()) {
                infoModifyType = 1;
                SingleInfoModifyActivity.SingleInfoModifyData modifyData = new SingleInfoModifyActivity.SingleInfoModifyData();
                modifyData.title = "修改群聊名称";
                modifyData.description = "修改群聊名称后，将在群内通知其他成员。";
                modifyData.avatarUrl = vm.groupsInfo.getValue().getFaceURL();
                modifyData.editT = vm.groupsInfo.getValue().getGroupName();
                infoModifyLauncher.launch(new Intent(this, SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
            }
        });
        view.myName.setOnClickListener(v -> {
            infoModifyType = 2;
            SingleInfoModifyActivity.SingleInfoModifyData modifyData = new SingleInfoModifyActivity.SingleInfoModifyData();
            modifyData.title = "我在群里的昵称";
            modifyData.description = "昵称修改后，只会在此群内显示，群内成员都可以看见。";
            ExGroupMemberInfo exGroupMemberInfo = vm.getOwnInGroup(vm.loginCertificate.userID);
            modifyData.avatarUrl = exGroupMemberInfo.groupMembersInfo.getFaceURL();
            modifyData.editT = exGroupMemberInfo.groupMembersInfo.getNickname();
            infoModifyLauncher.launch(new Intent(this, SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
        });
        view.avatar.setOnClickListener(v -> {
            albumDialog.show();
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            removeCacheVM();
    }


    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);

    }

    private void initView() {
        albumDialog = new PhotographAlbumDialog(this);
        albumDialog.setOnSelectResultListener(path -> {
            OpenIMClient.getInstance().uploadFile(new OnFileUploadProgressListener() {
                @Override
                public void onError(int code, String error) {

                }

                @Override
                public void onProgress(long progress) {

                }

                @Override
                public void onSuccess(String s) {
                    vm.updataGroup(vm.groupId, null, s, null, null, null);
                }
            }, path);

        });

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
            view.avatar.load(groupInfo.getFaceURL());
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
