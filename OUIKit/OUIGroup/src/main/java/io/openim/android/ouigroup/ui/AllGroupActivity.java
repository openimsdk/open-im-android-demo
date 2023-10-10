package io.openim.android.ouigroup.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.databinding.LayoutPopSelectedFriendsBinding;
import io.openim.android.ouicore.databinding.LayoutSelectedFriendsBinding;
import io.openim.android.ouicore.databinding.OftenRecyclerViewBinding;
import io.openim.android.ouicore.ex.MultipleChoice;
import io.openim.android.ouicore.vm.SelectTargetVM;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.sdk.models.GroupInfo;

public class AllGroupActivity extends BaseActivity<SocialityVM, OftenRecyclerViewBinding> {

    private LayoutSelectedFriendsBinding selectedFriendsBinding;
    private SelectTargetVM choiceVM;
    private  RecyclerViewAdapter<GroupInfo, ViewHol.ItemViewHo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(OftenRecyclerViewBinding.inflate(getLayoutInflater()));

        init();
        initView();
        listener();
    }

    private void listener() {
        vm.groups.observe(this,v->{
           if ( v.isEmpty())return;
           adapter.setItems(v);
        });
    }

    void init() {
        vm.getAllGroup();
        try {
            choiceVM = Easy.find(SelectTargetVM.class);
            choiceVM.metaData.observe(this,v->adapter.notifyDataSetChanged());
        }catch (Exception ignored){}
    }

    private void initView() {
        selectedFriendsBinding = LayoutSelectedFriendsBinding.inflate(getLayoutInflater());
        view.parent.addView(selectedFriendsBinding.getRoot());
        if (null!=choiceVM){
            choiceVM.bindDataToView(selectedFriendsBinding);
            choiceVM.showPopAllSelectFriends(selectedFriendsBinding,
                LayoutPopSelectedFriendsBinding.inflate(getLayoutInflater()));
            choiceVM.submitTap(selectedFriendsBinding.submit);
        }
        view.top.title.setText(io.openim.android.ouicore.R.string.group);

        view.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        view.recyclerView.setAdapter(adapter=new RecyclerViewAdapter<GroupInfo,ViewHol.ItemViewHo>
            (ViewHol.ItemViewHo.class) {

            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, GroupInfo data,
                                   int position) {

                holder.view.avatar.load(data.getFaceURL(), true);
                holder.view.nickName.setText(data.getGroupName());

                holder.view.select.setVisibility(View.VISIBLE);
                holder.view.select.setChecked(choiceVM.contains(new MultipleChoice(data.getGroupID())));
                holder.view.getRoot().setOnClickListener(v -> {
                    holder.view.select.setChecked(!holder.view.select.isChecked());

                    if (holder.view.select.isChecked()){
                        MultipleChoice meta=new MultipleChoice(data.getGroupID());
                        meta.isGroup=true;
                        meta.name=data.getGroupName();
                        meta.icon=data.getFaceURL();
                        choiceVM.metaData.val().add(meta);
                        choiceVM.metaData.update();
                    }else {
                        choiceVM.removeMetaData(data.getGroupID());
                    }
                });
            }
        });
    }
}
