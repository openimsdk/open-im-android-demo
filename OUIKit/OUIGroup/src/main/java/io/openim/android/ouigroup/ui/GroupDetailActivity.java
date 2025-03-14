package io.openim.android.ouigroup.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.JoinKickedGroupNotification;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.net.RXRetrofit.NetObserver;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouigroup.databinding.ActivityGroupDetailBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupVerification;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

@Route(path = Routes.Group.DETAIL)
public class GroupDetailActivity extends BaseActivity<GroupVM, ActivityGroupDetailBinding> {
    public static final int REQUEST_CODE = 10000;
    public static final int RESULT_CODE = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class);
        vm.groupId = getIntent().getStringExtra(Constants.K_GROUP_ID);
        vm.getGroupsInfo();
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupDetailBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.root);

        initView();
        listener();
    }

    private void listener() {
        vm.groupsInfo.observe(this, groupInfo -> {
            view.avatar.load(groupInfo.getFaceURL(),true);
            view.createDate.setText(TimeUtil.getTime(groupInfo.getCreateTime(),TimeUtil.yearMonthDayFormat));
            if (groupInfo.getNeedVerification() == GroupVerification.DIRECTLY) {
                startChat();
            } else {
                List<String> ids = new ArrayList<>();
                ids.add(BaseApp.inst().loginCertificate.userID);
                vm.getGroupMembersInfo(new OnBase<List<GroupMembersInfo>>() {
                    @Override
                    public void onError(int code, String error) {

                    }

                    @Override
                    public void onSuccess(List<GroupMembersInfo> data) {
                        if (data.isEmpty()) {
                            view.joinGroup.setOnClickListener(v -> ARouter.getInstance().build(Routes.Main.SEND_VERIFY)
                                .withString(Constants.K_ID, vm.groupId).withBoolean(Constants.K_IS_PERSON, false).navigation(GroupDetailActivity.this, REQUEST_CODE));
                        } else {
                            startChat();
                        }
                    }
                }, ids);
            }
        });
        vm.isAgreeVerify.observe(this, status -> {
            if (status)
                startChat();
        });
    }

    private void initView() {


    }

    private void startChat() {
        view.joinGroup.setText(io.openim.android.ouicore.R.string.start_chat);
        view.joinGroup.setOnClickListener(v -> {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    OpenIMClient.getInstance().groupManager.isJoinGroup(vm.groupId, new OnBase<Boolean>() {
                        @Override
                        public void onError(int code, String error) {
                            emitter.onError(new Exception(error + code));
                        }

                        @Override
                        public void onSuccess(Boolean data) {
                            emitter.onNext(data);
                        }
                    });
                }).concatMap(isJoinGroup -> Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    if (isJoinGroup) {
                        emitter.onNext(false);
                    } else  {
                        GroupInfo groupInfo = vm.groupsInfo.val();
                        if (groupInfo != null && groupInfo.getNeedVerification() == GroupVerification.DIRECTLY) {
                            emitter.onNext(true);
                        } else {
                            if (groupInfo == null)
                                emitter.onError(new Exception("The group info is a null value."));
                            else if (groupInfo.getNeedVerification() != GroupVerification.DIRECTLY)
                                emitter.onError(new Exception("The verification of group is illegal. (" + groupInfo.getNeedVerification() + ")"));
                            else
                                emitter.onError(new Exception("Unknown GroupVerification Error"));
                        }
                    }
                }))
                .subscribe(new NetObserver<Boolean>(this) {
                    @Override
                    public void onSuccess(Boolean shouldBeVerified) {
                        if (shouldBeVerified) {
                            CommonDialog dialog = new CommonDialog(GroupDetailActivity.this);
                            OpenIMClient.getInstance().groupManager.joinGroup(new OnBase<String>() {
                                @Override
                                public void onError(int code, String error) {
                                    dialog.dismiss();
                                }

                                @Override
                                public void onSuccess(String data) {
                                    showWaiting();
                                    Common.UIHandler.postDelayed(() -> {
                                        dialog.dismiss();
                                        ARouter.getInstance().build(Routes.Conversation.CHAT)
                                            .withString(Constants.K_GROUP_ID, vm.groupId)
                                            .withString(Constants.K_NAME, vm.groupsInfo.getValue().getGroupName())
                                            .navigation();
                                        cancelWaiting();
                                        finish();
                                    }, 500L);
                                }
                            }, vm.groupId, "", 2);
                        } else {
                            ARouter.getInstance().build(Routes.Conversation.CHAT)
                                .withString(Constants.K_GROUP_ID, vm.groupId)
                                .withString(Constants.K_NAME, vm.groupsInfo.getValue().getGroupName())
                                .navigation();
                            finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        toast(e.getMessage());
                    }
                });
        });
    }

    public void toBack(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_CODE) {
            if (data != null) {
                String groupIdVerify = data.getStringExtra("groupId");
                IMEvent.getInstance().addAdvanceMsgListener(new OnAdvanceMsgListener() {
                    @Override
                    public void onRecvNewMessage(Message msg) {
                        if (msg.getContentType() == MessageType.MEMBER_INVITED_NTF) {
                            JoinKickedGroupNotification joinGroupNotification = GsonHel.fromJson(msg.getNotificationElem().getDetail(), JoinKickedGroupNotification.class);
                            List<GroupMembersInfo> joinUsers = joinGroupNotification.invitedUserList;
                            for (GroupMembersInfo userInfo: joinUsers) {
                                if (userInfo.getGroupID().equals(groupIdVerify) &&
                                    userInfo.getUserID().equals(BaseApp.inst().loginCertificate.userID))
                                    vm.isAgreeVerify.setValue(true);
                            }
                        }
                    }
                });
            }
        }
    }
}
