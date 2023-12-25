package io.openim.android.demo.ui.user;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.demo.databinding.ActivityPersonInfoBinding;
import io.openim.android.demo.ui.main.EditTextActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;
import io.openim.android.sdk.listener.OnMsgSendCallback;
import io.openim.android.sdk.models.CardElem;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.OfflinePushInfo;
import io.openim.android.sdk.models.UserInfo;

public class PersonDataActivity extends BaseActivity<PersonalVM, ActivityPersonInfoBinding> {

    private ChatVM chatVM;
    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PersonalVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPersonInfoBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
        uid = getIntent().getStringExtra(Constant.K_ID);
        if (TextUtils.isEmpty(uid)) {
            chatVM = BaseApp.inst().getVMByCache(ChatVM.class);
            uid = chatVM.userID;
        }
        vm.getUserInfo(uid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) removeCacheVM();
    }

    private void init() {
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        friendVM.setContext(this);
        friendVM.setIView(this);
        friendVM.getBlacklist();
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
        final String cid = "single_" + uid;
        BaseApp.inst().getVMByCache(ContactListVM.class).deleteConversationAndDeleteAllMsg(cid);
        setResult(RESULT_OK);
        finish();
    }

    private void listener() {
        view.part.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            LayoutCommonDialogBinding mainView = commonDialog.getMainView();
            mainView.tips.setText(io.openim.android.ouicore.R.string.delete_friend_tips);
            mainView.cancel.setOnClickListener(v1 -> commonDialog.dismiss());
            mainView.confirm.setOnClickListener(v1 -> {
                commonDialog.dismiss();
                friendVM.deleteFriend(uid);
            });
        });
        view.recommend.setOnClickListener(v -> {
            SelectTargetVM selectTargetVM =
                Easy.installVM(SelectTargetVM.class);
            selectTargetVM.setIntention(SelectTargetVM.Intention.isShareCard);
            selectTargetVM.setOnFinishListener(() -> {
                CommonDialog commonDialog = new CommonDialog(this);
                commonDialog.show();
                MultipleChoice target =selectTargetVM.metaData.val().get(0);
                LayoutCommonDialogBinding mainView = commonDialog.getMainView();
                mainView.tips.setText(String.format(getString(io.openim.android.ouicore.R.string.recommend_who),
                    target.name));
                mainView.cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                mainView.confirm.setOnClickListener(v1 -> {
                    commonDialog.dismiss();

                    CardElem cardElem = new CardElem();
                    cardElem.setUserID(vm.userInfo.val().getUserID());
                    cardElem.setNickname(vm.userInfo.val().getNickname());
                    cardElem.setFaceURL(vm.userInfo.val().getFaceURL());
                    Message message = OpenIMClient.getInstance().messageManager.createCardMessage(cardElem);
                    OfflinePushInfo offlinePushInfo = new OfflinePushInfo(); // 离线推送的消息备注；不为null
                    OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
                        @Override
                        public void onError(int code, String error) {
                            toast(error + code);
                        }

                        @Override
                        public void onProgress(long progress) {
                        }

                        @Override
                        public void onSuccess(Message message) {
                            toast(PersonDataActivity.this
                                .getString(io.openim.android.ouicore.R.string.send_succ));
                        }
                    }, message,target.key,
                        null, offlinePushInfo);
                });
            });
            ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
        });
        view.moreData.setOnClickListener(v -> {
            startActivity(new Intent(this, MoreDataActivity.class));
        });
        view.remark.setOnClickListener(view -> {
            if (null == vm.userInfo.getValue()) return;
            String remark = "";
            try {
                remark = vm.userInfo.val().getFriendInfo().getRemark();
            } catch (Exception e) {}
            resultLauncher.launch(new Intent(this, EditTextActivity.class)
                .putExtra(EditTextActivity.TITLE, getString(io.openim.android.ouicore.R.string.remark))
                    .putExtra(EditTextActivity.MAX_LENGTH,16)
                .putExtra(EditTextActivity.INIT_TXT, remark));
        });
        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID().equals(uid)) {
                    isCon = true;
                    break;
                }
            }
            boolean finalIsCon = isCon;
            view.slideButton.post(() -> view.slideButton.setCheckedWithAnimation(finalIsCon));
        });
        view.joinBlackList.setOnClickListener(v -> {
            if (view.slideButton.isChecked()) friendVM.removeBlacklist(uid);
            else {
                addBlackList();
            }
        });
        view.slideButton.setOnSlideButtonClickListener(isChecked -> {
            if (isChecked) addBlackList();
            else friendVM.removeBlacklist(uid);
        });
    }

    private void addBlackList() {
        CommonDialog commonDialog = new CommonDialog(this);
        commonDialog.setCanceledOnTouchOutside(false);
        commonDialog.setCancelable(false);
        commonDialog.getMainView().tips.setText("确认对" + vm.userInfo.val().getNickname() + "拉黑吗？");
        commonDialog.getMainView().cancel.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.blackListUser.setValue(friendVM.blackListUser.getValue());
        });
        commonDialog.getMainView().confirm.setOnClickListener(v -> {
            commonDialog.dismiss();
            friendVM.addBlacklist(uid);
        });
        commonDialog.show();
    }

    private ActivityResultLauncher<Intent> resultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) return;
            String resultStr = result.getData().getStringExtra(Constant.K_RESULT);

            waitDialog.show();
            OpenIMClient.getInstance().friendshipManager.setFriendRemark(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    waitDialog.dismiss();
                    toast(error + code);
                }

                @Override
                public void onSuccess(String data) {
                    waitDialog.dismiss();
                    vm.userInfo.val().setRemark(resultStr);
                    Obs.newMessage(Constant.Event.USER_INFO_UPDATE);
                }
            }, uid, resultStr);
        });
}
