package io.openim.android.ouicontact.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityOftenSerchBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SearchResultItem;

public class SearchGroupAndFriendsActivity extends BaseActivity<SearchVM,
    ActivityOftenSerchBinding> {

    private final Handler handler = new Handler();

    private static final int TITLE = 0;
    private static final int CONTACT_ITEM = 1;
    private static final int GROUP_ITEM = 2;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityOftenSerchBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    private void listener() {
        view.cancel.setOnClickListener(v -> finish());
        vm.searchContent.observe(this, s -> {
            clearData();
            if (s.isEmpty()) return;
            vm.page = 1;

            vm.searchFriendV2();
            vm.searchGroupV2();
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) vm.searchContent.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    vm.page = 1;
                    vm.searchContent.setValue(input);
                }, 500);

            }
        });

        vm.friendInfo.observe(this, friendInfos -> {
            addNape(getString(io.openim.android.ouicore.R.string.contact), friendInfos);
        });
        vm.groupsInfo.observe(this, groupInfos -> {
            addNape(getString(io.openim.android.ouicore.R.string.group), groupInfos);
        });

    }

    private void addNape(String title, List data) {
        if (data.isEmpty()) return;
        int start = adapter.getItems().size();
        adapter.getItems().add(title);//包含1表示有查看更多
        adapter.getItems().addAll(data);
        adapter.notifyItemRangeInserted(start, adapter.getItems().size());
    }

    private void clearData() {
        vm.clearData();
        adapter.getItems().clear();
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter() {

            @Override
            public int getItemViewType(int position) {
                Object o = getItems().get(position);
                if (o instanceof String) return TITLE;
                if (o instanceof FriendInfo) return CONTACT_ITEM;
                if (o instanceof GroupInfo) return GROUP_ITEM;
                return super.getItemViewType(position);
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                if (viewType == TITLE) return new ViewHol.TitleViewHolder(parent);
                if (viewType == CONTACT_ITEM || viewType == GROUP_ITEM)
                    return new ViewHol.ItemViewHo(parent);

                return super.onCreateViewHolder(parent, viewType);
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data,
                                   int position) {
                if (getItemViewType(position) == TITLE) {
                    String title = (String) data;
                    ViewHol.TitleViewHolder titleViewHolder = (ViewHol.TitleViewHolder) holder;
                    titleViewHolder.view.title.setText(title);
                } else {
                    ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                    itemViewHo.view.select.setVisibility(View.GONE);
                    final Intent intent = new Intent();
                    if (data instanceof FriendInfo) {
                        //联系人
                        FriendInfo da = (FriendInfo) data;
                        itemViewHo.view.avatar.load(da.getFaceURL());
                        itemViewHo.view.nickName.setText(da.getNickname());

                        intent.putExtra(Constant.K_ID, da.getUserID());
                        intent.putExtra(Constant.K_NAME, da.getNickname());
                    }
                    if ((data instanceof GroupInfo)) {
                        //群组
                        GroupInfo groupInfo = (GroupInfo) data;
                        itemViewHo.view.avatar.load(groupInfo.getFaceURL(), true);
                        itemViewHo.view.nickName.setText(groupInfo.getGroupName());

                        intent.putExtra(Constant.K_GROUP_ID, groupInfo.getGroupID());
                        intent.putExtra(Constant.K_NAME, groupInfo.getGroupName());
                    }

                    ((ViewHol.ItemViewHo) holder).view.getRoot().setOnClickListener(v -> {
                        setResult(RESULT_OK, intent);
                        finish();
                    });

                }
            }
        });
        adapter.setItems(new ArrayList());
    }

    void init() {

    }
}
