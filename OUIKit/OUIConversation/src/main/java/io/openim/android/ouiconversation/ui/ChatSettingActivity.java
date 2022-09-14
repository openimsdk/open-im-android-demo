package io.openim.android.ouiconversation.ui;


import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityChatSettingBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.UserInfo;

public class ChatSettingActivity extends BaseActivity<ChatVM, ActivityChatSettingBinding>implements ChatVM.ViewAction {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityChatSettingBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        click();
    }

    private void click() {
        view.user.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                .withString(Constant.K_ID, vm.otherSideID)
                .withBoolean(Constant.K_RESULT, true)
                .navigation();
        });
        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_tips);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                vm.clearC2CHistory(vm.otherSideID);
            });
        });
    }

    private void initView() {
        List<String> uid = new ArrayList<>();
        uid.add(vm.otherSideID);
        OpenIMClient.getInstance().userInfoManager.getUsersInfo(new OnBase<List<UserInfo>>() {
            @Override
            public void onError(int code, String error) {
                toast(error + code);
            }

            @Override
            public void onSuccess(List<UserInfo> data) {
                if (data.isEmpty()) return;
                view.avatar.load(data.get(0).getFaceURL());
                view.userName.setText(data.get(0).getNickname());
            }
        }, uid);
    }

    @Override
    public void scrollToPosition(int position) {

    }

    @Override
    public void closePage() {

    }
}
