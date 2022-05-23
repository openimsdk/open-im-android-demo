package io.openim.android.ouicontact.ui;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import static io.openim.android.ouicontact.utils.Constant.CONVERSATION_ID;
import static io.openim.android.ouicontact.utils.Constant.K_NAME;
import static io.openim.android.ouicontact.utils.Constant.K_USER_ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import java.util.List;

import io.openim.android.ouicontact.R;

import io.openim.android.ouicontact.databinding.FragmentContactListBinding;
import io.openim.android.ouicontact.databinding.LayoutContactItemBinding;
import io.openim.android.ouicontact.vm.ContactListVM;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.entity.MsgConversation;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.sdk.models.ConversationInfo;
import io.openim.android.sdk.models.Message;

@Route(path = Routes.Contact.CONTACT_LIST)
public class ContactListFragment extends BaseFragment<ContactListVM> implements ContactListVM.ViewAction {


    private FragmentContactListBinding view;

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(ContactListVM.class);
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

        });
        view.recyclerView.setOnItemClickListener((view, position) -> {
            MsgConversation msgConversation = vm.conversations.getValue().get(position);
            startActivity(new Intent(getContext(), ChatActivity.class)
                    .putExtra(K_USER_ID, msgConversation.conversationInfo.getUserID())
                    .putExtra(K_NAME,msgConversation.conversationInfo.getShowName())
            );
        });

        CustomAdapter adapter = new CustomAdapter(getContext());
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


    static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<MsgConversation> conversationInfos;
        private Context context;

        public CustomAdapter(Context context) {
            this.context = context;
        }

        public void setConversationInfos(List<MsgConversation> conversationInfos) {
            this.conversationInfos = conversationInfos;
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

            viewHolder.viewBinding.avatar.load(msgConversation.conversationInfo.getFaceURL());
            viewHolder.viewBinding.nickName.setText(msgConversation.conversationInfo.getShowName());
            viewHolder.viewBinding.lastMsg.setText(msgConversation.lastMsg.getContent());
            viewHolder.viewBinding.badge.setVisibility(msgConversation.conversationInfo.getUnreadCount() != 0 ? View.VISIBLE : View.GONE);
            viewHolder.viewBinding.badge.setText(msgConversation.conversationInfo.getUnreadCount() + "");
            viewHolder.viewBinding.time.setText(TimeUtil.getTimeString(msgConversation.conversationInfo.getLatestMsgSendTime()));
        }

        @Override
        public int getItemCount() {
            return null==conversationInfos?0:conversationInfos.size();
        }
    }

}