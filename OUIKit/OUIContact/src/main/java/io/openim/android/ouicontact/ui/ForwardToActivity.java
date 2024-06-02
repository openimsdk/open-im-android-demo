package io.openim.android.ouicontact.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicontact.databinding.ActivityForwardToBinding;
import io.openim.android.ouicontact.ui.fragment.FriendFragment;
import io.openim.android.ouicontact.ui.fragment.GroupFragment;
import io.openim.android.ouicontact.ui.search.SearchGroupAndFriendsActivity;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SocialityVM;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.sdk.models.UserInfo;


@Route(path = Routes.Contact.FORWARD)
public class ForwardToActivity extends BaseActivity<SocialityVM, ActivityForwardToBinding> {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, friendFragment, groupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SocialityVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityForwardToBinding.inflate(getLayoutInflater()));
        sink();
        vm.getAllFriend();

        initView();
        listener();
    }
    private final ActivityResultLauncher<Intent> launcher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), v->{
        if (v.getResultCode()!=RESULT_OK)return;

        CommonDialog commonDialog =
            new CommonDialog(this);
        commonDialog.getMainView().tips.setText(getString(io.openim.android.ouicore.R.string.confirm_send_who)
            + v.getData().getStringExtra(Constants.K_NAME));
        commonDialog.getMainView().cancel.setOnClickListener(v1 -> commonDialog.dismiss());
        commonDialog.getMainView().confirm.setOnClickListener(v1 -> {
            setResult(RESULT_OK, v.getData());
            finish();
        });
        commonDialog.show();
    });

    private void listener() {
        view.searchView.setOnClickListener(v -> {
            launcher.launch(new Intent(this, SearchGroupAndFriendsActivity.class)
                .putExtra(Constants.IS_SELECT_FRIEND,true));
        });
        view.menuGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == view.men1.getId()) {
                view.menBg1.setVisibility(View.VISIBLE);
                view.menBg2.setVisibility(View.GONE);
                switchFragment(friendFragment);
            } else {
                view.menBg2.setVisibility(View.VISIBLE);
                view.menBg1.setVisibility(View.GONE);
                switchFragment(groupFragment);
            }
        });

    }

    private void initView() {
        groupFragment = GroupFragment.newInstance();
        groupFragment.setPage(2);

        friendFragment = FriendFragment.newInstance();
        friendFragment.setPage(1);
        switchFragment(friendFragment);


        ((FriendFragment) friendFragment).setConfirmListener((userInfo, id) -> {
            setResult(RESULT_OK, new Intent().putExtra(Constants.K_ID, id)
                .putExtra(Constants.K_NAME, userInfo.getNickname()));
            finish();
        });
        ((GroupFragment) groupFragment).setConfirmListener((userInfo, id) -> {
            setResult(RESULT_OK, new Intent().putExtra(Constants.K_GROUP_ID, id));
            finish();
        });
    }


    private void switchFragment(BaseFragment fragment) {
        try {
            if (fragment != null && !fragment.isVisible() && mCurrentTabIndex != fragment.getPage()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    transaction.add(view.fragmentContainer.getId(), fragment);
                }
                if (lastFragment != null) {
                    transaction.hide(lastFragment);
                }
                transaction.show(fragment).commit();
                lastFragment = fragment;
                mCurrentTabIndex = lastFragment.getPage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface ConfirmListener {
        void onListener(UserInfo userInfo, String id);
    }
}
