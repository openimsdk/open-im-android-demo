package io.openim.android.ouigroup.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.mao.sortletterlib.SortLetterView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityInitiateGroupBinding;
import io.openim.android.ouigroup.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouigroup.databinding.ItemPsrsonStickyBinding;
import io.openim.android.ouigroup.entity.ExUserInfo;
import io.openim.android.ouigroup.vm.GroupVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.FriendInfo;

/**
 * 发起群聊/邀请入群/移除群聊
 */
@Route(path = Routes.Group.CREATE_GROUP)
public class InitiateGroupActivity extends BaseActivity<GroupVM, ActivityInitiateGroupBinding> {

    private RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder> adapter;

    //邀请入群
    public static final String IS_INVITE_TO_GROUP = "isInviteToGroup";
    //移除群聊
    public static final String IS_REMOVE_GROUP = "isRemoveGroup";

    private boolean isInviteToGroup = false;
    private boolean isRemoveGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isInviteToGroup = getIntent().getBooleanExtra(IS_INVITE_TO_GROUP, false);
        isRemoveGroup = getIntent().getBooleanExtra(IS_REMOVE_GROUP, false);
        if (isInviteToGroup || isRemoveGroup)
            bindVMByCache(GroupVM.class);
        else
            bindVM(GroupVM.class, true);

        vm.isInviteToGroup = isInviteToGroup;
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityInitiateGroupBinding.inflate(getLayoutInflater()));

        initView();

        vm.getAllFriend();
        listener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isInviteToGroup && !isRemoveGroup)
            removeCacheVM();
    }

    private void initView() {
        sink();
        if (isInviteToGroup)
            view.title.setText(io.openim.android.ouicore.R.string.Invite_to_the_group);
        if (isRemoveGroup)
            view.title.setText(io.openim.android.ouicore.R.string.remove_group);

        view.scrollView.fullScroll(View.FOCUS_DOWN);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerViewAdapter<ExUserInfo, RecyclerView.ViewHolder>() {
            private int STICKY = 1;
            private int ITEM = 2;

            private String lastSticky = "";

            @Override
            public void setItems(List<ExUserInfo> items) {
                lastSticky = items.get(0).sortLetter;
                items.add(0, getExUserInfo());
                for (int i = 0; i < items.size(); i++) {
                    ExUserInfo userInfo = items.get(i);
                    if (!lastSticky.equals(userInfo.sortLetter)) {
                        lastSticky = userInfo.sortLetter;
                        items.add(i, getExUserInfo());
                    }
                }
                super.setItems(items);
            }

            @NonNull
            private ExUserInfo getExUserInfo() {
                ExUserInfo exUserInfo = new ExUserInfo();
                exUserInfo.sortLetter = lastSticky;
                exUserInfo.isSticky = true;
                return exUserInfo;
            }

            @Override
            public int getItemViewType(int position) {
                return getItems().get(position).isSticky ? STICKY : ITEM;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == ITEM)
                    return new ItemViewHo(parent);

                return new StickyViewHo(parent);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, ExUserInfo data, int position) {
                if (getItemViewType(position) == ITEM) {
                    ItemViewHo itemViewHo = (ItemViewHo) holder;
                    FriendInfo friendInfo = data.userInfo.getFriendInfo();
                    itemViewHo.view.avatar.load(friendInfo.getFaceURL());
                    itemViewHo.view.nickName.setText(friendInfo.getNickname());
                    itemViewHo.view.select.setChecked(data.isSelect);
                    if (!data.isEnabled)
                        itemViewHo.view.item.setOnClickListener(null);
                    else
                        itemViewHo.view.item.setOnClickListener(v -> {
                            data.isSelect = !data.isSelect;
                            notifyItemChanged(position);
                            int num = getSelectNum();
                            view.selectNum.setText("已选择：" + num + "人");
                            view.submit.setText("确定（" + num + "/999）");
                            view.submit.setEnabled(num > 0);
                        });
                } else {
                    StickyViewHo stickyViewHo = (StickyViewHo) holder;
                    stickyViewHo.view.title.setText(data.sortLetter);
                }

            }

        };
        view.recyclerView.setAdapter(adapter);
    }

    private int getSelectNum() {
        List<FriendInfo> friendInfos = new ArrayList<>();
        int num = 0;
        for (ExUserInfo item : adapter.getItems()) {
            if (item.isSelect && item.isEnabled) {
                num++;
                friendInfos.add(item.userInfo.getFriendInfo());
            }
        }
        vm.selectedFriendInfo.setValue(friendInfos);
        return num;
    }

    private void listener() {
        vm.letters.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            StringBuilder letters = new StringBuilder();
            for (String s : v) {
                letters.append(s);
            }
            view.sortView.setLetters(letters.toString());
        });


        vm.exUserInfo.observe(this, v -> {
            if (null == v || v.isEmpty()) return;
            List<ExUserInfo> exUserInfos = new ArrayList<>(v);
            adapter.setItems(exUserInfos);
        });

        view.sortView.setOnLetterChangedListener((letter, position) -> {
            for (int i = 0; i < adapter.getItems().size(); i++) {
                ExUserInfo exUserInfo = adapter.getItems().get(i);
                if (!exUserInfo.isSticky)
                    continue;
                if (exUserInfo.sortLetter.equalsIgnoreCase(letter)) {
                    View viewByPosition = view.recyclerView.getLayoutManager().findViewByPosition(i);
                    if (viewByPosition != null) {
                        view.scrollView.smoothScrollTo(0, viewByPosition.getTop());
                    }
                    return;
                }
            }
        });
        view.submit.setOnClickListener(v -> {
            if (isInviteToGroup) {
                vm.inviteUserToGroup(vm.selectedFriendInfo.getValue());
                return;
            }
            if (isRemoveGroup) {
                vm.kickGroupMember(vm.selectedFriendInfo.getValue());
                return;
            }
            createLauncher.launch(new Intent(this, CreateGroupActivity.class));
        });
    }

    @Override
    public void onSuccess(Object body) {
        super.onSuccess(body);
        finish();
    }

    private final ActivityResultLauncher<Intent> createLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                finish();
            }
        });

    public static class ItemViewHo extends RecyclerView.ViewHolder {
        ItemPsrsonSelectBinding view;

        public ItemViewHo(@NonNull View itemView) {
            super(ItemPsrsonSelectBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonSelectBinding.bind(this.itemView);
        }
    }

    public static class StickyViewHo extends RecyclerView.ViewHolder {
        ItemPsrsonStickyBinding view;

        public StickyViewHo(@NonNull View itemView) {
            super(ItemPsrsonStickyBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemPsrsonStickyBinding.bind(this.itemView);
        }
    }
}
