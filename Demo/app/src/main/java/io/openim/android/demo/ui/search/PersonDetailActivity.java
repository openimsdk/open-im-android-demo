package io.openim.android.demo.ui.search;

import static io.openim.android.ouicore.utils.Constant.ID;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavCallback;
import com.alibaba.android.arouter.launcher.ARouter;


import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityPersonDetailBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouiconversation.utils.Constant;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Main.PERSON_DETAIL)
public class PersonDetailActivity extends BaseActivity<SearchVM, ActivityPersonDetailBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        bindViewDataBinding(ActivityPersonDetailBinding.inflate(getLayoutInflater()));
        super.onCreate(savedInstanceState);

        sink();

        listener();
        vm.searchContent = getIntent().getStringExtra(ID);
        vm.searchPerson();

        click();
    }


    private void click() {
        view.sendMsg.setOnClickListener(v -> ARouter.getInstance().build(Routes.Conversation.CHAT)
            .withString(ID, vm.searchContent)
            .withString(io.openim.android.ouicore.utils.Constant.K_NAME, vm.userInfo.getValue().get(0).getNickname())
            .navigation());


        view.addFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, SendVerifyActivity.class).putExtra(ID, vm.searchContent));
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
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
        setResult(RESULT_OK);
        finish();
    }

    private void listener() {
        vm.userInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                vm.checkFriend(v);
                UserInfo userInfo = v.get(0);
                view.nickName.setText(userInfo.getNickname());
                view.userId.setText(userInfo.getUserID());
                view.avatar.load(userInfo.getFaceURL());
            }
        });
        vm.friendshipInfo.observe(this, v -> {
            if (null != v && !v.isEmpty()) {
                FriendshipInfo friendshipInfo = v.get(0);
                if (friendshipInfo.getResult() == 1) {
                    view.addFriend.setVisibility(View.GONE);
                    view.part.setVisibility(View.VISIBLE);
                } else {
                    view.addFriend.setVisibility(View.VISIBLE);
                    view.part.setVisibility(View.GONE);
                }
            }

        });
    }

}
