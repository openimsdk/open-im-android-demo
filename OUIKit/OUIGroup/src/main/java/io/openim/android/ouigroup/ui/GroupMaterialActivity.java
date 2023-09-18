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

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.IConversationBridge;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.SingleInfoModifyActivity;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupMaterialBinding;

import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouigroup.ui.v3.GroupManageActivity;
import io.openim.android.ouigroup.ui.v3.SelectTargetActivityV3;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.Opt;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.PutArgs;

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
        vm.setContext(this);
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
                    if (infoModifyType == 1)
                        vm.UPDATEGroup(vm.groupId, var, null, null, null, null);
                    if (infoModifyType == 2)
                        vm.setGroupMemberNickname(vm.groupId, BaseApp.inst().loginCertificate.userID, var);
                });
    }


    void init() {
        iConversationBridge =
            (IConversationBridge) ARouter.getInstance().build(Routes.Service.CONVERSATION).navigation();
    }

    private void click() {
        view.groupId.setOnClickListener(v -> {
            Common.copy(vm.groupId);
            toast(getString(io.openim.android.ouicore.R.string.copy_succ));
        });
        view.topSlideButton.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.pinConversation(iConversationBridge.getConversationInfo(), is);
        });
        view.noDisturb.setOnSlideButtonClickListener(is -> {
            if (null == iConversationBridge) return;
            iConversationBridge.setConversationRecvMessageOpt(is ? Opt.ReceiveNotNotifyMessage :
                Opt.NORMAL, iConversationBridge.getConversationInfo().getConversationID());
        });
        view.chatHistory.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.CHAT_HISTORY).navigation();
        });
        view.photo.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Conversation.MEDIA_HISTORY).withBoolean(Constant.K_RESULT, true).navigation();
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
                try {
                    infoModifyType = 1;
                    SingleInfoModifyActivity.SingleInfoModifyData modifyData =
                        new SingleInfoModifyActivity.SingleInfoModifyData();
                    modifyData.title =
                        getString(io.openim.android.ouicore.R.string.edit_group_name2);
                    modifyData.description =
                        getString(io.openim.android.ouicore.R.string.edit_group_name_tips);
                    modifyData.avatarUrl = vm.groupsInfo.getValue().getFaceURL();
                    modifyData.editT = vm.groupsInfo.getValue().getGroupName();
                    infoModifyLauncher.launch(new Intent(this, SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
                } catch (Exception ignored) {
                }
            }
        });
        view.myName.setOnClickListener(v -> {
            try {
                infoModifyType = 2;
                SingleInfoModifyActivity.SingleInfoModifyData modifyData =
                    new SingleInfoModifyActivity.SingleInfoModifyData();
                modifyData.title = getString(io.openim.android.ouicore.R.string.in_group_name);
                modifyData.description =
                    getString(io.openim.android.ouicore.R.string.in_group_name_tips);
                ExGroupMemberInfo exGroupMemberInfo = vm.getOwnInGroup(BaseApp.inst().loginCertificate.userID);
                if (null == exGroupMemberInfo) {
                    WaitDialog waitDialog = new WaitDialog(this);
                    waitDialog.show();
                    vm.getGroupMembersInfo(new IMUtil.IMCallBack<List<GroupMembersInfo>>() {
                        @Override
                        public void onError(int code, String error) {
                            waitDialog.dismiss();
                        }

                        @Override
                        public void onSuccess(List<GroupMembersInfo> data) {
                            waitDialog.dismiss();
                            if (data.isEmpty()) return;
                            GroupMembersInfo membersInfo = data.get(0);
                            modifyData.avatarUrl = membersInfo.getFaceURL();
                            modifyData.editT = membersInfo.getNickname();
                            infoModifyLauncher.launch(new Intent(GroupMaterialActivity.this,
                                SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
                        }
                    }, new ArrayList<>(Collections.singleton(BaseApp.inst().loginCertificate.userID)));
                } else {
                    modifyData.avatarUrl = exGroupMemberInfo.groupMembersInfo.getFaceURL();
                    modifyData.editT = exGroupMemberInfo.groupMembersInfo.getNickname();
                    infoModifyLauncher.launch(new Intent(this, SingleInfoModifyActivity.class).putExtra(SingleInfoModifyActivity.SINGLE_INFO_MODIFY_DATA, modifyData));
                }
            } catch (Exception ignored) {
            }
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
                holder.view.img.setVisibility(TextUtils.isEmpty(data.getGroupID()) ? View.GONE :
                    View.VISIBLE);
                holder.view.img2.setVisibility(TextUtils.isEmpty(data.getGroupID()) ?
                    View.VISIBLE : View.GONE);
                if (TextUtils.isEmpty(data.getGroupID())) {
                    //加/减按钮
                    int reId;
                    holder.view.img2.setImageResource(reId = data.getRoleLevel());
                    holder.view.txt.setText(reId == R.mipmap.ic_group_add ?
                        io.openim.android.ouicore.R.string.add :
                        io.openim.android.ouicore.R.string.remove);
                    holder.view.getRoot().setOnClickListener(v -> {
                        boolean isAdd = reId == R.mipmap.ic_group_add;
                        if (isAdd) {
                            SelectTargetVM sv = Easy.installVM(SelectTargetVM.class);
                            sv.setIntention(SelectTargetVM.Intention.invite);
                            ContactListVM ctv = BaseApp.inst().getVMByCache(ContactListVM.class);
                            List<String> ids = new ArrayList<>();
                            for (MsgConversation msgConversation : ctv.conversations.val()) {
                                if (msgConversation.conversationInfo.getConversationType() == ConversationType.SINGLE_CHAT) {
                                    ids.add(msgConversation.conversationInfo.getUserID());
                                }
                            }
                            sv.isInGroup(GroupMaterialActivity.this.vm.groupId, ids);
                            sv.setOnFinishListener(() -> {
                                List<String> selectIds = new ArrayList<>();
                                for (MultipleChoice choice : sv.inviteList.val()) {
                                    selectIds.add(choice.key);
                                }
                                vm.inviteUserToGroup(selectIds);
                            });
                            startActivity(new Intent(GroupMaterialActivity.this,
                                SelectTargetActivityV3.class));
                        }
                    });
                } else {
                    holder.view.img.load(data.getFaceURL(), data.getNickname());
                    holder.view.txt.setText(data.getNickname());
                    holder.view.getRoot().setOnClickListener(v -> {
                        ARouter.getInstance().build(Routes.Main.PERSON_DETAIL).withString(Constant.K_ID, data.getUserID()).withString(Constant.K_GROUP_ID, vm.groupId).navigation();
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
            if (groupInfo.getMemberCount() == 0) {
                view.quitGroup.setText(io.openim.android.ouicore.R.string.delete_conversiton);
                view.quitGroup.setOnClickListener(new OnDedrepClickListener() {
                    @Override
                    public void click(View v) {
                        ContactListVM contactListVM =
                            BaseApp.inst().getVMByCache(ContactListVM.class);
                        if (null != contactListVM) {
                            contactListVM.deleteConversationAndDeleteAllMsg(conversationId);
                            Postcard postcard =
                                ARouter.getInstance().build(Routes.Conversation.CHAT);
                            LogisticsCenter.completion(postcard);
                            ActivityManager.finishActivity(postcard.getDestination());
                            finish();
                        }
                    }
                });
            }
        });

        vm.groupMembers.observe(this, groupMembersInfos -> {
            if (groupMembersInfos.isEmpty()) return;
            view.all.setText(String.format(getResources().getString(io.openim.android.ouicore.R.string.view_all_member), vm.groupsInfo.getValue().getMemberCount()));
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
            spanCount = 5;
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
