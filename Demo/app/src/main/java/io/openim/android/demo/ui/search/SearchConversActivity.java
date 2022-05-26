package io.openim.android.demo.ui.search;


import static io.openim.android.ouicore.utils.Constant.ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivitySearchPersonBinding;
import io.openim.android.demo.databinding.LayoutSearchItemBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.models.GroupInfo;
import io.openim.android.sdk.models.UserInfo;

public class SearchConversActivity extends BaseActivity<SearchVM, ActivitySearchPersonBinding> {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(SearchVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySearchPersonBinding.inflate(getLayoutInflater()));

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.sink);

        initView();
    }

    private void initView() {
        view.searchView.getEditText().setFocusable(true);
        view.searchView.getEditText().setFocusableInTouchMode(true);
        view.searchView.getEditText().requestFocus();
        view.searchView.getEditText().setHint(vm.isPerson ? R.string.search_by_id : R.string.search_group_by_id);
        view.searchView.getEditText().setOnKeyListener((v, keyCode, event) -> {
            vm.searchContent = view.searchView.getEditText().getText().toString();
            vm.search();
            return false;
        });


        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this, vm.isPerson);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(recyclerViewAdapter);

        vm.userInfo.observe(this, v -> {
            if (vm.searchContent.isEmpty()) return;
            List<String> userIDs = new ArrayList<>();
            for (UserInfo userInfo : v) {
                userIDs.add(userInfo.getUserID());
            }
            bindDate(recyclerViewAdapter, userIDs);
        });
        vm.groupsInfo.observe(this, v -> {
            if (vm.searchContent.isEmpty()) return;
            List<String> groupIds = new ArrayList<>();
            for (GroupInfo groupInfo : v) {
                groupIds.add(groupInfo.getGroupID());
            }
            bindDate(recyclerViewAdapter, groupIds);
        });

        view.cancel.setOnClickListener(v -> finish());
    }

    private void bindDate(RecyclerViewAdapter recyclerViewAdapter, List<String> v) {

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

        List<String> titles = new ArrayList<>();
        Context context;
        boolean isPerson;

        public RecyclerViewAdapter(Context context, boolean isPerson) {
            this.context = context;
            this.isPerson = isPerson;
        }

        public void setUserInfoList(List<String> titles) {
            this.titles = titles;
        }

        @NonNull
        @Override
        public AViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AViewHolder(LayoutSearchItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }


        @Override
        public void onBindViewHolder(@NonNull AViewHolder holder, int position) {
            String title = titles.get(position);
            holder.view.userId.setText(":  " + title);

            holder.view.getRoot().setOnClickListener(v -> {
                if (isPerson)
                    context.startActivity(new Intent(context, PersonDetailActivity.class).putExtra(ID, title));
                else
                    ARouter.getInstance().build(Routes.Group.DETAIL)
                        .withString(io.openim.android.ouicore.utils.Constant.GROUP_ID, title).navigation();
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
