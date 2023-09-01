package io.openim.android.ouicore.vm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.databinding.LayoutSelectedFriendsBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.ForwardDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.FriendInfo;

public class MultipleChoiceVM extends BaseVM {
    public static final String SHARE_CARD = "shareCard";
    /**
     * 发起群聊
     * true 隐藏最近会话、隐藏群，只显示好友
     */
    public boolean isCreateGroup;
    /**
     * 邀请入群
     * true 显示最近会话、隐藏群，只显示好友
     */
    public boolean invite;
    /**
     * 分享名片
     * 隐藏最近会话、隐藏群、隐藏底部菜单、只显示好友、单选
     */
    public boolean isShareCard;


    public static final String NOTIFY_ITEM_REMOVED = "notify_item_removed";
    public State<List<MultipleChoice>> metaData = new State<>(new ArrayList<>());

    public void shareCard() {
        Postcard postcard = ARouter.getInstance().build(Routes.Main.HOME);
        Postcard postcard2 = ARouter.getInstance().build(Routes.Conversation.CHAT);
        LogisticsCenter.completion(postcard);
        LogisticsCenter.completion(postcard2);
        ActivityManager.finishAllExceptActivity(postcard.getDestination(),
            postcard2.getDestination());

        postSubject(SHARE_CARD);
    }

    public boolean contains(MultipleChoice data) {
        return metaData.val().contains(data);
    }

    public void addMetaData(String id, String name, String faceUrl) {
        MultipleChoice data = new MultipleChoice();
        data.key = id;
        data.name = name;
        data.icon = faceUrl;
        if (!metaData.getValue().contains(data)) {
            metaData.getValue().add(data);
            metaData.update();
        }
    }

    public void removeMetaData(String id) {
        MultipleChoice data = new MultipleChoice();
        data.key = id;
        if (metaData.getValue().contains(data)) {
            metaData.getValue().remove(data);
            metaData.update();
        }
    }

    @SuppressLint("SetTextI18n")
    public void bindDataToView(LayoutSelectedFriendsBinding view) {
        Context context = view.getRoot().getContext();
        metaData.observe((LifecycleOwner) context, userInfos -> {
            if (!userInfos.isEmpty()) {
                view.more2.setVisibility(View.VISIBLE);
                view.content.setVisibility(View.VISIBLE);
            } else {
                view.content.setVisibility(View.GONE);
                view.more2.setVisibility(View.GONE);
            }
            view.selectNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips2), userInfos.size()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < userInfos.size(); i++) {
                builder.append(userInfos.get(i).name);
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
        RecyclerViewAdapter<MultipleChoice, ViewHol.ImageTxtRightViewHolder> adapter;
        view2.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<MultipleChoice,
            ViewHol.ImageTxtRightViewHolder>(ViewHol.ImageTxtRightViewHolder.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ImageTxtRightViewHolder holder,
                                   MultipleChoice data, int position) {
                if (data.isGroup) holder.view.img.load(data.icon, true);
                else holder.view.img.load(data.icon, data.name);
                holder.view.txt.setText(data.name);
                holder.view.right.setOnClickListener(v -> {
                    removeMetaData(data.key);
                    notifyDataSetChanged();
                    subject(NOTIFY_ITEM_REMOVED);
                });
            }
        });
        adapter.setItems(metaData.getValue());
        metaData.observe((LifecycleOwner) context, userInfos -> {
            view2.selectedNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips2), userInfos.size()));
        });
        view.selectLy.setOnClickListener(v -> {
            bottomPopDialog.show();
        });
    }

    public void submitTap(Button button) {
        button.setOnClickListener(v -> {
            if (isCreateGroup) {
                GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
                if (null == groupVM) groupVM = new GroupVM();
                groupVM.selectedFriendInfo.getValue().clear();
                List<MultipleChoice> multipleChoices = this.metaData.getValue();
                for (int i = 0; i < multipleChoices.size(); i++) {
                    MultipleChoice us = multipleChoices.get(i);
                    FriendInfo friendInfo = new FriendInfo();
                    friendInfo.setUserID(us.key);
                    friendInfo.setNickname(us.name);
                    friendInfo.setFaceURL(us.icon);
                    groupVM.selectedFriendInfo.getValue().add(friendInfo);
                }
                BaseApp.inst().putVM(groupVM);
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP2).navigation();
                return;
            }

            showConfirmDialog(v.getContext());
        });
    }

    private void showConfirmDialog(Context context) {
        ForwardDialog dialog = new ForwardDialog(context);
        dialog.show();
    }
}
