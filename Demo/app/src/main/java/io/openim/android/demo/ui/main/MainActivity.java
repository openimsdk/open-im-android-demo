package io.openim.android.demo.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentTransaction;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.igexin.sdk.PushManager;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.user.PersonalFragment;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicontact.ui.fragment.ContactFragment;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouiconversation.ui.fragment.ContactListFragment;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.MomentsBridge;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Routes;

@Route(path = Routes.Main.HOME)
public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment,
        personalFragment,appletFragment;
    private ActivityResultLauncher<Intent> resultLauncher = Common.getCaptureActivityLauncher(this);
    private boolean hasShoot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        runOnUiThread(() -> {
            hasShoot = AndPermission.hasPermissions(MainActivity.this, Permission.CAMERA,
                Permission.RECORD_AUDIO);
            Common.permission(MainActivity.this, () -> {
                hasShoot = true;
                AndPermission.with(this).overlay().start();
            }, hasShoot, Permission.CAMERA, Permission.RECORD_AUDIO);
        });

        PushManager.getInstance().initialize(this);
        bindVM(MainVM.class);
        vm.fromLogin = getIntent().getBooleanExtra(LoginActivity.FORM_LOGIN, false);
        bindViewDataBinding(ActivityMainBinding.inflate(getLayoutInflater()));
        super.onCreate(savedInstanceState);
        setLightStatus();

        view.setMainVM(vm);

        click();
        view.men1.setChecked(true);
    }


    private void bindDot() {
        ContactVM contactVM = ((ContactFragment) contactFragment).getVM();
        if (null == contactVM) return;
        contactVM.friendDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.groupDotNum.getValue() > 0) ?
                View.VISIBLE : View.GONE);
        });
        contactVM.groupDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.friendDotNum.getValue() > 0) ?
                View.VISIBLE : View.GONE);
        });
    }


    private void click() {
        view.menuGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.men1) switchFragment(conversationListFragment);
            if (checkedId == R.id.men2) switchFragment(contactFragment);
            if (checkedId == R.id.men3) switchFragment(appletFragment);
            if (checkedId == R.id.men4) switchFragment(personalFragment);
        });
    }


    @Override
    public void jump() {
        //token过期
        IMUtil.logout(this, LoginActivity.class);
    }

    @Override
    public void err(String msg) {

    }

    @Override
    public void succ(Object o) {

    }

    @Override
    public void initDate() {
        contactFragment =
            (ContactFragment) ARouter.getInstance().build(Routes.Contact.HOME).navigation();
        conversationListFragment =
            (ContactListFragment) ARouter.getInstance().build(Routes.Conversation.CONTACT_LIST).navigation();
        personalFragment = PersonalFragment.newInstance();

        appletFragment =
            (BaseFragment) ARouter.getInstance().build(Routes.Applet.HOME).navigation();

        personalFragment.setPage(4);
        switchFragment(personalFragment);

        if (null != appletFragment) {
            appletFragment.setPage(3);
            switchFragment(appletFragment);
        } else {
            view.men3.setVisibility(View.GONE);
        }
        if (null != contactFragment) {
            contactFragment.setPage(2);
            switchFragment(contactFragment);
        }
        if (null != conversationListFragment) {
            ((ContactListFragment) conversationListFragment).setResultLauncher(resultLauncher);
            conversationListFragment.setPage(1);
            switchFragment(conversationListFragment);
        }
        Common.UIHandler.postDelayed(this::bindDot, 500);
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
