package io.openim.android.demo.ui.main;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import androidx.fragment.app.FragmentTransaction;


import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.databinding.LayoutAddActionBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.search.AddConversActivity;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;

public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MainVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMainBinding.inflate(getLayoutInflater()));

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());

        view.setMainVM(vm);

        vm.visibility.observe(this, v -> view.isOnline.setVisibility(v));
        click();
    }

    private void click() {
        showPopupWindow();
        view.menuGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.men1)
                    switchFragment(conversationListFragment);
                if (checkedId == R.id.men2)
                    switchFragment(contactFragment);
            }
        });
    }

    private void showPopupWindow() {
        view.addFriend.setOnClickListener(v -> {
            //初始化一个PopupWindow，width和height都是WRAP_CONTENT
            PopupWindow popupWindow = new PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutAddActionBinding view = LayoutAddActionBinding.inflate(getLayoutInflater());
            view.addFriend.setOnClickListener(c -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, AddConversActivity.class));
            });
            view.addGroup.setOnClickListener(c -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, AddConversActivity.class)
                    .putExtra(AddConversActivity.IS_PERSON, false));
            });
            //设置PopupWindow的视图内容
            popupWindow.setContentView(view.getRoot());
            //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
            popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            popupWindow.setOutsideTouchable(true);

            //设置PopupWindow消失监听
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                }
            });
            //PopupWindow在targetView下方弹出
            popupWindow.showAsDropDown(v);

        });
    }


    @Override
    public void jump() {
        //token过期
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void err(String msg) {

    }

    @Override
    public void succ(Object o) {

    }

    @Override
    public void initDate() {
        conversationListFragment = (BaseFragment) ARouter.getInstance().build(Routes.Conversation.CONTACT_LIST).navigation();
        contactFragment = (BaseFragment) ARouter.getInstance().build(Routes.Contact.HOME).navigation();

        if (null != contactFragment) {
            contactFragment.setPage(2);
            switchFragment(contactFragment);
        }

        if (null != conversationListFragment) {
            conversationListFragment.setPage(1);
            switchFragment(conversationListFragment);
        }

//        getSupportFragmentManager().beginTransaction()
//            .add(R.id.fragment_container, contactListFragment).commit();
    }


    /**
     * 切换Fragment
     */
    private void switchFragment(BaseFragment fragment) {
        try {
            if (fragment != null && !fragment.isVisible() && mCurrentTabIndex != fragment.getPage()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    transaction.add(R.id.fragment_container, fragment);
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
}
