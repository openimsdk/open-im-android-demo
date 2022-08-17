package io.openim.android.ouigroup.ui;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupDetailBinding;
import io.openim.android.ouigroup.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Group.DETAIL)
public class GroupDetailActivity extends BaseActivity<GroupVM, ActivityGroupDetailBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class);
        vm.groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        vm.getGroupsInfo();
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupDetailBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.root);

        initView();
    }

    private void initView() {

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
                        .withString(Constant.K_ID, vm.groupId).withBoolean(Constant.K_IS_PERSON, false).navigation());
                } else {
                    view.joinGroup.setText(io.openim.android.ouicore.R.string.start_chat);
                    view.joinGroup.setOnClickListener(v -> {
                        ARouter.getInstance().build(Routes.Conversation.CHAT)
                            .withString(Constant.K_GROUP_ID, vm.groupId)
                            .withString(io.openim.android.ouicore.utils.Constant.K_NAME, vm.groupsInfo.getValue().getGroupName())
                            .navigation();
                    });
                }
            }
        }, ids);
    }

    public void toBack(View v) {
        finish();
    }
}
