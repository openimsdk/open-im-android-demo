package io.openim.android.ouicontact.ui.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.List;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivitySearchFriendsBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupMembersInfo;

@Route(path = Routes.Contact.SEARCH_FRIENDS)
public class SearchFriendsActivity extends BaseActivity<SearchVM, ActivitySearchFriendsBinding> {

    private final Handler handler = new Handler();
    //为null 搜索好友 否则 搜索群成员
    private String groupId;
    private boolean isSearchGroupMember;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchFriendsBinding.inflate(getLayoutInflater()));
        sink();
        init();
        listener();
        initView();
    }

    private void initView() {
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter<Object, ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {
            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, Object data, int position) {
                GroupMembersInfo da = null;
                FriendInfo da2 = null;
                holder.view.select.setVisibility(View.GONE);
                if (isSearchGroupMember) {
                    da = (GroupMembersInfo) data;
                    holder.view.avatar.load(da.getFaceURL());
                    Common.stringBindForegroundColorSpan(holder.view.nickName, da.getNickname(), vm.searchContent.getValue());
                } else {
                    da2 = (FriendInfo) data;
                    holder.view.avatar.load(da2.getFaceURL());
                    Common.stringBindForegroundColorSpan(holder.view.nickName, da2.getNickname(), vm.searchContent.getValue());
                }

                final GroupMembersInfo finalDa = da;
                final FriendInfo finalDa1 = da2;
                holder.view.getRoot().setOnClickListener(v -> {
                    setResult(RESULT_OK, new Intent().putExtra(Constant.K_ID, isSearchGroupMember ? finalDa.getUserID()
                        : finalDa1.getUserID()).putExtra(Constant.K_RESULT,isSearchGroupMember ? GsonHel.toJson(finalDa)
                        :GsonHel.toJson(finalDa1)));
                    finish();
                });
            }
        });
        if (isSearchGroupMember)
            adapter.setItems(vm.groupMembersInfo.getValue());
        else
            adapter.setItems(vm.friendInfo.getValue());
    }

    void init() {
        groupId = getIntent().getStringExtra(Constant.K_GROUP_ID);
        isSearchGroupMember = !TextUtils.isEmpty(groupId);
    }

    private void listener() {
        view.cancel.setOnClickListener(v -> finish());
        vm.groupMembersInfo.observe(this, groupMembersInfos -> adapter.notifyDataSetChanged());
        vm.friendInfo.observe(this, friendInfos -> adapter.notifyDataSetChanged());


        vm.searchContent.observe(this, s -> {
            vm.groupMembersInfo.getValue()
                .clear();
            vm.friendInfo.getValue()
                .clear();
            adapter.notifyDataSetChanged();

            loadMore();
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty())
                    vm.searchContent.setValue("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String input = s.toString();
                    vm.page = 0;
                    vm.searchContent.setValue(input);
                }, 500);
            }
        });


        if (isSearchGroupMember) {
            view.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) view.recyclerview.getLayoutManager();
                    int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (lastVisiblePosition >= adapter.getItems().size() - 3) {
                        vm.page++;
                        loadMore();
                    }
                }
            });
        }

    }

    private void loadMore() {
        if (TextUtils.isEmpty(groupId))
            vm.searchFriendV2();
        else
            vm.searchGroupMemberByNickname(groupId, vm.searchContent.getValue());
    }
}
