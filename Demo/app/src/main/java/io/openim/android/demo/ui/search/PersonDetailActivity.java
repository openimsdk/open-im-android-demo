package io.openim.android.demo.ui.search;


import static io.openim.android.ouicore.utils.Constants.ActivityResult.DELETE_FRIEND;
import static io.openim.android.ouicore.utils.Constants.ActivityResult.SET_REMARK;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.demo.databinding.ActivityPersonDetailBinding;
import io.openim.android.demo.ui.user.MoreDataActivity;
import io.openim.android.demo.ui.user.PersonDataActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouiconversation.ui.PreviewMediaActivity;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMBack;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.ouigroup.ui.SetMuteActivity;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.AllowType;
import io.openim.android.sdk.enums.GroupRole;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.FriendApplicationInfo;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.SetGroupMemberInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.UserInfo;
import io.openim.android.ouicore.entity.BurnAfterReadingNotification;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.widget.SlideButton;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.observers.DisposableObserver;

@Route(path = Routes.Main.PERSON_DETAIL)
public class PersonDetailActivity extends BaseActivity<SearchVM, ActivityPersonDetailBinding> implements Observer {
    //聊天窗口对象正是此人信息
    private boolean formChat;

    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private FriendshipInfo friendshipInfo;
    //表示群成员详情  群id
    private String groupId;
    // 不允许添加组成员为好友
    private boolean applyMemberFriend;
    private boolean isInBlackList;

    private SlideButton readVanishSwitch;
    private String conversationId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        bindViewDataBinding(ActivityPersonDetailBinding.inflate(getLayoutInflater()));
        super.onCreate(savedInstanceState);
        sink();
        init();

        listener();
        click();
    }


    private void init() {
        formChat = getIntent().getBooleanExtra(Constants.K_RESULT, false);
        groupId = getIntent().getStringExtra(Constants.K_GROUP_ID);
        vm.searchContent.setValue(getIntent().getStringExtra(Constants.K_ID));

        Obs.inst().addObserver(this);
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        friendVM.setContext(this);
        friendVM.setIView(this);

        update();

        if (formChat) {
            conversationId = "single_" + vm.searchContent.getValue();
            view.readVanishLy.setVisibility(View.VISIBLE);
            readVanishSwitch = view.readVanishSwitch;
            boolean on = SharedPreferencesUtil.get(this)
                .getInteger(Constants.SP_Prefix_ReadVanish + conversationId) == 1;
            readVanishSwitch.setCheckedWithAnimation(on);
            readVanishSwitch.setOnSlideButtonClickListener(isChecked -> {
                SharedPreferencesUtil.get(this)
                    .setCache(Constants.SP_Prefix_ReadVanish + conversationId,
                        isChecked ? 1 : 0);
                BurnAfterReadingNotification n = new BurnAfterReadingNotification();
                n.recvID = vm.searchContent.getValue();
                n.sendID = BaseApp.inst().loginCertificate.userID;
                n.isPrivate = isChecked;
                n.conversationID = conversationId;
                Message msg = OpenIMClient.getInstance().messageManager
                    .createCustomMessage(GsonHel.toJson(n), "", "");
                OpenIMClient.getInstance().messageManager.sendMessage(
                    new OnMsgSendCallback() {
                        @Override
                        public void onError(int code, String error) {
                        }

                        @Override
                        public void onProgress(long progress) {
                        }

                        @Override
                        public void onSuccess(Message message) {
                        }
                    },
                    msg,
                    vm.searchContent.getValue(),
                    null,
                    new OfflinePushInfo());
                toast(getString(isChecked
                    ? io.openim.android.ouicore.R.string.start_burn_after_read
                    : io.openim.android.ouicore.R.string.stop_burn_after_read));
            });
        }
    }

    private ActivityResultLauncher<Intent> jumpCallBack =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                showWaiting();
                Common.UIHandler.postDelayed(this::getOneselfAndTargetMemberInfo, 200);
            }
        });

    /**
     * 获取自己和选择用户的MemberInfo
     */
    private void getOneselfAndTargetMemberInfo() {
        List<String> ids = new ArrayList<>();
        ids.add(BaseApp.inst().loginCertificate.userID);
        ids.add(vm.searchContent.getValue());
        getGroupMembersInfo(ids, result -> {
            if (result.isEmpty()) return;
            GroupMembersInfo oneselfGroupMembersInfo = null;
            GroupMembersInfo targetGroupMembersInfo = null;
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getUserID().equals(BaseApp.inst().loginCertificate.userID)) {
                    oneselfGroupMembersInfo = result.get(i);
                } else targetGroupMembersInfo = result.get(i);
            }
            if (oneself()) targetGroupMembersInfo = oneselfGroupMembersInfo;
            if (null == oneselfGroupMembersInfo || null == targetGroupMembersInfo) return;
            view.groupNickName.setText(targetGroupMembersInfo.getNickname());
            view.time.setText(TimeUtil.getTime(targetGroupMembersInfo.getJoinTime(),
                TimeUtil.yearMonthDayFormat));
            view.joinMethodLy.setVisibility(oneself() ? View.GONE : View.VISIBLE);

            String muteTime = "";
            if (targetGroupMembersInfo.getMuteEndTime() != 0)
                muteTime = TimeUtil.getTime(targetGroupMembersInfo.getMuteEndTime(),
                    TimeUtil.yearTimeSecondFormat);
            view.muteTime.setText(muteTime);
            if (oneself()) return;

            view.manager.setVisibility(isOwner(oneselfGroupMembersInfo) ? View.VISIBLE : View.GONE);
            if (isOwner(oneselfGroupMembersInfo)) {
                //自己是群主-显示
                view.mute.setVisibility(View.VISIBLE);
            } else {
                //自己是管理员且对方不是群主不是管理员-显示
                if (isAdmin(oneselfGroupMembersInfo)
                    && !isOwner(targetGroupMembersInfo) && !isAdmin(targetGroupMembersInfo))
                    view.mute.setVisibility(View.VISIBLE);
                else
                    view.mute.setVisibility(View.GONE);
            }

            if (targetGroupMembersInfo.getJoinSource() == 2) {
                List<String> ids2 = new ArrayList<>();
                ids2.add(targetGroupMembersInfo.getInviterUserID());
                getGroupMembersInfo(ids2,
                    result2 -> view.joinMethod.setText(result2.get(0).getNickname() + getString(io.openim.android.ouicore.R.string.inviter_join)));
            }
            if (targetGroupMembersInfo.getJoinSource() == 3) {
                view.joinMethod.setText(io.openim.android.ouicore.R.string.search_id_join);
            }
            if (targetGroupMembersInfo.getJoinSource() == 4) {
                view.joinMethod.setText(io.openim.android.ouicore.R.string.group_qr_join);
            }

            view.setManager.setCheckedWithAnimation(isAdmin(targetGroupMembersInfo));
            final String uid = targetGroupMembersInfo.getUserID();
            view.setManager.setOnSlideButtonClickListener(isChecked -> {
                SetGroupMemberInfo info = new SetGroupMemberInfo();
                info.setGroupID(groupId);
                info.setUserID(uid);
                info.setRoleLevel(isChecked ? GroupRole.ADMIN : GroupRole.MEMBER);
                OpenIMClient.getInstance().groupManager.setGroupMemberInfo(info, new IMBack<String>() {
                    @Override
                    public void onSuccess(String data) {
                        toast(getString(io.openim.android.ouicore.R.string.set_succ));
                        GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
                        if (null != groupVM) {
                            groupVM.superGroupMembers.val().clear();
                            groupVM.page = 0;
                            groupVM.getSuperGroupMemberList();
                        }
                    }
                });
            });
            view.mute.setOnClickListener(v -> {
                Easy.installVM(GroupVM.class)
                    .groupId = groupId;
                jumpCallBack.launch(new Intent(this,
                    SetMuteActivity.class).putExtra(Constants.K_ID, vm.searchContent.getValue()).putExtra("isShowMute", !TextUtils.isEmpty(view.muteTime.getText())));
            });
            cancelWaiting();
        });
    }

    private ActivityResultLauncher<Intent> personDataActivityLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            switch (result.getResultCode()) {
                case DELETE_FRIEND:
                    onSuccess(DELETE_FRIEND);
                    break;
                case SET_REMARK:
                    vm.queryRealUserInfo(vm.searchContent.val()).subscribe(new DefaultObserver<UserInfo>() {
                        @Override
                        public void onNext(UserInfo userInfo) {
                            vm.userInfo.setValue(new ArrayList<>(Collections.
                                singleton(vm.updateUserInfo(
                                    vm.userInfo.val().isEmpty() ? null:
                                        vm.userInfo.val().get(0), userInfo))));
                            setResult(SET_REMARK);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                    break;
                default:
                    break;
            }
        });

    private void click() {
        view.avatar.setOnClickListener(v -> {
            UserInfo userInfo = vm.userInfo.getValue().get(0);
            PreviewMediaVM mediaVM = Easy.installVM(PreviewMediaVM.class);
            PreviewMediaVM.MediaData mediaData =
                new PreviewMediaVM.MediaData(userInfo.getNickname());
            mediaData.mediaUrl = userInfo.getFaceURL();
            mediaVM.preview(mediaData);
            v.getContext().startActivity(
                new Intent(v.getContext(), PreviewMediaActivity.class));
        });
        view.userId.setOnClickListener(v -> {
            try {
                Common.copy(vm.userInfo.getValue().get(0).getUserID());
                toast(getString(io.openim.android.ouicore.R.string.copy_succ));
            } catch (Exception ignore) {
            }
        });
        view.userInfo.setOnClickListener(v -> {
            String uid = vm.userInfo.val().get(0)
                .getUserID();
            Easy.installVM(PersonalVM.class)
                .getUsersInfoWithCache(uid, groupId)
                .uid = uid;
            startActivity(new Intent(this, MoreDataActivity.class));
        });
        view.more.setOnClickListener(v -> {
            String uid = vm.userInfo.val().get(0)
                .getUserID();
            Easy.installVM(PersonalVM.class)
                .getUsersInfoWithCache(uid, groupId)
                .uid = uid;
            personDataActivityLauncher.launch(new Intent(this,
                PersonDataActivity.class));
        });
        view.sendMsg.setOnClickListener(v -> {
            if (!formChat) {
                ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
                if (null != chatVM) {
                    AppCompatActivity compatActivity = (AppCompatActivity) chatVM.getContext();
                    compatActivity.finish();
                    overridePendingTransition(0, 0);
                }
                runOnUiThread(() -> ARouter.getInstance().build(Routes.Conversation.CHAT)
                    .withString(Constants.K_ID, vm.searchContent.getValue())
                    .withString(Constants.K_NAME, vm.userInfo.getValue().get(0).getNickname()).navigation());
            }
            setResult(RESULT_OK);
            finish();
        });


        view.addFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, SendVerifyActivity.class).putExtra(Constants.K_ID,
                vm.searchContent.getValue()));
        });
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
        if (body instanceof Integer)
            setResult((int) body);
        finish();
    }

    boolean oneself() {
        try {
            return BaseApp.inst().loginCertificate.userID.equals(vm.searchContent.getValue());
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isHideAdd() {
        List<UserInfo> userInfoList = vm.userInfo.getValue();
        boolean notAllowed = true;
        if (null != userInfoList && !userInfoList.isEmpty()) {
            notAllowed = Objects.requireNonNullElse(userInfoList.get(0).getAllowAddFriend(), 0) == AllowType.NotAllowed.value;
        }
        boolean isFriend = vm.isFriend.val() != null ? vm.isFriend.val() : false;
        return applyMemberFriend || isFriend || oneself() || notAllowed || isInBlackList;
    }

    private void listener() {
        vm.groupsInfo.observe(this, groupInfos -> {
            if (groupInfos.isEmpty()) return;

            GroupInfo groupInfo = groupInfos.get(0);
            // 不允许查看群成员资料
            if (groupInfo.getLookMemberInfo() == 1) {
                view.userInfoLy.setVisibility(View.GONE);
                view.userId.setVisibility(View.GONE);
            } else {
                view.userId.setVisibility(View.VISIBLE);
                boolean isFriend = vm.isFriend.val() != null ? vm.isFriend.val() : false;
                if (!oneself() && isFriend) view.userInfoLy
                    .setVisibility(View.VISIBLE);
            }
            // 不允许添加组成员为好友
            applyMemberFriend = groupInfo.getApplyMemberFriend() == 1;

            view.addFriend.setVisibility(isHideAdd() ? View.GONE :
                View.VISIBLE);
            view.userId.setVisibility(applyMemberFriend ? View.GONE : View.VISIBLE);

            getOneselfAndTargetMemberInfo();
        });

        friendVM.blackListUser.observe(this, userInfos -> {
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID().equals(vm.searchContent.getValue())) {
                    isInBlackList = true;
                    break;
                }
            }
            if (null != friendshipInfo) {
                if (friendshipInfo.getResult() == 1 || isInBlackList) {
                    //是好友或在黑名单
                    view.userInfoLy.setVisibility(View.VISIBLE);
                    view.addFriend.setVisibility(View.GONE);
                    vm.isFriend.setValue(friendshipInfo.getResult() == 1);
                } else {
                    view.userInfoLy.setVisibility(View.GONE);
                    view.addFriend.setVisibility(isHideAdd()
                        ? View.GONE : View.VISIBLE);
                }
            }
            if (!TextUtils.isEmpty(groupId)) {
                vm.searchGroup(groupId);
                view.groupInfo.setVisibility(View.VISIBLE);
            }

            view.sendMsg.setVisibility(View.VISIBLE);
        });
        vm.userInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                UserInfo userInfo = v.get(0);
                String nickName = userInfo.getNickname();
                if (!TextUtils.isEmpty(userInfo.getRemark())) {
                    nickName += "(" + userInfo.getRemark() + ")";
                }
                view.nickName.setText(nickName);
                view.userId.setText(userInfo.getUserID());
                view.avatar.load(userInfo.getFaceURL(), userInfo.getNickname());
                view.bottomMenu.setVisibility(oneself() ? View.GONE : View.VISIBLE);
                view.addFriend.setVisibility(isHideAdd() ? View.GONE : View.VISIBLE);
            }
        });
        vm.friendshipInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                friendshipInfo = v.get(0);
                friendVM.getBlacklist();
            }
        });
        vm.isFriend.observe(this, v -> {
            if (v != null) {
                view.more.setVisibility(v ? View.VISIBLE : View.GONE);
                view.addFriend.setVisibility(isHideAdd() ? View.GONE : View.VISIBLE);
            }
        });
    }

    void getGroupMembersInfo(List<String> ids,
                             IMUtil.OnSuccessListener<List<GroupMembersInfo>> successListener) {
        OpenIMClient.getInstance().groupManager.getGroupMembersInfo(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                successListener.onSuccess(data);
            }
        }, groupId, ids);
    }

    private boolean isOwner(GroupMembersInfo membersInfo) {
        return membersInfo.getRoleLevel() == GroupRole.OWNER;
    }

    private boolean isAdmin(GroupMembersInfo membersInfo) {
        return membersInfo.getRoleLevel() == GroupRole.ADMIN;
    }


    @Override
    public void update(Observable o, Object arg) {
        Obs.Message message = (Obs.Message) arg;
        if (message.tag == Constants.Event.USER_INFO_UPDATE) {
            update();
        }
    }

    private void update() {
        vm.getUserData(vm.searchContent.val()).subscribe(new DisposableObserver<UserInfo>() {
            @Override
            public void onNext(UserInfo userInfo) {
                vm.userInfo.setValue(new ArrayList<>(Collections.
                    singleton(vm.updateUserInfo(
                        vm.userInfo.val().isEmpty() ? null:
                            vm.userInfo.val().get(0), userInfo))));
            }

            @Override
            public void onError(Throwable e) {
                toast(e.getMessage());
            }

            @Override
            public void onComplete() {
                cancelWaiting();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Obs.inst().deleteObserver(this);
        Easy.delete(PersonalVM.class);
    }
}
