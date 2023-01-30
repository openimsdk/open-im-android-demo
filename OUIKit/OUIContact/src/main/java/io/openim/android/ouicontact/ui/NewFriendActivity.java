package io.openim.android.ouicontact.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityNewFriendBinding;
import io.openim.android.ouicontact.databinding.ItemFriendNoticeBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.sdk.models.FriendApplicationInfo;

public class NewFriendActivity extends BaseActivity<ContactVM, ActivityNewFriendBinding> {
    RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(ContactVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityNewFriendBinding.inflate(getLayoutInflater()));
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());

        vm.getRecvFriendApplicationList();
        initView();
        listener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }

    private void initView() {
        view.searchView.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Main.SEARCH_CONVER).navigation();
        });
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new RecyclerViewAdapter<FriendApplicationInfo, ViewHo>(ViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHo holder, FriendApplicationInfo data, int position) {
                holder.view.avatar.load(data.getFromFaceURL());
                holder.view.nickName.setText(data.getFromNickname());
                holder.view.hil.setText(data.getReqMsg());
                holder.view.handle.setBackgroundColor(getResources().getColor(android.R.color.white));

                if (data.getHandleResult() == 0) {
                    holder.view.handle.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_3_stroke_418ae5);
                    holder.view.handle.setText(getString(io.openim.android.ouicore.R.string.accept));

                    holder.view.getRoot().setOnClickListener(v -> {
                        vm.friendDetail.setValue(data);
                        startActivity(new Intent(NewFriendActivity.this, FriendRequestDetailActivity.class));
                    });
                } else if (data.getHandleResult() == -1) {
                    holder.view.getRoot().setOnClickListener(null);
                    holder.view.handle.setText(getString(io.openim.android.ouicore.R.string.rejected));
                    holder.view.handle.setTextColor(Color.parseColor("#999999"));
                } else {
                    holder.view.handle.setText(getString(io.openim.android.ouicore.R.string.hil));
                    holder.view.getRoot().setOnClickListener(v -> ARouter.getInstance().build(Routes.Conversation.CHAT)
                        .withString(Constant.K_ID, data.getFromUserID())
                        .withString(Constant.K_NAME, data.getFromNickname())
                        .navigation());
                }

            }
        };
        view.recyclerView.setAdapter(adapter);
    }

    private void listener() {
        vm.friendApply.observe(this, v -> {
            adapter.setItems(v);
        });
    }


    public void toBack(View v) {
        finish();
    }


    public static class ViewHo extends RecyclerView.ViewHolder {
        public ItemFriendNoticeBinding view;

        public ViewHo(@NonNull View parent) {
            super((ItemFriendNoticeBinding.inflate(LayoutInflater.from(parent.getContext()), (ViewGroup) parent, false).getRoot()));
            view = ItemFriendNoticeBinding.bind(this.itemView);
        }
    }
}
