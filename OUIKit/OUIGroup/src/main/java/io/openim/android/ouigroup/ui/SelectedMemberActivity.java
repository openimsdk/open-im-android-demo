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
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.entity.ExGroupMemberInfo;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.vm.GroupMemberVM;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivitySelectedMemberBinding;

public class SelectedMemberActivity extends BasicActivity<ActivitySelectedMemberBinding> {

    private RecyclerViewAdapter<MultipleChoice, ViewHol.ItemViewHo> adapter;
    private GroupMemberVM vm;
    private boolean isUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vm = Easy.find(GroupMemberVM.class);
        super.onCreate(savedInstanceState);
        viewBinding(ActivitySelectedMemberBinding.inflate(getLayoutInflater()));
        initView();
    }


    private void initView() {
        view.submit.setOnClickListener(v -> {
            if (isUpdate)
                vm.choiceList.update();
            finish();
        });
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter = new RecyclerViewAdapter<MultipleChoice,
            ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, MultipleChoice data,
                                   int position) {
                holder.view.getRoot().setIntercept(!data.isEnabled);
                holder.view.getRoot().setAlpha(data.isEnabled?1f:0.3f);
                holder.view.avatar.load(data.icon);
                holder.view.select.setVisibility(View.GONE);
                holder.view.nickName.setText(data.name);
                holder.view.menu.setVisibility(View.VISIBLE);
                holder.view.menu.setText(io.openim.android.ouicore.R.string.delete);
                holder.view.menu.setTextColor(getResources()
                    .getColor(android.R.color.holo_red_dark));
                holder.view.menu.setOnClickListener(v -> {
                    isUpdate=true;
                    vm.removeChoice(data.key);
                    adapter.notifyDataSetChanged();
                });

            }
        });
       runOnUiThread(() -> adapter.setItems(vm.choiceList.val()));
    }
}
