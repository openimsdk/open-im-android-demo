package io.openim.android.ouiconversation.ui.fragment;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.ouiconversation.R;

import io.openim.android.ouiconversation.databinding.FragmentContactListBinding;
import io.openim.android.ouiconversation.databinding.LayoutAddActionBinding;
import io.openim.android.ouiconversation.ui.ChatActivity;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.Opt;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.ConversationReq;
import io.openim.android.sdk.models.FriendInfo;

@Route(path = Routes.Conversation.CONTACT_LIST)
public class ConversationListFragment extends BaseFragment<ContactListVM> implements ContactListVM.ViewAction, Observer {

    private long mLastClickTime;

    private FragmentContactListBinding view;
    private CustomAdapter adapter;
    private HasPermissions hasScanPermission;
    private final UserLogic user = Easy.find(UserLogic.class);
    private final HashSet<Integer> slideSet = new HashSet<>();
    private int slideNum = 0;


    public static ConversationListFragment newInstance() {

        Bundle args = new Bundle();

        ConversationListFragment fragment = new ConversationListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(ContactListVM.class);
        BaseApp.inst().putVM(vm);
        Obs.inst().addObserver(this);
        Activity activity = getActivity();
        if (null != activity) {
            activity.runOnUiThread(() -> hasScanPermission = new HasPermissions(getContext(),
                Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = FragmentContactListBinding.inflate(getLayoutInflater());

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.header.getLayoutParams();
        lp.setMargins(0, SinkHelper.getStatusBarHeight(), 0, 0);
        view.header.setLayoutParams(lp);
        init();

        return view.getRoot();
    }

    private final OnItemClickListener onItemClickListener = (view, position) -> {
        long nowTime = System.currentTimeMillis();
        long timeInterval = 700;
        if (nowTime - mLastClickTime < timeInterval) return;
        mLastClickTime = nowTime;

        slideSet.remove(position);
        MsgConversation msgConversation = vm.conversations.getValue().get(position);
        Intent intent = new Intent(getContext(), ChatActivity.class)
            .putExtra(Constants.K_NAME
                , msgConversation.conversationInfo.getShowName());
        if (msgConversation.conversationInfo.getConversationType() == ConversationType.SINGLE_CHAT)
            intent.putExtra(Constants.K_ID, msgConversation.conversationInfo.getUserID());

        if (msgConversation.conversationInfo.getConversationType() == ConversationType.GROUP_CHAT
            || msgConversation.conversationInfo.getConversationType() == ConversationType.SUPER_GROUP_CHAT)
            intent.putExtra(Constants.K_GROUP_ID, msgConversation.conversationInfo.getGroupID());

        if (msgConversation.conversationInfo.getGroupAtType() == ConversationType.NOTIFICATION)
            intent.putExtra(Constants.K_NOTICE, msgConversation.notificationMsg);
        startActivity(intent);

        // remove the conversion's @ status
        ConversationReq conversationReq = new ConversationReq();
        conversationReq.setGroupAtType(0);
        OpenIMClient.getInstance().conversationManager.setConversation(null, msgConversation.conversationInfo.getConversationID(), conversationReq);
    };

    @SuppressLint("NewApi")
    private void init() {
        view.setLifecycleOwner(this);
        view.setUser(user);

        initHeader();
        view.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ChatVM chatVM = new ChatVM();

        adapter = new CustomAdapter(onItemClickListener);
        view.recyclerView.setAdapter(adapter);
        view.recyclerView.setItemAnimator(null);

        vm.conversations.observe(getActivity(), v -> {
            if (null == v || v.size() == 0) return;
            slideSet.clear();
            for (int i = 0; i < v.size(); i++) {
                ConversationInfo con = v.get(i).conversationInfo;
                if (con.getRecvMsgOpt() == 0 && con.getUnreadCount() != 0) {
                    slideSet.add(i);
                }
            }
            adapter.setConversationInfos(v);
            adapter.notifyDataSetChanged();
        });

        Animation animation = AnimationUtils.loadAnimation(getActivity(),
            R.anim.animation_repeat_spinning);
        Easy.find(UserLogic.class).connectStatus
            .observe(getActivity(), connectStatus -> {
                if (connectStatus == UserLogic.ConnectStatus.CONNECTING
                    || connectStatus == UserLogic.ConnectStatus.SYNCING) {
                    view.status.startAnimation(animation);
                } else {
                    view.status.clearAnimation();
                }
            });


        view.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (Common.mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    Common.mShouldScroll = false;
                    Common.smoothMoveToPosition(view.recyclerView, Common.mToPosition);
                }
            }
        });
    }

    private void initHeader() {
        view.avatar.load(null, null);
        if (BaseApp.inst().loginCertificate != null) {
            view.avatar.load(BaseApp.inst().loginCertificate.faceURL, BaseApp.inst().loginCertificate.nickname);
            view.name.setText(BaseApp.inst().loginCertificate.nickname);
        }
        user.info.observe(getActivity(), v -> {
            if (v != null) {
                view.avatar.load(v.getFaceURL(), v.getNickname());
                view.name.setText(v.getNickname());
            }
        });
        view.addFriend.setOnClickListener(this::showPopupWindow);
    }


    private void showPopupWindow(View v) {
        //初始化一个PopupWindow，width和height都是WRAP_CONTENT
        PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        LayoutAddActionBinding view = LayoutAddActionBinding.inflate(getLayoutInflater());
        view.addFriend.setOnClickListener(c -> {
            popupWindow.dismiss();
            ARouter.getInstance().build(Routes.Main.ADD_CONVERS).navigation();
        });
        view.addGroup.setOnClickListener(c -> {
            popupWindow.dismiss();
            ARouter.getInstance().build(Routes.Main.ADD_CONVERS).withBoolean(Constants.K_RESULT,
                false).navigation();
        });
        view.createGroup.setOnClickListener(c -> {
            popupWindow.dismiss();

            SelectTargetVM targetVM = Easy.installVM(SelectTargetVM.class)
                .setIntention(SelectTargetVM.Intention.isCreateGroup);
            targetVM.setOnFinishListener(() -> {
                GroupVM groupVM = BaseApp.inst().getVMByCache(GroupVM.class);
                if (null == groupVM)
                    groupVM = new GroupVM();
                groupVM.selectedFriendInfo.getValue().clear();
                List<MultipleChoice> multipleChoices = targetVM.metaData.getValue();
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
            });
            ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
        });
        //设置PopupWindow的视图内容
        popupWindow.setContentView(view.getRoot());
        //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(true);

        //PopupWindow在targetView下方弹出
        popupWindow.showAsDropDown(v);
    }


    @Override
    public void onErr(String msg) {
        try {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
        }

    }

    public void clickSlideSet() {
        if (slideSet.isEmpty()) return;
        if (slideNum > slideSet.size() - 1)
            slideNum = 0;
        Common.smoothMoveToPosition(view.recyclerView,
            (int) slideSet.toArray()[slideNum++]);
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
    }

    static class CustomAdapter extends RecyclerView.Adapter<ViewHol.ContactItemHolder> {

        private List<MsgConversation> conversationInfos;
        private OnItemClickListener itemClickListener;

        public CustomAdapter(OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public void setConversationInfos(List<MsgConversation> conversationInfos) {
            this.conversationInfos = conversationInfos;
            notifyItemChanged(1);
        }

        @Override
        public ViewHol.ContactItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ViewHol.ContactItemHolder(viewGroup);
        }

        @Override
        public void onBindViewHolder(ViewHol.ContactItemHolder viewHolder, int position) {
            final int index = position;
            viewHolder.viewBinding.getRoot().setOnClickListener(new OnDedrepClickListener() {
                @Override
                public void click(View v) {
                    if (null != itemClickListener)
                        itemClickListener.onItemClick(v, index);
                }
            });
            MsgConversation msgConversation = conversationInfos.get(position);
            boolean isGroup =
                msgConversation.conversationInfo.getConversationType() != ConversationType.SINGLE_CHAT;
            viewHolder.viewBinding.avatar.load(msgConversation.conversationInfo.getFaceURL(),
                isGroup, isGroup ? null : msgConversation.conversationInfo.getShowName());
            viewHolder.viewBinding.nickName.setText(msgConversation.conversationInfo.getShowName());

            if (msgConversation.conversationInfo.getRecvMsgOpt() != Opt.NORMAL) {
                viewHolder.viewBinding.noDisturbTips
                    .setVisibility(msgConversation.conversationInfo.getUnreadCount() > 0 ?
                        View.VISIBLE : View.GONE);
                viewHolder.viewBinding.noDisturbIc.setVisibility(View.VISIBLE);
                viewHolder.viewBinding.badge.badge.setVisibility(View.GONE);
            } else {
                viewHolder.viewBinding.badge.badge.setVisibility(View.VISIBLE);
                viewHolder.viewBinding.noDisturbTips.setVisibility(View.GONE);
                viewHolder.viewBinding.noDisturbIc.setVisibility(View.GONE);
                int count = msgConversation.conversationInfo.getUnreadCount();
                viewHolder.viewBinding.badge.badge.setVisibility(count != 0 ? View.VISIBLE :
                    View.GONE);
                viewHolder.viewBinding.badge.badge.setText(count >= 100 ? "99+" : String.valueOf(count));
            }
            viewHolder.viewBinding.time.setText(TimeUtil.getTimeString(msgConversation.conversationInfo.getLatestMsgSendTime()));

            viewHolder.viewBinding.setTop.setVisibility(msgConversation.conversationInfo.isPinned() ? View.VISIBLE : View.GONE);

            CharSequence lastMsg = msgConversation.lastMsg;
            viewHolder.viewBinding.lastMsg.setText(lastMsg);
        }

        @Override
        public int getItemCount() {
            return null == conversationInfos ? 0 : conversationInfos.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseApp.inst().removeCacheVM(vm.getClass());
        Obs.inst().deleteObserver(this);

        Animation animation = view.status.getAnimation();
        if (null != animation) animation.cancel();
    }

    @Override
    public void update(Observable observable, Object o) {
        Obs.Message message = (Obs.Message) o;
        if (message.tag == Constants.Event.USER_INFO_UPDATE) {
            initHeader();
        }
    }
}
