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
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SingleInfoModifyActivity;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupMaterialBinding;

import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupVerification;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.MATERIAL)
public class GroupMaterialActivity extends BaseActivity<GroupVM, ActivityGroupMaterialBinding> {

    int spanCount = 7;
    private PhotographAlbumDialog albumDialog;
    private ActivityResultLauncher infoModifyLauncher;
    private int infoModifyType;
    private IConversationBridge iConversationBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class, true);
        vm.groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupMaterialBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);
        vm.getGroupsInfo();

        sink();

        init();
        initView();
        click();

        infoModifyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK) return;
            String var = result.getData().getStringExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA);
            if (infoModifyType == 1) vm.updataGroup(vm.groupId, var, null, null, null, null);
            if (infoModifyType == 2)
                vm.setGroupMemberNickname(vm.groupId, vm.loginCertificate.userID, var);
        });
    }

    void init() {
        iConversationBridge = (IConversationBridge) ARouter.getInstance().build(Routes.Service.CONVERSATION).navigation();
    }

    private void click() {
        view.memberPermissions.setOnClickListener(v -> {
            startActivity(new Intent(this, MemberPermissionActivity.class));
        });
        view.joinValidation.setOnClickListener(v -> {
            BottomPopDialog dialog = new BottomPopDialog(this);
            dialog.show();
            dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
            dialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.allowAnyoneJoinGroup);
            dialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.inviteNotVerification);
            dialog.getMainView().menu4.setVisibility(View.VISIBLE);
            dialog.getMainView().menu4.setText(io.openim.android.ouicore.R.string.needVerification);

            dialog.getMainView().menu1.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(Constant.GroupVerification.directly, data -> {
                    vm.groupsInfo.getValue().setNeedVerification(Constant.GroupVerification.directly);
                    vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                });
            });
            dialog.getMainView().menu2.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(Constant.GroupVerification.applyNeedVerificationInviteDirectly, data -> {
                    vm.groupsInfo.getValue().setNeedVerification(Constant.GroupVerification.applyNeedVerificationInviteDirectly);
                    vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                });
            });
            dialog.getMainView().menu4.setOnClickListener(v1 -> {
                dialog.dismiss();
                vm.setGroupVerification(Constant.GroupVerification.allNeedVerification, data -> {
                    vm.groupsInfo.getValue().setNeedVerification(Constant.GroupVerification.allNeedVerification);
                    vm.groupsInfo.setValue(vm.groupsInfo.getValue());
                });
            });
        });
        view.totalSilence.setOnSlideButtonClickListener(isChecked -> {
            vm.changeGroupMute(isChecked, data -> {
                view.totalSilence.setCheckedWithAnimation(isChecked);
            });
        });
        view.topSlideButton.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.pinConversation(iConversationBridge.getConversationInfo(), is);
        });
        view.noDisturb.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.setConversationRecvMessageOpt(is ? 2 : 0, iConversationBridge.getConversationInfo().getConversationID());
        });
        view.chatHistory.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.CHAT_HISTORY).navigation();
        });
        view.qrCode.setOnClickListener(v -> startActivity(new Intent(this, ShareQrcodeActivity.class)));
        view.groupId.setOnClickListener(v -> startActivity(new Intent(this, ShareQrcodeActivity.class).putExtra(ShareQrcodeActivity.IS_QRCODE, false)));
        view.bulletin.setOnClickListener(v -> startActivity(new Intent(this, GroupBulletinActivity.class).putExtra(Constant.K_RESULT, vm.isOwner())));

        view.groupMember.setOnClickListener(v -> {
            gotoMemberList(false);
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
        view.avatarEdit.setOnClickListener(v -> {
            if (!vm.isGroupOwner.getValue()) return;
            albumDialog.show();
        });

        view.quitGroup.setOnClickListener(v -> {
            if (vm.isOwner()) {
                vm.dissolveGroup();
            } else {
                vm.quitGroup();
            }
        });
        view.transferPermissions.setOnClickListener(v -> {
            gotoMemberList(true);
        });
        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_tips);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                iConversationBridge.clearCHistory(vm.groupId);
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) removeCacheVM();
    }


    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);

    }

    private void initView() {
        if (vm.isOwner()) view.quitGroup.setText(io.openim.android.ouicore.R.string.dissolve_group);
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
                    int reId;
                    holder.view.img.load(reId = data.getRoleLevel());
                    holder.view.getRoot().setOnClickListener(v -> {
                        startActivity(new Intent(GroupMaterialActivity.this, InitiateGroupActivity.class).putExtra(reId == R.mipmap.ic_group_add ? InitiateGroupActivity.IS_INVITE_TO_GROUP : InitiateGroupActivity.IS_REMOVE_GROUP, true));
                    });
                } else {
                    layoutParams.topMargin = 0;
                    layoutParams.width = Common.dp2px(42);
                    layoutParams.height = Common.dp2px(42);
                    holder.view.img.load(data.getFaceURL());
                }
                holder.view.txt.setVisibility(View.GONE);

            }

        };
        view.recyclerview.setAdapter(adapter);
        vm.groupsInfo.observe(this, groupInfo -> {
            view.avatar.load(groupInfo.getFaceURL());
            vm.getGroupMemberList();
            view.totalSilence.setCheckedWithAnimation(groupInfo.getStatus() == Constant.GroupStatus.status3);

            view.describe.setText(getJoinGroupOption(groupInfo.getNeedVerification()));
            view.groupType.setText(groupInfo.getGroupType() == 2 ? getString(io.openim.android.ouicore.R.string.super_group) : getString(io.openim.android.ouicore.R.string.ordinary_group));

            view.quitGroup.setText(getString(vm.isOwner() ? io.openim.android.ouicore.R.string.dissolve_group : io.openim.android.ouicore.R.string.quit_group));
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

        iConversationBridge.setNotDisturbStatusListener(this, data -> {
            view.noDisturb.post(() -> view.noDisturb.setCheckedWithAnimation(data == 2));
        });
        iConversationBridge.setConversationInfoChangeListener(this, data -> {
            view.topSlideButton.post(() -> view.topSlideButton.setCheckedWithAnimation(data.isPinned()));
        });

    }

    String getJoinGroupOption(int value) {
        if (value == Constant.GroupVerification.allNeedVerification) {
            return getString(io.openim.android.ouicore.R.string.needVerification);
        } else if (value == Constant.GroupVerification.directly) {
            return getString(io.openim.android.ouicore.R.string.allowAnyoneJoinGroup);
        }
        return getString(io.openim.android.ouicore.R.string.inviteNotVerification);
    }

    private void gotoMemberList(boolean transferPermissions) {
        if (vm.groupMembers.getValue().isEmpty()) return;
        if (vm.groupMembers.getValue().size() > Constant.SUPER_GROUP_LIMIT)
            startActivity(new Intent(GroupMaterialActivity.this, SuperGroupMemberActivity.class).putExtra(Constant.K_FROM, transferPermissions));
        else
            startActivity(new Intent(GroupMaterialActivity.this, GroupMemberActivity.class).putExtra(Constant.K_FROM, transferPermissions));
    }
}
