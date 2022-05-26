package io.openim.android.ouicontact.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityGroupNoticeListBinding;
import io.openim.android.ouicontact.databinding.ItemGroupNoticeBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.L;
import io.openim.android.sdk.models.GroupApplicationInfo;

public class GroupNoticeListActivity extends BaseActivity<ContactVM, ActivityGroupNoticeListBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(ContactVM.class,true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupNoticeListBinding.inflate(getLayoutInflater()));

        initView();
        vm.getRecvGroupApplicationList();
    }

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter<GroupApplicationInfo, RecyclerViewItem>(RecyclerViewItem.class) {
            @Override
            public void onBindView(@NonNull RecyclerViewItem holder, GroupApplicationInfo data, int position) {
                holder.v.avatar.load(data.getUserFaceURL());
                holder.v.nickName.setText(data.getNickname());
                holder.v.groupName.setText("  " + data.getGroupName());
                holder.v.hil.setText(getString(io.openim.android.ouicore.R.string.reason) + "\n" + data.getReqMsg());

                holder.v.getRoot().setOnClickListener(null);
                holder.v.handle.setBackgroundColor(getResources().getColor(android.R.color.white));
                if(data.getHandleResult()==0){
                    holder.v.handle.setBackgroundResource(io.openim.android.ouicore.R.drawable.sty_radius_3_stroke_418ae5);
                    holder.v.handle.setText(getString(io.openim.android.ouicore.R.string.agree));

                    holder.v.getRoot().setOnClickListener(v -> {
                        vm.applyDetail.setValue(data);
                        startActivity(new Intent(GroupNoticeListActivity.this,GroupNoticeDetailActivity.class));
                    });
                }else if (data.getHandleResult()==-1){
                    holder.v.handle.setText(getString(io.openim.android.ouicore.R.string.rejected));
                    holder.v.handle.setTextColor(Color.parseColor("#999999"));
                }else {
                    holder.v.handle.setText(getString(io.openim.android.ouicore.R.string.agreeed));
                    holder.v.handle.setTextColor(Color.parseColor("#999999"));
                }
            }
        };
        view.recyclerView.setAdapter(recyclerViewAdapter);

        vm.groupApply.observe(this, v -> recyclerViewAdapter.setItems(v));
    }

    public static class RecyclerViewItem extends RecyclerView.ViewHolder {
        public ItemGroupNoticeBinding v;

        public RecyclerViewItem(@NonNull View parent) {
            super((ItemGroupNoticeBinding.inflate(LayoutInflater.from(parent.getContext()))).getRoot());
            v = ItemGroupNoticeBinding.bind(itemView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }
}
