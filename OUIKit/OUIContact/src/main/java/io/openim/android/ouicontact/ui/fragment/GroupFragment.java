package io.openim.android.ouicontact.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.openim.android.ouicontact.ui.ForwardToActivity;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;
import io.openim.android.ouicore.entity.ExUserInfo;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.GroupInfo;

public class GroupFragment extends BaseFragment<SocialityVM> {
    private ForwardToActivity.ConfirmListener confirmListener;
    ViewRecyclerViewBinding view;
    private RecyclerViewAdapter<GroupInfo, ViewHol.GroupViewHo> adapter;

    public void setConfirmListener(ForwardToActivity.ConfirmListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    public static GroupFragment newInstance() {

        Bundle args = new Bundle();

        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        vm.getAllGroup();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = ViewRecyclerViewBinding.inflate(inflater);
        initView();
        listener();
        return view.getRoot();
    }

    private void initView() {
        view.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter<GroupInfo, ViewHol.GroupViewHo>(ViewHol.GroupViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHol.GroupViewHo holder, GroupInfo data, int position) {
                holder.view.avatar.load(data.getFaceURL());
                holder.view.title.setText(data.getGroupName());
                holder.view.description.setText(data.getMemberCount() + "人");

                holder.view.getRoot().setOnClickListener(v -> {
                    CommonDialog commonDialog = new CommonDialog(getContext());
                    commonDialog.getMainView().tips.setText("确认发送给：" + data.getGroupName());
                    commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
                    commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
                        if (null != confirmListener)
                            confirmListener.onListener(data.getGroupID());
                    });
                    commonDialog.show();
                });

            }
        };
        view.recyclerView.setAdapter(adapter);
    }

    private void listener() {
        vm.groups.observe(getActivity(), groupInfos -> {
            adapter.setItems(groupInfos);
        });
    }

}
