package io.openim.android.ouicore.vm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.databinding.LayoutSelectedFriendsBinding;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.ForwardDialog;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.UserInfo;

public class SelectFriendsVM extends BaseVM {

    public static final String NOTIFY_ITEM_REMOVED = "notify_item_removed";
    public State<List<UserInfo>> userInfoList = new State<>(new ArrayList<>());


    public boolean contains(UserInfo userInfo) {
        return userInfoList.getValue().contains(userInfo);
    }

    public void addUserInfo(String id, String name, String faceUrl) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(id);
        userInfo.setNickname(name);
        userInfo.setFaceURL(faceUrl);
        if (!userInfoList.getValue().contains(userInfo)) {
            userInfoList.getValue().add(userInfo);
            userInfoList.update();
        }
    }

    public void removeUserInfo(String id) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(id);
        if (userInfoList.getValue().contains(userInfo)) {
            userInfoList.getValue().remove(userInfo);
            userInfoList.update();
        }
    }

    @SuppressLint("SetTextI18n")
    public void bindDataToView(LayoutSelectedFriendsBinding view) {
        Context context = view.getRoot().getContext();
        userInfoList.observe((LifecycleOwner) context, userInfos -> {
            if (!userInfos.isEmpty()) {
                view.more2.setVisibility(View.VISIBLE);
                view.content.setVisibility(View.VISIBLE);
            } else {
                view.content.setVisibility(View.GONE);
                view.more2.setVisibility(View.GONE);
            }
            view.selectNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips), userInfos.size()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < userInfos.size(); i++) {
                builder.append(userInfos.get(i).getNickname());
                if (i != userInfos.size() - 1) builder.append("、");
            }
            view.content.setText(builder.toString());
            view.submit.setEnabled(userInfos.size() > 0);
            view.submit.setText(context.getString(io.openim.android.ouicore.R.string.sure) + "（" + userInfos.size() + "/999）");
        });
    }

    public void showPopAllSelectFriends(LayoutSelectedFriendsBinding view,
                                        LayoutPopSelectedFriendsBinding view2) {
        Context context = view.getRoot().getContext();
        BottomPopDialog bottomPopDialog = new BottomPopDialog(context, view2.getRoot());
        view2.sure.setOnClickListener(v -> bottomPopDialog.dismiss());
        view2.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        RecyclerViewAdapter<UserInfo, ViewHol.ImageTxtRightViewHolder> adapter;
        view2.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<UserInfo,
            ViewHol.ImageTxtRightViewHolder>(ViewHol.ImageTxtRightViewHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ImageTxtRightViewHolder holder, UserInfo data
                , int position) {
                holder.view.img.load(data.getFaceURL(), data.getNickname());
                holder.view.txt.setText(data.getNickname());
                holder.view.right.setOnClickListener(v -> {
                    removeUserInfo(data.getUserID());
                    notifyDataSetChanged();
                    subject(NOTIFY_ITEM_REMOVED);
                });
            }
        });
        adapter.setItems(userInfoList.getValue());
        userInfoList.observe((LifecycleOwner) context, userInfos -> {
            view2.selectedNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips), userInfos.size()));
        });
        view.selectLy.setOnClickListener(v -> {
            bottomPopDialog.show();
        });
    }

    public void submitTap(Button button) {
        button.setOnClickListener(v -> {
            try {
                Easy.find(ForwardVM.class);
                //转发
                ForwardDialog forwardDialog = new ForwardDialog(v.getContext());
                forwardDialog.show();
                return;
            } catch (Exception ignore) {}

            //发起群聊
            GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
            if (null == groupVM)
                groupVM = new GroupVM();
            groupVM.selectedFriendInfo.getValue().clear();
            List<UserInfo> userInfoList = this.userInfoList.getValue();
            for (int i = 0; i < userInfoList.size(); i++) {
                UserInfo us = userInfoList.get(i);
                FriendInfo friendInfo = new FriendInfo();
                friendInfo.setUserID(us.getUserID());
                friendInfo.setNickname(us.getNickname());
                friendInfo.setFaceURL(us.getFaceURL());
                groupVM.selectedFriendInfo.getValue().add(friendInfo);
            }
            BaseApp.inst().putVM(groupVM);
            ARouter.getInstance().build(Routes.Group.CREATE_GROUP2).navigation();
        });

    }
}
