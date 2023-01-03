package io.openim.android.ouigroup.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivitySelectedMemberBinding;

public class SelectedMemberActivity extends BaseActivity<GroupVM, ActivitySelectedMemberBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivitySelectedMemberBinding.inflate(getLayoutInflater()));
        sink();
        initView();

    }

    private void initView() {
        view.submit.setOnClickListener(v -> finish());
        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(new RecyclerViewAdapter() {
            @Override
            public void onBindView(@NonNull RecyclerView.ViewHolder holder, Object data, int position) {

            }
        });

    }

}
