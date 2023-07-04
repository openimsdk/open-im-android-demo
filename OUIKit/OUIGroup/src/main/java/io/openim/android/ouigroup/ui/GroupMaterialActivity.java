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
import java.util.Map;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.MultipleChoiceVM;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SingleInfoModifyActivity;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupMaterialBinding;

import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouigroup.ui.v3.GroupManageActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupVerification;
import io.openim.android.sdk.enums.Opt;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.listener.OnPutFileListener;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.PutArgs;
import open_im_sdk_callback.PutFileCallback;

@Route(path = Routes.Group.MATERIAL)
public class GroupMaterialActivity extends BaseActivity<GroupVM, ActivityGroupMaterialBinding> {

    int spanCount = 5;
    private PhotographAlbumDialog albumDialog;
    private ActivityResultLauncher infoModifyLauncher;
    private int infoModifyType;
    private IConversationBridge iConversationBridge;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class, true);
        Easy.put(vm);
        vm.groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        conversationId = getIntent().getStringExtra(Constant.K_ID);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupMaterialBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);
        vm.getGroupsInfo();

        sink();

        init();
        initView();
        click();

        infoModifyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if (result.getResultCode() != RESULT_OK) return;
            String var =
                result.getData().getStringExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA);
            if (infoModifyType == 1) vm.UPDATEGroup(vm.groupId, var, null, null, null, null);
            if (infoModifyType == 2)
                vm.setGroupMemberNickname(vm.groupId, vm.loginCertificate.userID, var);
        });
    }

    void init() {
        iConversationBridge =
            (IConversationBridge) ARouter.getInstance().build(Routes.Service.CONVERSATION).navigation();
    }

    private void click() {
        view.topSlideButton.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.pinConversation(iConversationBridge.getConversationInfo(), is);
        });
        view.noDisturb.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.setConversationRecvMessageOpt(is ? Opt.ReceiveNotNotifyMessage : Opt.NORMAL,
                iConversationBridge.getConversationInfo().getConversationID());
        });
        view.chatHistory.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.CHAT_HISTORY).navigation();
        });
        view.photo.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.MEDIA_HISTORY)
                .withBoolean(Constant.K_RESULT, true).navigation();
        });
        view.video.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.MEDIA_HISTORY).navigation();
        });
        view.file.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.FILE_HISTORY).navigation();
        });
        view.qrCode.setOnClickListener(v -> startActivity(new Intent(this,
            ShareQrcodeActivity.class)));
        view.bulletin.setOnClickListener(v -> startActivity(new Intent(this,
            GroupBulletinActivity.class).putExtra(Constant.K_RESULT, vm.isOwner())));

        view.groupMember.setOnClickListener(v -> {
            gotoMemberList(false);
        });
        view.groupName.setOnClickListener(v -> {
            if (vm.isOwner()) {
                infoModifyType = 1;
                SingleInfoModifyActivity.SingleInfoModifyData modifyData =
                    new SingleInfoModifyActivity.SingleInfoModifyData();
                modifyData.title = "修改群聊名称";
                modifyData.description = "修改群聊名称后，将在群内通知其他成员。";
                modifyData.avatarUrl = vm.groupsInfo.getValue().getFaceURL();
                modifyData.editT = vm.groupsInfo.getValue().getGroupName();
                infoModifyLauncher.launch(new Intent(this, SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
            }
        });
        view.myName.setOnClickListener(v -> {
            infoModifyType = 2;
            SingleInfoModifyActivity.SingleInfoModifyData modifyData =
                new SingleInfoModifyActivity.SingleInfoModifyData();
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

        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_tips);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                iConversationBridge.clearCHistory(conversationId);
            });
        });
    }

    @Override
    protected void fasterDestroy() {
        removeCacheVM();
        Easy.delete(GroupVM.class);
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);

    }

    private void initView() {
        if (vm.isOwner()) view.quitGroup.setText(io.openim.android.ouicore.R.string.dissolve_group);
        albumDialog = new PhotographAlbumDialog(this);
        albumDialog.setOnSelectResultListener(path -> {
            PutArgs putArgs = new PutArgs(path[0]);
            putArgs.putID =
                BaseApp.inst().loginCertificate.userID + "_" + System.currentTimeMillis();
            OpenIMClient.getInstance().uploadFile(new OnFileUploadProgressListener() {
                @Override
                public void onError(int code, String error) {

                }

                @Override
                public void onProgress(long progress) {

                }

                @Override
                public void onSuccess(String s) {
                    try {
                        Map<String, String> fromJson = GsonHel.fromJson(s, Map.class);
                        vm.UPDATEGroup(vm.groupId, null, fromJson.get("url"), null, null, null);
                    } catch (Exception ignored) {
                    }
                }
            }, null, putArgs);
        });

        view.recyclerview.setLayoutManager(new GridLayoutManager(this, spanCount));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<GroupMembersInfo,
            ImageTxtViewHolder>(ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ImageTxtViewHolder holder, GroupMembersInfo data,
                                   int position) {
                LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) holder.view.img.getLayoutParams();
                layoutParams.topMargin = 0;
                layoutParams.width = Common.dp2px(48);
                layoutParams.height = Common.dp2px(48);

                holder.view.txt.setTextSize(12);
                holder.view.txt.setTextColor(getResources().getColor(io.openim.android.ouicore.R.color.txt_shallow));
                if (TextUtils.isEmpty(data.getGroupID())) {
                    //加/减按钮
                    int reId;
                    holder.view.img.load(reId = data.getRoleLevel());
                    holder.view.txt.setText(reId == R.mipmap.ic_group_add ?
                        io.openim.android.ouicore.R.string.add :
                        io.openim.android.ouicore.R.string.remove);
                    holder.view.getRoot().setOnClickListener(v -> {
                        startActivity(new Intent(GroupMaterialActivity.this,
                            InitiateGroupActivity.class).putExtra(reId == R.mipmap.ic_group_add ?
                            Constant.IS_INVITE_TO_GROUP : Constant.IS_REMOVE_GROUP, true));
                    });
                } else {
                    holder.view.img.load(data.getFaceURL(), data.getNickname());
                    holder.view.txt.setText(data.getNickname());
                    holder.view.getRoot().setOnClickListener(v -> {
                        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID,
                            data.getUserID()).withString(Constant.K_GROUP_ID, vm.groupId).navigation();
                    });
                }
            }

        };
        view.recyclerview.setAdapter(adapter);
        vm.groupsInfo.observe(this, groupInfo -> {
            view.avatar.load(groupInfo.getFaceURL(), true);
            vm.getGroupMemberList(spanCount * 2);
            view.quitGroup.setText(getString(vm.isOwner() ?
                io.openim.android.ouicore.R.string.dissolve_group :
                io.openim.android.ouicore.R.string.quit_group));

            view.all.setText(String.format(getResources().getString(io.openim.android.ouicore.R.string.view_all_member), groupInfo.getMemberCount()));
        });
        vm.groupMembers.observe(this, groupMembersInfos -> {
            if (groupMembersInfos.isEmpty()) return;
            spanCount = spanCount * 2;
            boolean owner = vm.isOwner();
            int end = owner ? spanCount - 2 : spanCount - 1;
            if (groupMembersInfos.size() < end) {
                end = groupMembersInfos.size();
            }
            List<GroupMembersInfo> groupMembersInfos1 =
                new ArrayList<>(groupMembersInfos.subList(0, end));
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
            view.noDisturb.post(() -> view.noDisturb.setCheckedWithAnimation(data == Opt.ReceiveNotNotifyMessage));
        });
        iConversationBridge.setConversationInfoChangeListener(this, data -> {
            view.topSlideButton.post(() -> view.topSlideButton.setCheckedWithAnimation(data.isPinned()));
        });
        view.groupManage.setOnClickListener(v -> {
            startActivity(new Intent(this, GroupManageActivity.class));
        });

    }


    private void gotoMemberList(boolean transferPermissions) {
//        if (vm.groupMembers.getValue().isEmpty()) return;
//        if (vm.groupMembers.getValue().size() > Constant.SUPER_GROUP_LIMIT)
        startActivity(new Intent(this, SuperGroupMemberActivity.class).putExtra(Constant.K_FROM,
            transferPermissions));
//        else
//            startActivity(new Intent(GroupMaterialActivity.this, GroupMemberActivity.class)
//            .putExtra(Constant.K_FROM, transferPermissions));
    }

}
