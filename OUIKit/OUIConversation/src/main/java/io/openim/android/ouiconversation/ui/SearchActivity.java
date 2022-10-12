package io.openim.android.ouiconversation.ui;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouiconversation.R;
import io.openim.android.ouiconversation.databinding.ActivitySearchBinding;
import io.openim.android.ouiconversation.databinding.ItemSearchTitleBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.databinding.ViewImageBinding;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.sdk.models.FriendInfo;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.SearchResultItem;

public class SearchActivity extends BaseActivity<SearchVM, ActivitySearchBinding> {
    private final String[] tabTitles = new String[]{
        BaseApp.inst().getString(io.openim.android.ouicore.R.string.synthesis),
        BaseApp.inst().getString(io.openim.android.ouicore.R.string.contact),
        BaseApp.inst().getString(io.openim.android.ouicore.R.string.group),
        BaseApp.inst().getString(io.openim.android.ouicore.R.string.chat_history2),
        BaseApp.inst().getString(io.openim.android.ouicore.R.string.file),
    };
    private final Handler handler = new Handler();

    //item type
    private static final int TITLE = 1;
    private static final int CONTACT_ITEM = 2;
    private static final int GROUP_ITEM = 3;
    private static final int CHAT_ITEM = 4;
    private static final int FILE_ITEM = 5;
    private static final int DIVISION = 6;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchBinding.inflate(getLayoutInflater()));
        sink();

        initView();
        listener();
    }

    private void listener() {
        vm.searchContent.observe(this, s -> {
            if (s.isEmpty()) {
                vm.clearData();
                return;
            }
            vm.searchFriendV2();
            vm.searchGroupV2();
            vm.searchLocalMessages(s);
            vm.searchLocalMessages(s, Constant.MsgType.FILE);
        });

        view.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

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
        view.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        vm.friendInfo.observe(this, friendInfos -> {
            if (friendInfos.isEmpty()) return;
            L.e("");

        });
    }

    private void initView() {
        for (String title : tabTitles) {
            TabLayout.Tab tab = view.tabLayout.newTab();
            tab.setText(title);
            view.tabLayout.addTab(tab);
        }
        view.searchView.getEditText().requestFocus();
        view.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerview.setAdapter(adapter = new RecyclerViewAdapter() {

            @Override
            public int getItemViewType(int position) {
                Object o = getItems().get(position);
                if (o instanceof String)
                    return TITLE;
                if (o instanceof FriendInfo)
                    return CONTACT_ITEM;
                if (o instanceof GroupInfo)
                    return GROUP_ITEM;
                if (o instanceof SearchResultItem) {
                    if (((SearchResultItem) o).getMessageList().get(0).getContentType()
                        == Constant.MsgType.FILE)
                        return FILE_ITEM;
                    else
                        return CHAT_ITEM;
                }
                //-1 表示分割线
                return DIVISION;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == TITLE)
                    return new TitleViewHolder(parent);
                if (viewType == CONTACT_ITEM || viewType == CHAT_ITEM)
                    return new ViewHol.ContactItemHolder(parent);
                if (viewType == GROUP_ITEM)
                    return new ViewHol.ItemViewHo(parent);
                if (viewType == FILE_ITEM)
                    return new ViewHol.FileItemViewHo(parent);

                return new ViewHol.DivisionItemViewHo(parent, Common.dp2px(10));
            }

            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data, int position) {
                switch (getItemViewType(position)) {
                    case TITLE:
                        TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
                        titleViewHolder.view.title.setText((String) data);
                        break;
                    case CONTACT_ITEM:
                    case CHAT_ITEM:
                        ViewHol.ContactItemHolder contactItemHolder = (ViewHol.ContactItemHolder) holder;
                        if (data instanceof FriendInfo) {
                            //联系人
                            FriendInfo da = (FriendInfo) data;
                            contactItemHolder.viewBinding.avatar.load(da.getFaceURL());
                            spannableStringBind(contactItemHolder.viewBinding.nickName, da.getNickname());
                            contactItemHolder.viewBinding.bottom.setVisibility(View.GONE);
                        } else {
                            //聊天记录
                            SearchResultItem da = (SearchResultItem) data;
                            contactItemHolder.viewBinding.avatar.load(da.getFaceURL());
                            contactItemHolder.viewBinding.nickName.setText(da.getShowName());
                            contactItemHolder.viewBinding.lastMsg.setText(
                                String.format(getString(io.openim.android.ouicore.R.string.two_related_chats), da.getMessageCount() + ""));
                        }
                        break;
                    case GROUP_ITEM:
                        GroupInfo groupInfo = (GroupInfo) data;
                        ViewHol.ItemViewHo itemViewHo = (ViewHol.ItemViewHo) holder;
                        itemViewHo.view.avatar.load(groupInfo.getFaceURL());
                        spannableStringBind(itemViewHo.view.nickName, groupInfo.getGroupName());
                        break;
                    case  FILE_ITEM:
                        ViewHol.FileItemViewHo fileItemViewHo= (ViewHol.FileItemViewHo) data;

                        break;
                }
            }

            private void spannableStringBind(TextView textView, String data) {
                SpannableStringBuilder spannableString = new SpannableStringBuilder(data);
                int start;
                String searchContent = vm.searchContent.getValue();
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#009ad6"));
                spannableString.setSpan(colorSpan, start = data.indexOf(searchContent),
                    start + searchContent.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(spannableString);
            }
        });
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        public ItemSearchTitleBinding view;

        public TitleViewHolder(@NonNull View itemView) {
            super(ItemSearchTitleBinding.inflate(LayoutInflater.from(itemView.getContext()), (ViewGroup) itemView, false).getRoot());
            view = ItemSearchTitleBinding.bind(this.itemView);
        }
    }
}
