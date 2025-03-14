package io.openim.android.demo.ui.user;

import static io.openim.android.ouicore.utils.Constants.ActivityResult.SET_REMARK;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.openim.android.demo.databinding.ActivityPersonInfoBinding;
import io.openim.android.demo.ui.main.EditTextActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
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
import io.openim.android.sdk.models.UpdateFriendsReq;
import io.openim.android.sdk.models.UserInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

public class PersonDataActivity extends BaseActivity<PersonalVM, ActivityPersonInfoBinding> {

    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private String currRemark = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vm = Easy.find(PersonalVM.class);
        vm.setContext(this);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPersonInfoBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();

        vm.queryFriendInfoWithoutBlocked(vm.uid).subscribe(new DisposableObserver<UserInfo>() {
            @Override
            public void onNext(UserInfo userInfo) {
                currRemark = userInfo.getRemark();
            }

            @Override
            public void onError(Throwable e) {
                toast(e.getMessage());
            }

            @Override
            public void onComplete() {
                vm.getUserInfo(vm.uid);
            }
        });
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
        final String cid = "single_" + vm.uid;
        BaseApp.inst().getVMByCache(ContactListVM.class).deleteConversationAndDeleteAllMsg(cid);
        if (body instanceof Integer)
            setResult((int) body);
        else
            setResult(RESULT_OK);
        finish();
    }

    private Single<List<String>> sendCardMessage(Message cardMsg, List<MultipleChoice> receiveInfo) {
        return Observable.fromIterable(receiveInfo).flatMap((Function<MultipleChoice, ObservableSource<String>>) multipleChoice ->
            Observable.create(emitter -> {
                try {
                    OpenIMClient.getInstance().messageManager.sendMessage(new OnMsgSendCallback() {
                        @Override
                        public void onError(int code, String error) {
                            emitter.onError(new Exception(error + code));
                        }

                        @Override
                        public void onProgress(long progress) {

                        }

                        @Override
                        public void onSuccess(Message s) {
                            emitter.onNext(s.getRecvID());
                        }
                    }, cardMsg, multipleChoice.key, null, new OfflinePushInfo());
                } catch (Exception e) {
                    emitter.onError(e);
                }
            })).toList();
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
                friendVM.deleteFriend(vm.uid);
            });
        });

        view.remark.setOnClickListener(view -> {
            resultLauncher.launch(new Intent(this, EditTextActivity.class)
                .putExtra(EditTextActivity.TITLE,
                    getString(io.openim.android.ouicore.R.string.remark))
                .putExtra(EditTextActivity.MAX_LENGTH, 16)
                .putExtra(EditTextActivity.INIT_TXT, currRemark));
        });
        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID().equals(vm.uid)) {
                    isCon = true;
                    break;
                }
            }
            boolean finalIsCon = isCon;
            view.slideButton.post(() -> view.slideButton.setCheckedWithAnimation(finalIsCon));
        });
        view.joinBlackList.setOnClickListener(v -> {
            if (view.slideButton.isChecked()) friendVM.removeBlacklist(vm.uid);
            else {
                addBlackList();
            }
        });
        view.slideButton.setOnSlideButtonClickListener(isChecked -> {
            if (isChecked) addBlackList();
            else friendVM.removeBlacklist(vm.uid);
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
            friendVM.addBlacklist(vm.uid);
        });
        commonDialog.show();
    }

    private ActivityResultLauncher<Intent> resultLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK) return;
            String resultStr = result.getData().getStringExtra(Constants.K_RESULT);

            showWaiting();
            UpdateFriendsReq updateFriendsReq = new UpdateFriendsReq();
            updateFriendsReq.setFriendUserIDs(new String[]{vm.uid});
            updateFriendsReq.setRemark(resultStr);
            OpenIMClient.getInstance().friendshipManager.updateFriendsReq(new OnBase<String>() {
                @Override
                public void onError(int code, String error) {
                    toast(code+error);
                }

                @Override
                public void onSuccess(String data) {
                    setResult(SET_REMARK);
                    cancelWaiting();
                }
            }, updateFriendsReq);
        });

    @Override
    protected void fasterDestroy() {
        super.fasterDestroy();
        Easy.delete(PersonalVM.class);
    }
}
