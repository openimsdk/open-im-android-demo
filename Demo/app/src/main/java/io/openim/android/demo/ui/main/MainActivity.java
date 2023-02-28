package io.openim.android.demo.ui.main;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.igexin.sdk.PushManager;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.search.AddConversActivity;
import io.openim.android.demo.ui.search.PersonDetailActivity;
import io.openim.android.demo.ui.user.PersonalFragment;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicalling.service.AudioVideoService;
import io.openim.android.ouicontact.ui.ContactFragment;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouiconversation.ui.ContactListFragment;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.MomentsBridge;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.SystemBarUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;

@Route(path = Routes.Main.HOME)
public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment,
        personalFragment, circleFragment;
    private ActivityResultLauncher<Intent> resultLauncher = Common.getCaptureActivityLauncher(this);
    private boolean hasShoot = false;
    private MomentsBridge momentsBridge;

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

        vm.visibility.observe(this, v -> {
            View view = findViewById(io.openim.android.ouiconversation.R.id.isOnline);
            if (null != view) {
                view.setVisibility(v);
            }
        });

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
            if (checkedId == R.id.men3) switchFragment(circleFragment);
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
        momentsBridge =
            (MomentsBridge) ARouter.getInstance().build(Routes.Service.MOMENTS).navigation();
        personalFragment.setPage(4);
        switchFragment(personalFragment);

        if (null != momentsBridge) {
            circleFragment = momentsBridge.buildMomentsFragment();
            circleFragment.setPage(3);
            switchFragment(circleFragment);
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (null == momentsBridge) return super.onKeyDown(keyCode, event);
        if (momentsBridge.onKeyDown(keyCode, event)) return true;
        else return super.onKeyDown(keyCode, event);
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
