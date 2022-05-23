package io.openim.android.demo.ui.search;


import static io.openim.android.ouicontact.utils.Constant.K_USER_ID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivitySearchPersonBinding;
import io.openim.android.demo.databinding.LayoutSearchItemBinding;
import io.openim.android.demo.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.models.UserInfo;

public class SearchPersonActivity extends BaseActivity<SearchVM> {

    private ActivitySearchPersonBinding view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = ActivitySearchPersonBinding.inflate(getLayoutInflater());
        bindVM(SearchVM.class);
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.sink);
        setContentView(view.getRoot());
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {

        view.searchView.getEditText().setFocusable(true);
        view.searchView.getEditText().setFocusableInTouchMode(true);
        view.searchView.getEditText().requestFocus();
        view.searchView.getEditText().setHint(R.string.search_by_id);
        view.searchView.getEditText().setOnKeyListener((v, keyCode, event) -> {
            vm.searchContent = view.searchView.getEditText().getText().toString();
            vm.search();
            return false;
        });


        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(this);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(recyclerViewAdapter);

        vm.userInfo.observe(this, v -> {
            if (null == v || v.isEmpty()) {
                view.notFind.setVisibility(View.VISIBLE);
                view.recyclerView.setVisibility(View.GONE);
            } else {
                view.notFind.setVisibility(View.GONE);
                view.recyclerView.setVisibility(View.VISIBLE);
                recyclerViewAdapter.setUserInfoList(v);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });


        view.cancel.setOnClickListener(v -> finish());
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.AViewHolder> {

        List<UserInfo> userInfoList = new ArrayList<>();
        Context context;

        public RecyclerViewAdapter(Context context) {
            this.context = context;
        }

        public void setUserInfoList(List<UserInfo> userInfoList) {
            this.userInfoList = userInfoList;
        }

        @NonNull
        @Override
        public AViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AViewHolder(LayoutSearchItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }


        @Override
        public void onBindViewHolder(@NonNull AViewHolder holder, int position) {
            String userID = userInfoList.get(position).getUserID();
            holder.view.userId.setText(":  " + userID);

            holder.view.getRoot().setOnClickListener(v -> {
                context.startActivity(new Intent(context, PersonDetailActivity.class).putExtra(K_USER_ID, userID));
            });
        }

        @Override
        public int getItemCount() {
            return userInfoList.size();
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