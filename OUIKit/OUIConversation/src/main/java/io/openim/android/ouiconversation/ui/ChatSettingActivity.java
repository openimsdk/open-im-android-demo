package io.openim.android.ouiconversation.ui;


import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.databinding.ActivityChatSettingBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.UserInfo;

public class ChatSettingActivity extends BaseActivity<ChatVM, ActivityChatSettingBinding> implements ChatVM.ViewAction {

    ContactListVM contactListVM = new ContactListVM();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(ChatVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityChatSettingBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        click();
    }

    private ActivityResultLauncher<Intent> personDetailLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK)
            finish();
    });

    private void click() {
        view.addChat.setOnClickListener(v -> {
            BottomPopDialog dialog = new BottomPopDialog(this);
            dialog.show();
            dialog.getMainView().menu3.setOnClickListener(v1 -> dialog.dismiss());
            dialog.getMainView().menu1.setText(io.openim.android.ouicore.R.string.general_group);
            dialog.getMainView().menu2.setText(io.openim.android.ouicore.R.string.work_group);

            dialog.getMainView().menu1.setOnClickListener(v1 -> {
                dialog.dismiss();
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP)
                    .withString(Constant.K_ID,vm.userID)
                    .navigation();
            });
            dialog.getMainView().menu2.setOnClickListener(v1 -> {
                dialog.dismiss();
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP)
                    .withString(Constant.K_ID,vm.userID)
                    .withBoolean(Constant.K_RESULT, true)
                    .navigation();
            });
        });
        view.picture.setOnClickListener(v -> {
            startActivity(new Intent(this,
                MediaHistoryActivity.class).putExtra(Constant.K_RESULT, true));
        });
        view.video.setOnClickListener(v -> {
            startActivity(new Intent(this,
                MediaHistoryActivity.class));
        });
        view.file.setOnClickListener(v -> startActivity(new Intent(this,
            FileHistoryActivity.class)));

        view.readVanish.setOnSlideButtonClickListener(isChecked -> {
            OpenIMClient.getInstance().conversationManager
                .setOneConversationPrivateChat(new OnBase<String>() {
                                                   @Override
                                                   public void onError(int code, String error) {
                                                       toast(error + code);
                                                       view.readVanish.setCheckedWithAnimation(!isChecked);
                                                   }

                                                   @Override
                                                   public void onSuccess(String data) {
                                                       view.readVanish.setCheckedWithAnimation(isChecked);
                                                   }
                                               },
                    vm.conversationInfo.getValue().getConversationID(), isChecked);
        });
        view.topSlideButton.setOnSlideButtonClickListener(is -> {
            contactListVM.pinConversation(vm.conversationInfo.getValue(), is);
        });
        view.searchChat.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatHistorySearchActivity.class));
        });
        view.chatbg.setOnClickListener(view1 -> {
            startActivity(new Intent(this, SetChatBgActivity.class));
        });

        view.noDisturb.setOnSlideButtonClickListener(is -> {
            vm.setConversationRecvMessageOpt(is ? 2 : 0, vm.conversationInfo.getValue().getConversationID());
        });
        view.user.setOnClickListener(v -> {
            Postcard postcard = ARouter.getInstance().build(Routes.Main.PERSON_DETAIL);
            LogisticsCenter.completion(postcard);
            personDetailLauncher.launch(new Intent(this, postcard.getDestination())
                .putExtra(Constant.K_ID, vm.userID).putExtra(Constant.K_RESULT, true));
        });
        view.clearRecord.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            commonDialog.getMainView().tips.setText(io.openim.android.ouicore.R.string.clear_chat_tips);
            commonDialog.getMainView().cancel.setOnClickListener(view1 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(view1 -> {
                commonDialog.dismiss();
                vm.clearCHistory(vm.userID);
            });
        });
    }

    private void initView() {
        view.readVanish.setCheckedWithAnimation(vm.conversationInfo.getValue().isPrivateChat());

        vm.notDisturbStatus.observe(this, integer -> {
            view.noDisturb.post(() -> view.noDisturb.setCheckedWithAnimation(integer == 2));
        });
        vm.conversationInfo.observe(this, conversationInfo -> {
            view.topSlideButton.post(() -> view.topSlideButton.setCheckedWithAnimation(conversationInfo.isPinned()));
        });

        List<String> uid = new ArrayList<>();
        uid.add(vm.userID);
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
