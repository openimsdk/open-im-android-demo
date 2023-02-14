package io.openim.android.ouimoments.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouimoments.R;
import io.openim.android.ouimoments.adapter.viewholder.MsgDetailViewHolder;
import io.openim.android.ouimoments.databinding.ActivityMsgDetailBinding;
import io.openim.android.ouimoments.mvp.contract.CircleContract;
import io.openim.android.ouimoments.mvp.presenter.MsgDetailVM;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.models.WorkMomentsInfo;

public class MsgDetailActivity extends  BaseActivity<MsgDetailVM, ActivityMsgDetailBinding>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MsgDetailVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMsgDetailBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
    }

    private void listener() {
        vm.workMomentsInfo.observe(this, workMomentsInfos -> {
            L.e("");
        });
    }

    private void initView() {
        vm.getWorkMomentsNotification();

        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(new RecyclerViewAdapter<WorkMomentsInfo, MsgDetailViewHolder>
            (MsgDetailViewHolder.class) {

            @Override
            public void onBindView(@NonNull MsgDetailViewHolder holder, WorkMomentsInfo data,
                                   int position) {
                holder.view.avatar.load(data.getFaceURL());
                holder.view.nickName.setText(data.getUserName());
                holder.view.action.setText(data.getUserName());
            }
        });
    }


}
