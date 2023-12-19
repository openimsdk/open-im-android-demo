package io.openim.android.demo.ui.search;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivitySearchPersonBinding;
import io.openim.android.demo.databinding.LayoutSearchItemBinding;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.ex.CommEx;
import io.openim.android.ouicore.ex.Title;
import io.openim.android.ouicore.ex.User;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.RegexValid;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Main.SEARCH_CONVER)
public class SearchContactActivity extends BaseActivity<SearchVM, ActivitySearchPersonBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SearchVM searchVM = BaseApp.inst().getVMByCache(SearchVM.class);
        if (null == searchVM) {
            bindVM(SearchVM.class);
            vm.isPerson = true;
        } else bindVMByCache(SearchVM.class);

        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchPersonBinding.inflate(getLayoutInflater()));
        view.setSearchVM(vm);

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.sink);

        initView();
    }

    private void initView() {
        view.searchView.getEditText().setFocusable(true);
        view.searchView.getEditText().setFocusableInTouchMode(true);
        view.searchView.getEditText().requestFocus();
        view.searchView.getEditText().setHint(vm.isPerson ?
            io.openim.android.ouicore.R.string.search_by_id :
            io.openim.android.ouicore.R.string.search_group_by_id);
        view.searchView.getEditText().setOnKeyListener((v, keyCode, event) -> {
            String id;
            vm.searchContent.setValue(id = view.searchView.getEditText().getText().toString());
            if (vm.isPerson) vm.searchUser(vm.searchContent.getValue());
            else vm.searchGroup(id);
            return false;
        });


        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, vm.isPerson);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(recyclerViewAdapter);

        vm.userInfo.observe(this, v -> {
            String searchContent = vm.searchContent.val();
            if (searchContent.isEmpty() || null == v) return;
            List<Title> showData = new ArrayList<>();
            boolean isPhone = false, isUid = false;
            if (RegexValid.isAllNumber(searchContent)) {
                if (searchContent.length() == 11) isPhone = true;
                else isUid = true;
            }
            for (UserInfo userInfo : v) {
                Title title = new Title(userInfo.getUserID());
                if (isPhone) {
                    title.title = TextUtils.isEmpty(userInfo.getPhoneNumber())
                        ? searchContent :
                        userInfo.getPhoneNumber();
                } else if (isUid)
                    title.title = userInfo.getUserID();
                else title.title = userInfo.getNickname();

                showData.add(title);
            }
            bindDate(recyclerViewAdapter, showData);
        });
        vm.groupsInfo.observe(this, v -> {
            if (vm.searchContent.getValue().isEmpty()) return;
            List<Title> groupIds = new ArrayList<>();
            for (GroupInfo groupInfo : v) {
                Title title=new Title(groupInfo.getGroupID());
                title.title=groupInfo.getGroupID();
                groupIds.add(title);
            }
            bindDate(recyclerViewAdapter, groupIds);
        });

        view.cancel.setOnClickListener(v -> finish());
    }

    private void bindDate(RecyclerViewAdapter recyclerViewAdapter, List<Title> v) {

        if (null == v || v.isEmpty()) {
            view.notFind.setVisibility(View.VISIBLE);
            view.recyclerView.setVisibility(View.GONE);
        } else {
            view.notFind.setVisibility(View.GONE);
            view.recyclerView.setVisibility(View.VISIBLE);
            recyclerViewAdapter.setUserInfoList(v);
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.AViewHolder> {

        List<Title> titles = new ArrayList<>();
        Context context;
        boolean isPerson;

        public RecyclerViewAdapter(Context context, boolean isPerson) {
            this.context = context;
            this.isPerson = isPerson;
        }

        public void setUserInfoList(List<Title> titles) {
            this.titles = titles;
        }

        @NonNull
        @Override
        public AViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AViewHolder(LayoutSearchItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }


        @Override
        public void onBindViewHolder(@NonNull AViewHolder holder, int position) {
            Title title = titles.get(position);
            holder.view.userId.setText(":  " + title.title);

            holder.view.getRoot().setOnClickListener(v -> {
                if (isPerson)
                    context.startActivity(new Intent(context,
                        PersonDetailActivity.class).putExtra(Constant.K_ID, title.key));
                else
                    ARouter.getInstance().build(Routes.Group.DETAIL)
                        .withString(io.openim.android.ouicore.utils.Constant.K_GROUP_ID,
                            title.key).navigation();
            });
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        public static class AViewHolder extends RecyclerView.ViewHolder {
            public final LayoutSearchItemBinding view;

            public AViewHolder(LayoutSearchItemBinding viewBinding) {
                super(viewBinding.getRoot());
                view = viewBinding;
            }
        }
    }

}
