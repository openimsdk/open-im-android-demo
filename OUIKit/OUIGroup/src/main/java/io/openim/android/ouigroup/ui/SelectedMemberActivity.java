package io.openim.android.ouigroup.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivitySelectedMemberBinding;

public class SelectedMemberActivity extends BaseActivity<GroupVM, ActivitySelectedMemberBinding> {

    private RecyclerViewAdapter<ExGroupMemberInfo, ViewHol.ItemViewHo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySelectedMemberBinding.inflate(getLayoutInflater()));
        sink();
        initView();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void initView() {
        view.submit.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<ExGroupMemberInfo, ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, ExGroupMemberInfo data, int position) {
                holder.view.avatar.load(data.groupMembersInfo.getFaceURL());
                holder.view.select.setVisibility(View.GONE);
                holder.view.nickName.setText(data.groupMembersInfo.getNickname());
                holder.view.menu.setVisibility(View.VISIBLE);
                holder.view.menu.setText(io.openim.android.ouicore.R.string.delete);
                holder.view.menu.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                holder.view.menu.setOnClickListener(v -> {
                    if (!data.isEnabled){
                        toast(getString(io.openim.android.ouicore.R.string.group_call_tips));
                        return;
                    }
                    int index = vm.superGroupMembers.getValue().indexOf(data);
                    vm.superGroupMembers.getValue().get(index).isSelect = false;
                    adapter.getItems().remove(data);
                    adapter.notifyItemRemoved(position);
                });

            }
        });
        List<ExGroupMemberInfo> exGroupMemberInfos = new ArrayList<>();
        for (ExGroupMemberInfo exGroupMemberInfo : vm.superGroupMembers.getValue()) {
            if (exGroupMemberInfo.isSelect) exGroupMemberInfos.add(exGroupMemberInfo);
        }
        adapter.setItems(exGroupMemberInfos);
    }

}
