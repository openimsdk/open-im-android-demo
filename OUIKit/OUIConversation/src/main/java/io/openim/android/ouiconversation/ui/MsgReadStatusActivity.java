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
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.CallHistory;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

public class MsgReadStatusActivity extends BaseActivity<GroupVM, ActivityMsgReadStatusBinding> {

    private boolean isRead = true;
    private RecyclerViewAdapter<ExGroupMemberInfo, RecyclerView.ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMsgReadStatusBinding.inflate(getLayoutInflater()));
        sink();
        init();
        initView();
        listener();
    }

    void init() {
        vm.groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        List<String> uIds = getIntent().getStringArrayListExtra(Constant.K_ID);
        if (null == uIds)
            uIds = new ArrayList<>();
        vm.hasReadIDList = uIds;
        vm.getGroupsInfo();
    }

    private void initView() {
        view.title1.setText(String.format(getString(io.openim.android.ouicore.R.string.readed), vm.hasReadIDList.size() + ""));
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<ExGroupMemberInfo, RecyclerView.ViewHolder>() {


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHol.ItemViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExGroupMemberInfo data, int position) {
                ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                itemViewHo.view.avatar.load(data.groupMembersInfo.getFaceURL());
                itemViewHo.view.nickName.setText(data.groupMembersInfo.getNickname());
                itemViewHo.view.select.setVisibility(View.GONE);
                itemViewHo.view.identity.setVisibility(View.GONE);
            }
        };
        view.recyclerview.setAdapter(adapter);
        refreshMembersInfo();
    }

    private void listener() {
        vm.groupsInfo.observe(this, groupInfo -> {
            view.title2.setText(String.format(getString(io.openim.android.ouicore.R.string.unread),
                (groupInfo.getMemberCount() - vm.hasReadIDList.size() - 1) + ""));
        });
        view.menu1.setOnClickListener(view1 -> {
            isRead = true;
            menuChange();
        });
        view.menu2.setOnClickListener(view1 -> {
            isRead = false;
            menuChange();
        });

        vm.superGroupMembers.observe(this, groupMembersInfos -> {
            if (groupMembersInfos.isEmpty()) return;
            if (!isRead) {
                Iterator<ExGroupMemberInfo> iterator = groupMembersInfos.iterator();
                while (iterator.hasNext()) {
                    ExGroupMemberInfo exGroupMemberInfo = iterator.next();
                    if (vm.hasReadIDList.contains(exGroupMemberInfo.groupMembersInfo.getUserID())
                        || exGroupMemberInfo.groupMembersInfo.getUserID().equals(BaseApp.inst().loginCertificate.userID))
                        iterator.remove();
                }
            }
            adapter.setItems(groupMembersInfos);
        });
        view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition >= adapter.getItems().size() - 3) {
                    loadMembersInfo();
                }
            }
        });
    }

    private void menuChange() {
        if (isRead) {
            view.menu1bg.setVisibility(View.VISIBLE);
            view.menu2bg.setVisibility(View.GONE);
        } else {
            view.menu1bg.setVisibility(View.GONE);
            view.menu2bg.setVisibility(View.VISIBLE);
        }
        refreshMembersInfo();
    }

    private void refreshMembersInfo() {
        vm.page = 0;
        vm.superGroupMembers.getValue().clear();
        adapter.notifyDataSetChanged();
        if (isRead) {
            vm.loadHasReadGroupMembersInfo();
        } else {
            vm.getSuperGroupMemberList();
        }
    }

    private void loadMembersInfo() {
        vm.page++;
        if (isRead)
            vm.loadHasReadGroupMembersInfo();
        else {
            vm.getSuperGroupMemberList();
        }
    }

}
