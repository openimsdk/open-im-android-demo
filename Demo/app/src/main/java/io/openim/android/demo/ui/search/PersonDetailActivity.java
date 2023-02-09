package io.openim.android.demo.ui.search;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;


import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.demo.databinding.ActivityPersonDetailBinding;
import io.openim.android.demo.ui.user.PersonDataActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Main.PERSON_DETAIL)
public class PersonDetailActivity extends BaseActivity<SearchVM, ActivityPersonDetailBinding> implements Observer {
    //聊天窗口对象正是此人信息
    private boolean formChat;

    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private FriendshipInfo friendshipInfo;
    //表示群成员详情  群id
    private String groupId;
    // 不允许查看群成员资料
    private boolean notLookMemberInfo;
    // 不允许添加组成员为好友
    private boolean applyMemberFriend;
    //已经是好友
    private boolean isFriend;

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
        formChat = getIntent().getBooleanExtra(Constant.K_RESULT, false);
        groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        vm.searchContent.setValue(getIntent().getStringExtra(Constant.K_ID));

        Obs.inst().addObserver(this);
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        waitDialog.setNotDismiss();
        friendVM.setContext(this);
        friendVM.setIView(this);
        waitDialog.show();

        vm.searchPerson();
    }

    private ActivityResultLauncher<Intent> personDataActivityLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) onSuccess(null);
    });

    private void click() {
        view.copy.setOnClickListener(v -> {
            Common.copy(vm.userInfo.getValue().get(0).getUserID());
            toast(getString(io.openim.android.ouicore.R.string.copy_succ));
        });
        view.userInfo.setOnClickListener(v -> {
            personDataActivityLauncher.launch(new Intent(this, PersonDataActivity.class).putExtra(Constant.K_ID, vm.userInfo.getValue().get(0).getUserID()));
        });
        view.sendMsg.setOnClickListener(v -> {
            if (!formChat) {
                ChatVM chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
                if (null != chatVM) {
                    AppCompatActivity compatActivity = (AppCompatActivity) chatVM.getContext();
                    compatActivity.finish();
                    overridePendingTransition(0, 0);
                }
                runOnUiThread(() -> ARouter.getInstance().build(Routes.Conversation.CHAT).withString(Constant.K_ID, vm.searchContent.getValue()).withString(Constant.K_NAME, vm.userInfo.getValue().get(0).getNickname()).navigation());
            }
            setResult(RESULT_OK);
            finish();
        });


        view.addFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, SendVerifyActivity.class).putExtra(Constant.K_ID,
                vm.searchContent.getValue()));
        });


        view.call.setOnClickListener(v -> {
            if (null == callingService) return;
            IMUtil.showBottomPopMenu(this, (v1, keyCode, event) -> {
                List<String> ids = new ArrayList<>();
                ids.add(vm.searchContent.getValue());
                SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(keyCode != 1, true, ids,
                    null);
                callingService.call(signalingInfo);
                return false;
            });
        });
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
        setResult(RESULT_OK);
        finish();
    }

    boolean oneself() {
        try {
            return vm.userInfo.getValue().get(0).getUserID().equals(BaseApp.inst().loginCertificate.userID);
        } catch (Exception ignored) {
        }
        return false;
    }

    private void listener() {
        vm.groupsInfo.observe(this, groupInfos -> {
            if (groupInfos.isEmpty()) return;
            GroupInfo groupInfo = groupInfos.get(0);
            // 不允许查看群成员资料
            if (notLookMemberInfo = groupInfo.getLookMemberInfo() == 1) {
                view.userInfo.setVisibility(View.GONE);
                view.idLy.setVisibility(View.GONE);
            } else {
                view.idLy.setVisibility(View.VISIBLE);
                if (!oneself() && isFriend) view.userInfo.setVisibility(View.VISIBLE);
            }
            // 不允许添加组成员为好友
            applyMemberFriend =
                groupInfo.getApplyMemberFriend() == 1;
            view.addFriend.setVisibility((applyMemberFriend || isFriend) ? View.GONE : View.VISIBLE);
            view.idLy.setVisibility(applyMemberFriend ? View.GONE : View.VISIBLE);
        });

        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID().equals(vm.searchContent.getValue())) {
                    isCon = true;
                    break;
                }
            }
            if (null != friendshipInfo) {
                if (friendshipInfo.getResult() == 1 || isCon) {
                    view.userInfo.setVisibility(View.VISIBLE);
                    view.addFriend.setVisibility(View.GONE);
                    isFriend = true;
                } else {
                    view.userInfo.setVisibility(View.GONE);
                    view.addFriend.setVisibility(View.VISIBLE);
                }
            }
            if (!TextUtils.isEmpty(groupId)) {
                vm.searchGroup(groupId);
                view.groupInfo.setVisibility(View.VISIBLE);
                getGroupMembersInfo();
            }
        });
        vm.userInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                vm.checkFriend(v);

                UserInfo userInfo = v.get(0);
                String nickName = userInfo.getNickname();
                if (!TextUtils.isEmpty(userInfo.getRemark())) {
                    nickName += "(" + userInfo.getRemark() + ")";
                }
                view.nickName.setText(nickName);
                view.userId.setText(userInfo.getUserID());
                view.avatar.load(userInfo.getFaceURL());
                if (oneself()) view.bottomMenu.setVisibility(View.GONE);
            }
        });
        vm.friendshipInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                friendshipInfo = v.get(0);
                friendVM.getBlacklist();
            }
        });
    }

    private void getGroupMembersInfo() {
        List<String> ids = new ArrayList<>();
        ids.add(vm.searchContent.getValue());
        OpenIMClient.getInstance().groupManager.getGroupMembersInfo(new OnBase<List<GroupMembersInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                GroupMembersInfo groupMembersInfo = data.get(0);
                view.groupNickName.setText(groupMembersInfo.getNickname());
                view.time.setText(TimeUtil.getTime(groupMembersInfo.getJoinTime() * 1000,
                    TimeUtil.yearMonthDayFormat));
            }
        }, groupId, ids);
    }

    @Override
    public void update(Observable o, Object arg) {
        Obs.Message message = (Obs.Message) arg;
        if (message.tag == Constant.Event.USER_INFO_UPDATE) {
            vm.searchPerson();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Obs.inst().deleteObserver(this);
    }
}
