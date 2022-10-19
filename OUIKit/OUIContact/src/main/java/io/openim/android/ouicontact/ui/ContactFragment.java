package io.openim.android.ouicontact.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.openim.android.ouicontact.databinding.ViewContactHeaderBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.adapter.RecyclerViewAdapter;
import io.openim.android.ouicore.adapter.ViewHol;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.databinding.ItemPsrsonSelectBinding;
import io.openim.android.ouicore.databinding.ViewRecyclerViewBinding;
import io.openim.android.ouicore.databinding.ViewSwipeRecyclerViewBinding;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;
import io.openim.android.ouicore.vm.ContactListVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.GroupMembersInfo;
import io.openim.android.sdk.models.UserInfo;

@Route(path = Routes.Contact.HOME)
public class ContactFragment extends BaseFragment<ContactVM> implements Observer {
    private ViewSwipeRecyclerViewBinding view;
    private ViewContactHeaderBinding header;
    private RecyclerViewAdapter adapter;
    private ContactListVM contactListVM;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(ContactVM.class);
        Obs.inst().addObserver(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = ViewSwipeRecyclerViewBinding.inflate(getLayoutInflater());
        header = ViewContactHeaderBinding.inflate(getLayoutInflater());

        initView();
        click();

        return view.getRoot();
    }

    public ContactVM getVM() {
        return vm;
    }

    private void click() {
        header.groupNotice.setOnClickListener(v -> {
            vm.dotNum.setValue(0);
            SharedPreferencesUtil.remove(getContext(), Constant.K_GROUP_NUM);
            startActivity(new Intent(getActivity(), GroupNoticeListActivity.class));
        });

        header.newFriendNotice.setOnClickListener(v -> {
            vm.friendDotNum.setValue(0);
            SharedPreferencesUtil.remove(getContext(), Constant.K_FRIEND_NUM);
            startActivity(new Intent(getActivity(), NewFriendActivity.class));
        });

        header.myGoodFriend.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AllFriendActivity.class));
        });

        header.myGroup.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyGroupActivity.class));
        });
    }


    private void initView() {
        vm.dotNum.observe(getActivity(), v -> {
            header.badge.badge.setVisibility(v == 0 ? View.GONE : View.VISIBLE);
            header.badge.badge.setText(v + "");
        });
        vm.friendDotNum.observe(getActivity(), v -> {
            header.newFriendNoticeBadge.badge.setVisibility(v == 0 ? View.GONE : View.VISIBLE);
            header.newFriendNoticeBadge.badge.setText(v + "");
        });


        view.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RecyclerViewAdapter<UserInfo, ViewHol.ItemViewHo>(ViewHol.ItemViewHo.class) {


            @Override
            public void onBindView(@NonNull ViewHol.ItemViewHo holder, UserInfo data, int position) {
                holder.view.avatar.load(data.getFaceURL());
                holder.view.nickName.setText(data.getNickname());
                holder.view.getRoot().setOnClickListener(v -> {

                });
            }
        };
        view.recyclerView.setAdapter(adapter);
        view.recyclerView.addHeaderView(header.getRoot());
    }

    private void bindData() {
        contactListVM = BaseApp.inst().getVMByCache(ContactListVM.class);
        if (null == contactListVM) return;
        adapter.setItems(contactListVM.frequentContacts.getValue());

        contactListVM.frequentContacts.observe(getActivity(), userInfos -> {
            if (userInfos.isEmpty()) return;
            adapter.setItems(userInfos);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Obs.inst().deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        Obs.Message message = (Obs.Message) arg;
        if (message.tag == Constant.Event.CONTACT_LIST_VM_INIT) {
            Common.UIHandler.postDelayed(this::bindData, 500);
        }
    }
}
