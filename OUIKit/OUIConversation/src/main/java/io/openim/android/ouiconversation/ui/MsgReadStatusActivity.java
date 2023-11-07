package io.openim.android.ouiconversation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivityMsgReadStatusBinding;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouiconversation.vm.MsgStatusVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.GroupHasReadInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class MsgReadStatusActivity extends BasicActivity<ActivityMsgReadStatusBinding> {

    private boolean isRead = false;
    private RecyclerViewAdapter<GroupMembersInfo, RecyclerView.ViewHolder> adapter;

    private MsgStatusVM vm;
    private GroupHasReadInfo groupHasReadInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding(ActivityMsgReadStatusBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    void init() {
        vm= Easy.installVM(this, MsgStatusVM.class);
        vm.conversationId = getIntent().getStringExtra(Constant.K_ID);
        vm.msgId = getIntent().getStringExtra(Constant.K_RESULT);
         groupHasReadInfo = (GroupHasReadInfo) getIntent().getSerializableExtra(Constant.K_RESULT2);
    }

    private void initView() {
        view.title1.setText(String.format(getString(io.openim.android.ouicore.R.string.unread),
            groupHasReadInfo.getUnreadCount() + ""));
        view.title2.setText(String.format(getString(io.openim.android.ouicore.R.string.readed),
            groupHasReadInfo.getHasReadCount() + ""));

        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<GroupMembersInfo, RecyclerView.ViewHolder>() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, GroupMembersInfo data, int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                itemViewHo.view.avatar.load(data.getFaceURL());
                itemViewHo.view.nickName.setText(data.getNickname());
                itemViewHo.view.select.setVisibility(View.GONE);
                itemViewHo.view.identity.setVisibility(View.GONE);
            }
        };
        view.recyclerview.setAdapter(adapter);
        adapter.setItems(vm.groupMembersInfoList.val());
        refreshMembersInfo();
    }

    private void listener() {
        view.menu1.setOnClickListener(view1 -> {
            isRead = false;
            menuChange();
        });
        view.menu2.setOnClickListener(view1 -> {
            isRead = true;
            menuChange();
        });

        vm.groupMembersInfoList.observe(this, groupMembersInfos -> {
            if (groupMembersInfos.isEmpty()) return;
            adapter.notifyItemRangeChanged(groupMembersInfos.size()-vm.count
                ,groupMembersInfos.size());
        });
        view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (adapter.getItems().size()>=vm.count
                    &&lastVisiblePosition >= adapter.getItems().size() - 3) {
                    loadMembersInfo();
                }
            }
        });
    }

    private void menuChange() {
        if (isRead) {
            view.menu1bg.setVisibility(View.GONE);
            view.menu2bg.setVisibility(View.VISIBLE);
        } else {
            view.menu1bg.setVisibility(View.VISIBLE);
            view.menu2bg.setVisibility(View.GONE);
        }
        refreshMembersInfo();
    }

    private void refreshMembersInfo() {
        vm.offset = 0;
        vm.groupMembersInfoList.val().clear();
        adapter.notifyDataSetChanged();
        loadMembersInfo();
    }

    private void loadMembersInfo() {
        if (isRead) {
            vm.getGroupMessageReaderList(0);
        } else {
            vm.getGroupMessageReaderList(1);
        }
    }

}
