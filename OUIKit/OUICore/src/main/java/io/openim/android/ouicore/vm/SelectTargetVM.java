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
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.injection.BaseVM;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.databinding.LayoutSelectedFriendsBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.ForwardDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.GroupMembersInfo;

public class SelectTargetVM extends BaseVM {
    public static final String NOTIFY_ITEM_REMOVED = "notify_item_removed";

    public enum Intention {
        /**
         * 转发-默认
         * 显示最近会话、显示群，显示好友、多选
         */
        Forward,
        /**
         * 发起群聊
         * 显示最近会话、隐藏群，只显示好友、多选
         */
        isCreateGroup,
        /**
         * 邀请入群
         * 显示最近会话、隐藏群，只显示好友、新增inviteList用于底部显示、多选
         */
        invite,
        /**
         * 分享名片
         * 隐藏最近会话、隐藏群、隐藏底部菜单、只显示好友、单选
         */
        isShareCard,

        /**
         * 选择好友
         * 隐藏最近会话、隐藏群、显示底部菜单、显示好友、多选
         */
        selectFriends
    }

    private OnFinishListener onFinishListener;
    private Intention intention = Intention.Forward;

    public SelectTargetVM setIntention(Intention intention) {
        this.intention = intention;
        return this;
    }

    public boolean isShareCard() {
        return intention == Intention.isShareCard;
    }

    public boolean isInvite() {
        return intention == Intention.invite;
    }

    public boolean isSelectFriends() {
        return intention == Intention.selectFriends;
    }

    public boolean isCreateGroup() {
        return intention == Intention.isCreateGroup;
    }

    public void isInGroup(String groupId, List<String> ids) {
        OpenIMClient.getInstance().groupManager.getGroupMembersInfo(new IMUtil.IMCallBack<List<GroupMembersInfo>>() {
            @Override
            public void onSuccess(List<GroupMembersInfo> data) {
                if (data.isEmpty()) return;
                for (GroupMembersInfo datum : data) {
                    MultipleChoice choice = new MultipleChoice(datum.getUserID());
                    choice.name = datum.getNickname();
                    choice.icon = datum.getFaceURL();
                    choice.isSelect = true;
                    choice.isEnabled = false;
                    if (!contains(choice))
                        metaData.val().add(choice);
                }
                metaData.update();
            }
        }, groupId, ids);
    }

    public State<List<MultipleChoice>> metaData = new State<>(new ArrayList<>());
    public State<List<MultipleChoice>> inviteList = new State<>(new ArrayList<>());

    public void finishIntention() {
        Postcard postcard = ARouter.getInstance().build(Routes.Main.HOME);
        Postcard postcard2 = ARouter.getInstance().build(Routes.Conversation.CHAT);
        LogisticsCenter.completion(postcard);
        LogisticsCenter.completion(postcard2);
        ActivityManager.finishAllExceptActivity(postcard.getDestination(),
            postcard2.getDestination());

        toFinish();
    }

    public void toFinish() {
        if (null != onFinishListener)
            onFinishListener.onFinish();
    }

    public boolean contains(MultipleChoice data) {
        return metaData.val().contains(data);
    }

    public void addMetaData(String id, String name, String faceUrl) {
        MultipleChoice data = new MultipleChoice();
        data.key = id;
        data.name = name;
        data.icon = faceUrl;
        addDate(data);
    }

    public void addDate(MultipleChoice choice) {
        if (!metaData.val().contains(choice)) {
            metaData.val().add(choice);
            metaData.update();
        }
    }

    public void removeMetaData(String id) {
        MultipleChoice data = new MultipleChoice();
        data.key = id;
        if (metaData.val().contains(data)) {
            metaData.val().remove(data);
            metaData.update();
        }
    }

    @SuppressLint("SetTextI18n")
    public void bindDataToView(LayoutSelectedFriendsBinding view) {
        Context context = view.getRoot().getContext();
        metaData.observe((LifecycleOwner) context, v -> {
            List<MultipleChoice> userInfoList = new ArrayList<>();
            if (isInvite()) {
                //邀请入群 这里只加入新邀请人员
                for (MultipleChoice choice : v) {
                    if (choice.isEnabled && choice.isSelect) {
                        userInfoList.add(choice);
                    }
                }
                inviteList.val().clear();
                inviteList.val().addAll(userInfoList);
                inviteList.update();
            } else {
                userInfoList.addAll(v);
            }

            if (!userInfoList.isEmpty()) {
                view.more2.setVisibility(View.VISIBLE);
                view.content.setVisibility(View.VISIBLE);
            } else {
                view.content.setVisibility(View.GONE);
                view.more2.setVisibility(View.GONE);
            }
            view.selectNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips2), userInfoList.size()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < userInfoList.size(); i++) {
                builder.append(userInfoList.get(i).name);
                if (i != userInfoList.size() - 1) builder.append("、");
            }
            view.content.setText(builder.toString());
            view.submit.setEnabled(userInfoList.size() > 0);
            view.submit.setText(context.getString(io.openim.android.ouicore.R.string.sure) + "（" + userInfoList.size() + "/999）");
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
        if (isInvite()) {
            inviteList.observe((LifecycleOwner) context, userInfos -> {
                view2.selectedNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips2), userInfos.size()));
            });
        } else {
            metaData.observe((LifecycleOwner) context, userInfos -> {
                view2.selectedNum.setText(String.format(context.getString(io.openim.android.ouicore.R.string.selected_tips2), userInfos.size()));
            });
        }

        view.selectLy.setOnClickListener(v -> {
            adapter.setItems(isInvite() ? inviteList.val() : metaData.val());
            bottomPopDialog.show();
        });
    }

    public void submitTap(Button button) {
        button.setOnClickListener(v -> {
            if (isSelectFriends()) {
                toFinish();
                return;
            }
            if (intention != Intention.Forward) {
                finishIntention();
                return;
            }

            //转发
            showConfirmDialog(v.getContext());
        });
    }

    private void showConfirmDialog(Context context) {
        ForwardDialog dialog = new ForwardDialog(context);
        dialog.show();
    }


    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface OnFinishListener {
        void onFinish();

    }
}
