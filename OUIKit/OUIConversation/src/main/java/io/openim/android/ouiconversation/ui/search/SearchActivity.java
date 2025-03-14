package io.openim.android.ouiconversation.ui.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openim.android.ouiconversation.databinding.ActivitySearchBinding;
import io.openim.android.ouiconversation.ui.ChatActivity;
import io.openim.android.ouiconversation.vm.ChatVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.NotificationMsg;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.GetFilePathFromUri;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.sdk.enums.ConversationType;
import io.openim.android.sdk.enums.MessageType;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.NotificationElem;
import io.openim.android.sdk.models.SearchResultItem;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Conversation.SEARCH)
public class SearchActivity extends BaseActivity<SearchVM, ActivitySearchBinding> {
    private final Handler handler = new Handler();
    private static final int DIVISION = -1;
    private static final int TITLE = 0;
    private static final int USER = 1;
    private static final int GROUP = 2;

    private RecyclerViewAdapter adapter;
    //当前选中的tab
    private int selectTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchBinding.inflate(getLayoutInflater()));
        sink();

        init();
    }

    private void init() {
        initView();
        view.cancel.setOnClickListener(v -> finish());
    }

    private void listener() {
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
                if (s.toString().isEmpty())
                    vm.searchContent.setValue("");
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
        vm.userInfo.observe(this, friendInfos -> {
            addNape(BaseApp.inst().getString(io.openim.android.ouicore.R.string.contact), friendInfos);
        });
        vm.groupsInfo.observe(this, groupInfos -> {
            addNape(BaseApp.inst().getString(io.openim.android.ouicore.R.string.group), groupInfos);
        });
    }

    private void clearData() {
        vm.clearData();
        adapter.getItems().clear();
        adapter.notifyDataSetChanged();
    }

    private void addMoreNape(List data) {
        if (data.isEmpty()) return;
        int start = adapter.getItems().size();
        adapter.getItems().addAll(data);
        adapter.notifyItemRangeInserted(start, adapter.getItems().size());
    }

    private void addNape(String title, List data) {
        if (adapter.getItems().isEmpty() && data.isEmpty() && !TextUtils.isEmpty(vm.searchContent.val())) {
            view.notFind.setVisibility(View.VISIBLE);
            view.recyclerview.setVisibility(View.GONE);
            return;
        }
        if (data.isEmpty()) return;
        view.notFind.setVisibility(View.GONE);
        view.recyclerview.setVisibility(View.VISIBLE);
        if (selectTabIndex != 0) {
            //没有选择综合tab 我们只渲染单一项
            addMoreNape(data);
            return;
        }
        int maxShowNum = 5; //最大显示5条
        boolean hasMore;
        int start = adapter.getItems().size();
        adapter.getItems().add(title + ((hasMore = data.size() > maxShowNum) ? 1 : ""));//包含1表示有查看更多
        adapter.getItems().addAll(hasMore ? data.subList(0, maxShowNum) : data);
        adapter.getItems().add(-1); //表示分割线
        adapter.notifyItemRangeInserted(start, adapter.getItems().size());
    }

    private void initView() {
        view.searchView.getEditText().requestFocus();
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter() {

            @Override
            public int getItemViewType(int position) {
                Object o = getItems().get(position);
                if (o instanceof String)
                    return TITLE;
                if (o instanceof UserInfo)
                    return USER;
                if (o instanceof GroupInfo)
                    return GROUP;
                return DIVISION;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                              int viewType) {
                if (viewType == TITLE)
                    return new ViewHol.TitleViewHolder(parent);
                if (viewType == USER || viewType == GROUP)
                    return new ViewHol.ContactItemHolder(parent);
                return new ViewHol.DivisionItemViewHo(parent, Common.dp2px(10));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data,
                                   int position) {
                switch (getItemViewType(position)) {
                    case TITLE:
                        String title = (String) data;
                        ViewHol.TitleViewHolder titleViewHolder = (ViewHol.TitleViewHolder) holder;
                        if (title.contains("1")) {
                            titleViewHolder.view.title.setText(title.substring(0, title.indexOf(
                                "1")));
                            titleViewHolder.view.more.setVisibility(View.VISIBLE);
                        } else {
                            titleViewHolder.view.more.setVisibility(View.GONE);
                            titleViewHolder.view.title.setText(title);
                        }
                        break;
                    case USER:
                    case GROUP:
                        ViewHol.ContactItemHolder contactItemHolder =
                            (ViewHol.ContactItemHolder) holder;
                        contactItemHolder.viewBinding.bottom.setVisibility(View.VISIBLE);
                        contactItemHolder.viewBinding.expand.setVisibility(View.VISIBLE);
                        if (data instanceof UserInfo) {
                            //联系人
                            UserInfo da = (UserInfo) data;
                            contactItemHolder.viewBinding.avatar.load(da.getFaceURL());
                            Common.stringBindForegroundColorSpan(contactItemHolder.viewBinding.nickName, da.getNickname(), vm.searchContent.getValue());
                            if (TextUtils.isEmpty(da.getRemark()))
                                contactItemHolder.viewBinding.bottom.setVisibility(View.GONE);
                            else {
                                contactItemHolder.viewBinding.lastMsg.setText(getString(io.openim.android.ouicore.R.string.remark) + ":"
                                    + da.getRemark());
                            }
                            contactItemHolder.viewBinding.getRoot().setOnClickListener(v -> {
                                ARouter.getInstance().build(Routes.Main.PERSON_DETAIL)
                                    .withString(Constants.K_ID, da.getUserID()).navigation(SearchActivity.this);
                            });
                        }
                        if ((data instanceof GroupInfo)) {
                            //群组
                            contactItemHolder.viewBinding.bottom.setVisibility(View.GONE);
                            contactItemHolder.viewBinding.expand.setVisibility(View.GONE);
                            GroupInfo groupInfo = (GroupInfo) data;
                            contactItemHolder.viewBinding.avatar.load(groupInfo.getFaceURL());
                            Common.stringBindForegroundColorSpan(contactItemHolder.viewBinding.nickName, groupInfo.getGroupName(), vm.searchContent.getValue());
                            contactItemHolder.viewBinding.getRoot().setOnClickListener(v -> {
                                BaseApp.inst().removeCacheVM(ChatVM.class);
                                startActivity(new Intent(SearchActivity.this,
                                    ChatActivity.class).putExtra(Constants.K_GROUP_ID,
                                    groupInfo.getGroupID()));
                            });
                        }
                        break;
                }
            }


        });
        adapter.setItems(new ArrayList());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            BaseApp.inst().removeCacheVM(ChatVM.class);
        }
    }
}
