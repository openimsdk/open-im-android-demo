package io.openim.android.ouicontact.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.openim.android.ouicontact.R;
import io.openim.android.ouicontact.databinding.ActivityLabelBinding;
import io.openim.android.ouicontact.vm.LabelVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouicore.entity.UserLabel;
import io.openim.android.ouicore.entity.UserLabelChild;
import io.openim.android.ouicore.widget.SyLinearLayoutManager;

public class LabelActivity extends BaseActivity<LabelVM, ActivityLabelBinding> {

    private RecyclerViewAdapter<UserLabel, ViewHol.LabelItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(LabelVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityLabelBinding.inflate(getLayoutInflater()));
        sink();

        init();
        listener();
    }

    private void listener() {
        vm.userLabels.observe(this, userLabels -> {
            adapter.setItems(userLabels);
        });
    }

    void init(){
        view.recyclerView.setLayoutManager(new SyLinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter=new RecyclerViewAdapter<UserLabel, ViewHol.LabelItem>(ViewHol.LabelItem.class) {

            @Override
            public void onBindView(@NonNull ViewHol.LabelItem holder, UserLabel data,
                                   int position) {
                holder.view.name.setText(data.getTagName());
                StringBuilder member = new StringBuilder();
                for (UserLabelChild userLabelChild : data.getUserList()) {
                    member.append(userLabelChild.getUserName());
                    member.append("、");
                }
                holder.view.content.setText(member.substring(0,member.length()));
            }
        });
        vm.getUserTags();
    }
}

