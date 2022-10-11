package io.openim.android.ouigroup.ui;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityCreateGroupBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;

public class CreateGroupActivity extends BaseActivity<GroupVM, ActivityCreateGroupBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityCreateGroupBinding.inflate(getLayoutInflater()));
        view.setGroupVM(vm);
        sink();

        initView();

    }

    public void toBack(View view) {
        finish();
    }

    private void initView() {
        FriendInfo friendInfo=new FriendInfo();
        LoginCertificate loginCertificate=LoginCertificate.getCache(this);
        friendInfo.setUserID(loginCertificate.userID);
        friendInfo.setNickname(loginCertificate.nickname);
        vm.selectedFriendInfo.getValue().add(0,friendInfo);

        view.selectNum.setText(vm.selectedFriendInfo.getValue().size()+1 + "人");
        view.recyclerview.setLayoutManager(new GridLayoutManager(this, 5));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<FriendInfo, ImageTxtViewHolder>(ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ImageTxtViewHolder holder, FriendInfo data, int position) {
                holder.view.img.load(data.getFaceURL());
                holder.view.txt.setText(data.getNickname());
            }

        };
        view.recyclerview.setAdapter(adapter);
        adapter.setItems(vm.selectedFriendInfo.getValue());
    }

    @Override
    public void onError(String error) {
        super.onError(error);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);

        Toast.makeText(this, getString(R.string.create_succ), Toast.LENGTH_SHORT).show();
        GroupInfo groupInfo = (GroupInfo) body;
        ARouter.getInstance().build(Routes.Conversation.CHAT)
            .withString(Constant.K_GROUP_ID, groupInfo.getGroupID())
            .withString(io.openim.android.ouicore.utils.Constant.K_NAME, groupInfo.getGroupName())
            .navigation();

        setResult(RESULT_OK);
        finish();

    }
}
