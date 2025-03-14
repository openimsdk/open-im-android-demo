package io.openim.android.ouicontact.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.Observable;
import java.util.Observer;

import io.openim.android.ouicontact.databinding.ActivityMyGroupBinding;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.sdk.models.GroupInfo;

public class MyGroupActivity extends BaseActivity<SocialityVM, ActivityMyGroupBinding> implements Observer {

    private RecyclerViewAdapter<GroupInfo, ViewHol.GroupViewHo> joinedAdapter, createAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        Obs.inst().addObserver(this);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMyGroupBinding.inflate(getLayoutInflater()));
        sink();
        initView();
        listener();
        vm.getAllGroup();
    }

    private void initView() {
        view.create.setLayoutManager(new LinearLayoutManager(this));
        view.joined.setLayoutManager(new LinearLayoutManager(this));

        joinedAdapter = new ContentAdapter(ViewHol.GroupViewHo.class);
        view.joined.setAdapter(joinedAdapter);

        createAdapter = new ContentAdapter(ViewHol.GroupViewHo.class);
        view.create.setAdapter(createAdapter);
    }

    private void listener() {
        vm.groups.observe(this, groupInfos -> {
            joinedAdapter.setItems(groupInfos);
        });
        vm.ownGroups.observe(this, groupInfos -> {
            createAdapter.setItems(groupInfos);
        });

        view.menuGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == view.men1.getId()) {
                view.menBg1.setVisibility(View.VISIBLE);
                view.menBg2.setVisibility(View.GONE);
                view.create.setVisibility(View.VISIBLE);
                view.joined.setVisibility(View.GONE);
            } else {
                view.menBg2.setVisibility(View.VISIBLE);
                view.menBg1.setVisibility(View.GONE);

                view.create.setVisibility(View.GONE);
                view.joined.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        Obs.Message message = (Obs.Message) arg;
        if (message.tag == Constants.Event.DISSOLVE_GROUP) {
            vm.groups.getValue().clear();
            vm.ownGroups.getValue().clear();
            vm.getAllGroup();
        }
    }

    public static class ContentAdapter extends RecyclerViewAdapter<GroupInfo, ViewHol.GroupViewHo> {

        public ContentAdapter(Class<ViewHol.GroupViewHo> viewHolder) {
            super(viewHolder);
        }

        @Override
        public void onBindView(@NonNull ViewHol.GroupViewHo holder, GroupInfo data, int position) {
            holder.view.avatar.load(data.getFaceURL(), true);
            holder.view.title.setText(data.getGroupName());
            holder.view.description.setText(data.getMemberCount() + "人");

            holder.view.getRoot().setOnClickListener(v -> {
                ARouter.getInstance().build(Routes.Conversation.CHAT).withString(Constants.K_GROUP_ID, data.getGroupID()).withString(Constants.K_NAME, data.getGroupName()).navigation();
            });
        }
    }

    @Override
    protected void fasterDestroy() {
        super.fasterDestroy();
        Obs.inst().deleteObserver(this);
    }
}
