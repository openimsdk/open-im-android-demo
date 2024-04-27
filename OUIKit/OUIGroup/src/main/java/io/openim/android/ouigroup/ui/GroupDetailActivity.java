package io.openim.android.ouigroup.ui;


import android.os.Bundle;
import android.view.View;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouigroup.databinding.ActivityGroupDetailBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.GroupVerification;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.DETAIL)
public class GroupDetailActivity extends BaseActivity<GroupVM, ActivityGroupDetailBinding> {


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
                                .withString(Constants.K_ID, vm.groupId).withBoolean(Constants.K_IS_PERSON, false).navigation());
                        } else {
                            startChat();
                        }
                    }
                }, ids);
            }

        });
    }

    private void initView() {


    }

    private void startChat() {
        view.joinGroup.setText(io.openim.android.ouicore.R.string.start_chat);
        view.joinGroup.setOnClickListener(v -> {
            if (vm.groupsInfo.getValue().getNeedVerification() == GroupVerification.DIRECTLY) {
                CommonDialog dialog = new CommonDialog(this);
                OpenIMClient.getInstance().groupManager.joinGroup(new OnBase<String>() {
                    @Override
                    public void onError(int code, String error) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String data) {
                        dialog.dismiss();
                        ARouter.getInstance().build(Routes.Conversation.CHAT)
                            .withString(Constants.K_GROUP_ID, vm.groupId)
                            .withString(Constants.K_NAME, vm.groupsInfo.getValue().getGroupName())
                            .navigation();
                    }
                }, vm.groupId, "", 2);
            } else
                ARouter.getInstance().build(Routes.Conversation.CHAT)
                    .withString(Constants.K_GROUP_ID, vm.groupId)
                    .withString(Constants.K_NAME, vm.groupsInfo.getValue().getGroupName())
                    .navigation();
        });
    }

    public void toBack(View v) {
        finish();
    }
}
