package io.openim.android.demo.ui.search;

import static io.openim.android.ouicontact.utils.Constant.K_USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityPersonDetailBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicontact.utils.Constant;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.models.FriendshipInfo;
import io.openim.android.sdk.models.UserInfo;

public class PersonDetailActivity extends BaseActivity<SearchVM> {
    private ActivityPersonDetailBinding view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = ActivityPersonDetailBinding.inflate(getLayoutInflater());
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        bindVM(SearchVM.class);
        setContentView(view.getRoot());


        super.onCreate(savedInstanceState);

        listener();
        vm.searchContent = getIntent().getStringExtra(K_USER_ID);
        vm.search();

        click();
    }

    private void click() {
        view.sendMsg.setOnClickListener(v -> ARouter.getInstance().build(Routes.Contact.CHAT)
                .withString(Constant.K_USER_ID, vm.searchContent)
                .withString(Constant.K_NAME, vm.userInfo.getValue().get(0).getNickname())
                .navigation());


        view.addFriend.setOnClickListener(v -> {
            startActivity(new Intent(this, SendVerifyActivity.class).putExtra(K_USER_ID, vm.searchContent));
        });
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