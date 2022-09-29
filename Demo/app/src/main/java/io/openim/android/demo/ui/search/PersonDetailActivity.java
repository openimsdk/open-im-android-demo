package io.openim.android.demo.ui.search;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;


import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.databinding.ActivityPersonDetailBinding;
import io.openim.android.demo.ui.user.PersonDataActivity;
import io.openim.android.demo.vm.FriendVM;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Main.PERSON_DETAIL)
public class PersonDetailActivity extends BaseActivity<SearchVM, ActivityPersonDetailBinding> {
    private boolean formChat;
    private FriendVM friendVM = new FriendVM();
    private WaitDialog waitDialog;
    private FriendshipInfo friendshipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        bindViewDataBinding(ActivityPersonDetailBinding.inflate(getLayoutInflater()));
        super.onCreate(savedInstanceState);

        sink();
        init();

        listener();
        vm.searchContent = getIntent().getStringExtra(Constant.K_ID);
        vm.searchPerson();

        formChat = getIntent().getBooleanExtra(Constant.K_RESULT, false);
        click();
    }

    private void init() {
        waitDialog = new WaitDialog(this);
        friendVM.waitDialog = waitDialog;
        friendVM.setContext(this);
        friendVM.setIView(this);
        waitDialog.show();
    }


    private void click() {
        view.userInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, PersonDataActivity.class));
        });
        view.sendMsg.setOnClickListener(v -> {
                if (formChat) {
                    finish();
                } else {
                    ARouter.getInstance().build(Routes.Conversation.CHAT)
                        .withString(Constant.K_ID, vm.searchContent)
                        .withString(Constant.K_NAME, vm.userInfo.getValue().get(0).getNickname())
                        .navigation();
                }
            }
        );


        view.addFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, SendVerifyActivity.class).putExtra(Constant.K_ID, vm.searchContent));
        });
        view.part.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(this);
            commonDialog.show();
            LayoutCommonDialogBinding mainView = commonDialog.getMainView();
            mainView.tips.setText(io.openim.android.ouicore.R.string.delete_friend_tips);
            mainView.cancel.setOnClickListener(v1 -> commonDialog.dismiss());
            mainView.confirm.setOnClickListener(v1 -> {
                commonDialog.dismiss();
                vm.deleteFriend(vm.searchContent);
            });
        });

        view.call.setOnClickListener(v -> {
            if (null == callingService) return;
            IMUtil.showBottomPopMenu(this, (v1, keyCode, event) -> {
                List<String> ids = new ArrayList<>();
                ids.add(vm.searchContent);
                SignalingInfo signalingInfo = IMUtil.buildSignalingInfo(keyCode != 1, true,
                    ids, null);
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

    private void listener() {
        friendVM.blackListUser.observe(this, userInfos -> {
            boolean isCon = false;
            for (UserInfo userInfo : userInfos) {
                if (userInfo.getUserID()
                    .equals(vm.searchContent)) {
                    isCon = true;
                    break;
                }
            }
            if (null!=friendshipInfo){
                if (friendshipInfo.getResult() == 1 || isCon) {
                    view.userInfo.setVisibility(formChat ? View.VISIBLE : View.GONE);
                    view.addFriend.setVisibility(View.GONE);
                    view.part.setVisibility(View.VISIBLE);
                } else {
                    view.addFriend.setVisibility(View.VISIBLE);
                    view.part.setVisibility(View.GONE);
                }
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
            }
        });
        vm.friendshipInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                friendshipInfo = v.get(0);
                friendVM.getBlacklist();
            }

        });
    }

}
