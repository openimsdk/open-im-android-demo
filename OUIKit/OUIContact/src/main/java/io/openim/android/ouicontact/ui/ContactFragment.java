package io.openim.android.ouicontact.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicontact.databinding.FragmentContactBinding;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SharedPreferencesUtil;

@Route(path = Routes.Contact.HOME)
public class ContactFragment extends BaseFragment<ContactVM> {
    private FragmentContactBinding view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        bindVM(ContactVM.class);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = FragmentContactBinding.inflate(getLayoutInflater());
        initView();
        click();
        return view.getRoot();
    }

    public ContactVM getVM() {
        return vm;
    }

    private void click() {
        view.groupNotice.setOnClickListener(v -> {
            vm.dotNum.setValue(0);
            SharedPreferencesUtil.remove(getContext(), Constant.K_GROUP_NUM);
            startActivity(new Intent(getActivity(), GroupNoticeListActivity.class));
        });

        view.newFriendNotice.setOnClickListener(v -> {
            vm.friendDotNum.setValue(0);
            SharedPreferencesUtil.remove(getContext(), Constant.K_FRIEND_NUM);
            startActivity(new Intent(getActivity(), NewFriendActivity.class));
        });

        view.myGoodFriend.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AllFriendActivity.class));
        });

        view.myGroup.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyGroupActivity.class));
        });

    }

    private void initView() {
        vm.dotNum.observe(getActivity(), v -> {
            view.badge.badge.setVisibility(v == 0 ? View.GONE : View.VISIBLE);
            view.badge.badge.setText(v + "");
        });
        vm.friendDotNum.observe(getActivity(), v -> {
            view.newFriendNoticeBadge.badge.setVisibility(v == 0 ? View.GONE : View.VISIBLE);
            view.newFriendNoticeBadge.badge.setText(v + "");
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
