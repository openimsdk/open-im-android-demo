package io.openim.android.demo.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;


import com.alibaba.android.arouter.core.LogisticsCenter;
import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;
import com.hjq.window.EasyWindow;
import com.igexin.sdk.PushManager;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.ui.ServerConfigActivity;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.user.PersonalFragment;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicontact.ui.fragment.ContactFragment;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouiconversation.ui.fragment.ConversationListFragment;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;

@Route(path = Routes.Main.HOME)
public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment,
        personalFragment, appletFragment;
    private ActivityResultLauncher<Intent> resultLauncher = Common.getCaptureActivityLauncher(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init(getIntent());

        PushManager.getInstance().initialize(this);
        bindVM(MainVM.class);
        vm.fromLogin = getIntent().getBooleanExtra(LoginActivity.FORM_LOGIN, false);
        bindViewDataBinding(ActivityMainBinding.inflate(getLayoutInflater()));
        super.onCreate(savedInstanceState);
        setLightStatus();

        view.setMainVM(vm);

        click();
        listener();
        view.men1.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            Postcard postcard = ARouter.getInstance().build(Routes.Meeting.HOME);
            LogisticsCenter.completion(postcard);
            ActivityManager.finishActivity(postcard.getDestination());
            EasyWindow.cancelAll();
        } catch (Exception ignore) {
        }
    }


    private void init(Intent intent) {
        Easy.find(UserLogic.class).loginCacheUser();
        callingStatus();
    }

    private void callingStatus() {
        CallingService callingService =
            (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (null != callingService && callingService.getCallStatus()) {
            callingService.buildCallDialog(this, null,
                false).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        callingStatus();
    }

    private void listener() {
        vm.totalUnreadMsgCount.observe(this, v -> Common.buildBadgeView(this, view.men1, v));
    }


    private void bindDot() {
        ContactVM contactVM = ((ContactFragment) contactFragment).getVM();
        if (null == contactVM) return;
        contactVM.friendDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.groupDotNum.val() > 0) ?
                View.VISIBLE : View.GONE);
        });
        contactVM.groupDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.friendDotNum.val() > 0) ?
                View.VISIBLE : View.GONE);
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton[] menus = new RadioButton[]{view.men1, view.men2, view.men3, view.men4};
            if (v == view.men1) switchFragment(conversationListFragment);
            if (v == view.men2) switchFragment(contactFragment);
            if (v == view.men3) switchFragment(appletFragment);
            if (v == view.men4) switchFragment(personalFragment);
            for (RadioButton menu : menus) {
                menu.setChecked(menu == v);
            }
        }
    };

    private final GestureDetector gestureDetector = new GestureDetector(BaseApp.inst(),
        new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                clickListener.onClick(view.men1);
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ((ConversationListFragment) conversationListFragment).clickSlideSet();
                return super.onDoubleTap(e);
            }
        });

    private void click() {
        view.men1.setOnClickListener(clickListener);
        view.men2.setOnClickListener(clickListener);
        view.men3.setOnClickListener(clickListener);
        view.men4.setOnClickListener(clickListener);

        view.men1.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
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
            (ConversationListFragment) ARouter.getInstance().build(Routes.Conversation.CONTACT_LIST).navigation();
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
            ((ConversationListFragment) conversationListFragment).setResultLauncher(resultLauncher);
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

    @Override
    protected void onNewIntent(Intent intent) {
        init(intent);
        super.onNewIntent(intent);
    }
}
