package io.openim.android.ouigroup.ui;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.Map;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.ImageTxtViewHolder;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityCreateGroupBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.PutArgs;

@Route(path = Routes.Group.CREATE_GROUP2)
public class CreateGroupActivity extends BaseActivity<GroupVM, ActivityCreateGroupBinding> {

    private PhotographAlbumDialog albumDialog;
    private String faceURL;

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
        FriendInfo friendInfo = new FriendInfo();
        LoginCertificate loginCertificate = LoginCertificate.getCache(this);
        friendInfo.setUserID(loginCertificate.userID);
        friendInfo.setNickname(loginCertificate.nickname);
        if (!vm.selectedFriendInfo.getValue().get(0)
            .getUserID().equals(friendInfo.getUserID())) {
            vm.selectedFriendInfo.getValue().add(0, friendInfo);
        }
        view.selectNum.setText(vm.selectedFriendInfo.getValue().size() + "人");
        view.recyclerview.setLayoutManager(new GridLayoutManager(this, 5));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter<FriendInfo, ImageTxtViewHolder>(ImageTxtViewHolder.class) {

            @Override
            public void onBindView(@NonNull ImageTxtViewHolder holder, FriendInfo data, int position) {
                holder.view.img.load(data.getFaceURL(),data.getNickname());
                holder.view.txt.setText(data.getNickname());
            }
        };
        view.recyclerview.setAdapter(adapter);
        adapter.setItems(vm.selectedFriendInfo.getValue());

        view.submit.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                vm.createGroup(faceURL);
            }
        });

        albumDialog = new PhotographAlbumDialog(this);
        albumDialog.setOnSelectResultListener(path -> {
            PutArgs putArgs = new PutArgs(path[0]);
            OpenIMClient.getInstance().uploadFile(new OnFileUploadProgressListener() {
                @Override
                public void onError(int code, String error) {

                }

                @Override
                public void onProgress(long progress) {

                }

                @Override
                public void onSuccess(String s) {
                    try {
                        Map<String, String> fromJson = GsonHel.fromJson(s, Map.class);
                        faceURL =fromJson.get("url");
                        view.avatarEdit.load(faceURL);
                    } catch (Exception ignored) {}
                }
            }, null, putArgs);
        });

        view.avatarEdit.setOnClickListener(v -> {
            albumDialog.show();
        });
        view.avatarEdit.setResId(R.mipmap.ic_g_init_group_icon);
        view.avatarEdit.load(null);
    }

    @Override
    public void onError(String error) {
        super.onError(error);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);

        BaseApp.inst().removeCacheVM(GroupVM.class);
        Toast.makeText(this, getString(io.openim.android.ouicore.R.string.create_succ), Toast.LENGTH_SHORT).show();
        GroupInfo groupInfo = (GroupInfo) body;

        Postcard postcard1=ARouter.getInstance().build(Routes.Conversation.CHAT);
        postcard1.withString(Constant.K_GROUP_ID, groupInfo.getGroupID())
            .withString(io.openim.android.ouicore.utils.Constant.K_NAME,
                groupInfo.getGroupName()).navigation();

        setResult(RESULT_OK);

        Postcard postcard2 =ARouter.getInstance().build(Routes.Main.HOME);
        LogisticsCenter.completion(postcard1);
        LogisticsCenter.completion(postcard2);

        ActivityManager.finishAllExceptActivity(postcard1.getDestination(),postcard2.getDestination());
    }


}
