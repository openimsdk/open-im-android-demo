package io.openim.android.ouiconversation.ui;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;

import java.util.List;

import io.openim.android.ouiconversation.R;

import io.openim.android.ouiconversation.adapter.MessageViewHolder;
import io.openim.android.ouiconversation.databinding.FragmentContactListBinding;
import io.openim.android.ouiconversation.databinding.LayoutContactItemBinding;
import io.openim.android.ouiconversation.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.ConversationInfo;

@Route(path = Routes.Conversation.CONTACT_LIST)
public class ContactListFragment extends BaseFragment<ContactListVM> implements ContactListVM.ViewAction {

    private long mLastClickTime;
    private long timeInterval = 700;

    private FragmentContactListBinding view;
    private CustomAdapter adapter;

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(ContactListVM.class);
        BaseApp.inst().putVM(vm);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = FragmentContactListBinding.inflate(getLayoutInflater());
        init();
        return view.getRoot();
    }


    @SuppressLint("NewApi")
    private void init() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SwipeMenuCreator mSwipeMenuCreator = (leftMenu, rightMenu, position) -> {
            SwipeMenuItem delete = new SwipeMenuItem(getContext());
            delete.setText(R.string.remove);
            delete.setHeight(MATCH_PARENT);
            delete.setWidth(Common.dp2px(73));
            delete.setTextSize(16);
            delete.setTextColor(getContext().getColor(android.R.color.white));
            delete.setBackgroundColor(Color.parseColor("#FFAB41"));


            SwipeMenuItem top = new SwipeMenuItem(getContext());
            top.setText(R.string.top);
            top.setHeight(MATCH_PARENT);
            top.setWidth(Common.dp2px(73));
            top.setTextSize(16);
            top.setTextColor(getContext().getColor(android.R.color.white));
            top.setBackgroundColor(Color.parseColor("#1B72EC"));

            //右侧添加菜单
            rightMenu.addMenuItem(top);
            rightMenu.addMenuItem(delete);
        };
        view.recyclerView.setSwipeMenuCreator(mSwipeMenuCreator);
        view.recyclerView.setOnItemMenuClickListener((menuBridge, adapterPosition) -> {
            int menuPosition = menuBridge.getPosition();
            if (menuPosition == 0) {

            } else {
                MsgConversation conversationInfo =vm.conversations.getValue().get(adapterPosition);
                vm.conversations.getValue().remove(conversationInfo);
                adapter.notifyItemRemoved(adapterPosition);
                vm.deleteConversationFromLocalAndSvr(conversationInfo.conversationInfo.getConversationID());
            }
        });
        view.recyclerView.setOnItemClickListener((view, position) -> {
            long nowTime = System.currentTimeMillis();
            if (nowTime - mLastClickTime < timeInterval)
                return;
            mLastClickTime = nowTime;

            MsgConversation msgConversation = vm.conversations.getValue().get(position);
            Intent intent = new Intent(getContext(), ChatActivity.class)
                .putExtra(Constant.K_NAME, msgConversation.conversationInfo.getShowName());

            if (msgConversation.conversationInfo.getConversationType() == 1)
                intent.putExtra(Constant.K_ID, msgConversation.conversationInfo.getUserID());
            if (msgConversation.conversationInfo.getConversationType() == 2)
                intent.putExtra(Constant.K_GROUP_ID, msgConversation.conversationInfo.getGroupID());

            if (msgConversation.conversationInfo.getGroupAtType() == Constant.GroupAtType.groupNotification)
                intent.putExtra(Constant.K_NOTICE, msgConversation.notificationMsg);
            startActivity(intent);
            //重置强提醒
            if (msgConversation.conversationInfo.getGroupAtType() == Constant.GroupAtType.groupNotification)
                OpenIMClient.getInstance().conversationManager.resetConversationGroupAtType(null, msgConversation.conversationInfo.getConversationID());
        });

         adapter = new CustomAdapter(getContext());
        view.recyclerView.setAdapter(adapter);

//        view.recyclerView.addItemDecoration(new DefaultItemDecoration(getActivity().getColor(android.R.color.transparent), 1, 36));
        vm.conversations.observe(getActivity(), v -> {
            if (null == v || v.size() == 0) return;
            adapter.setConversationInfos(v);
            adapter.notifyDataSetChanged();
        });
    }


    @Override
    public void onErr(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
    }

    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<MsgConversation> conversationInfos;
        private Context context;

        public CustomAdapter(Context context) {
            this.context = context;
        }

        public void setConversationInfos(List<MsgConversation> conversationInfos) {
            this.conversationInfos = conversationInfos;
        }

        public List<MsgConversation> getConversationInfos() {
            return conversationInfos;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final LayoutContactItemBinding viewBinding;

            public ViewHolder(LayoutContactItemBinding viewBinding) {
                super(viewBinding.getRoot());
                this.viewBinding = viewBinding;
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ViewHolder(LayoutContactItemBinding.inflate(LayoutInflater.from(viewGroup.getContext())));
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            MsgConversation msgConversation = conversationInfos.get(position);

            viewHolder.viewBinding.avatar.load(msgConversation.conversationInfo.getFaceURL(), msgConversation.conversationInfo.getConversationType() == Constant.SessionType.GROUP_CHAT);
            viewHolder.viewBinding.nickName.setText(msgConversation.conversationInfo.getShowName());

            String lastMsg = IMUtil.getMsgParse(msgConversation.lastMsg);
            if (msgConversation.lastMsg.getContentType() == Constant.MsgType.BULLETIN
                && null != msgConversation.notificationMsg && null != msgConversation.notificationMsg.group
                && !TextUtils.isEmpty(msgConversation.notificationMsg.group.notification))
                lastMsg = "[" + context.getString(io.openim.android.ouicore.R.string.group_bulletin) + "]"
                    + msgConversation.notificationMsg.group.notification;

            viewHolder.viewBinding.lastMsg.setText(lastMsg);
            viewHolder.viewBinding.badge.badge.setVisibility(msgConversation.conversationInfo.getUnreadCount() != 0 ? View.VISIBLE : View.GONE);
            viewHolder.viewBinding.badge.badge.setText(msgConversation.conversationInfo.getUnreadCount() + "");
            viewHolder.viewBinding.time.setText(TimeUtil.getTimeString(msgConversation.conversationInfo.getLatestMsgSendTime()));
        }


        @Override
        public int getItemCount() {
            return null == conversationInfos ? 0 : conversationInfos.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseApp.viewModels.remove(vm.getClass().getCanonicalName());
    }
}
