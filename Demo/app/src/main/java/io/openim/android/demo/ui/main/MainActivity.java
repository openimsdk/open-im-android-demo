package io.openim.android.demo.ui.main;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONArray;

import java.util.Map;
import java.util.Objects;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.user.PersonalFragment;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicontact.ui.fragment.ContactFragment;
import io.openim.android.ouiconversation.ui.fragment.ConversationListFragment;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMEvent;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.services.CallingService;
import io.openim.android.ouicore.utils.ActivityManager;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.NotificationVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.sdk.listener.OnAdvanceMsgListener;
import io.openim.android.sdk.models.CustomElem;
import io.openim.android.sdk.models.SignalingInfo;
import io.openim.android.sdk.models.SignalingInvitationInfo;

@Route(path = Routes.Main.HOME)
public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment,
        personalFragment;
    private ActivityResultLauncher<Intent> resultLauncher = Common.getCaptureActivityLauncher(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(MainVM.class);
        init(getIntent());
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
    }


    private void init(Intent intent) {
        Easy.find(UserLogic.class).loginCacheUser(userId -> {
            if (!TextUtils.isEmpty(userId))
                vm.initOfflineNotificationConfig(MainActivity.this, userId);
        });
    }

    private void listener() {
        vm.totalUnreadMsgCount.observe(this, v -> Common.buildBadgeView(this, view.men1, v));
        handleCallingMsg();
    }

    private void handleCallingMsg() {
        CallingService callingService = (CallingService) ARouter.getInstance().build(Routes.Service.CALLING).navigation();
        if (callingService != null) {
            IMEvent.getInstance().addAdvanceMsgListener(new OnAdvanceMsgListener() {
                @Override
                public void onRecvOnlineOnlyMessage(String msg) {
                    Map map = JSONArray.parseObject(msg, Map.class);
                    if (map.containsKey("customElem")) {
                        String customElemStr = String.valueOf(map.get("customElem"));
                        if (!"null".equals(customElemStr)) {
                            CustomElem customElem = JSONArray.parseObject(customElemStr, CustomElem.class);
                            if (!TextUtils.isEmpty(customElem.getData())) {
                                Map customMap = JSONArray.parseObject(customElem.getData(), Map.class);
                                if (customMap.containsKey(Constants.K_CUSTOM_TYPE)) {
                                    int customType = Objects.requireNonNullElse((Integer) customMap.get(Constants.K_CUSTOM_TYPE), -1);
                                    String result = String.valueOf(customMap.get(Constants.K_DATA));
                                    if (!"null".equals(result) && customType >= Constants.MsgType.callingInvite
                                        && customType <= Constants.MsgType.callingHungup) {
                                        SignalingInvitationInfo signalingInvitationInfo = GsonHel.fromJson(result, SignalingInvitationInfo.class);
                                        SignalingInfo signalingInfo = new SignalingInfo();
                                        signalingInfo.setInvitation(signalingInvitationInfo);

                                        switch (customType) {
                                            case Constants.MsgType.callingInvite:
                                                callingService.onReceiveNewInvitation(signalingInfo);
                                                break;
                                            case Constants.MsgType.callingAccept:
                                                callingService.onInviteeAccepted(signalingInfo);
                                                break;
                                            case Constants.MsgType.callingReject:
                                                callingService.onInviteeRejected(signalingInfo);
                                                break;
                                            case Constants.MsgType.callingCancel:
                                                callingService.onInvitationCancelled(signalingInfo);
                                                break;
                                            case Constants.MsgType.callingHungup:
                                                callingService.onHangup(signalingInfo);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }


    private void bindDot() {
        NotificationVM notificationVM=Easy.find(NotificationVM.class);
        notificationVM.friendDot.observe(this, v -> view.badge.setVisibility((notificationVM.hasDot()) ?
            View.VISIBLE : View.GONE));
        notificationVM.groupDot.observe(this, v -> view.badge.setVisibility((notificationVM.hasDot()) ?
            View.VISIBLE : View.GONE));
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton[] menus = new RadioButton[]{view.men1, view.men2, view.men3};
            if (v == view.men1)
                switchFragment(conversationListFragment);
            if (v == view.men2) switchFragment(contactFragment);
            if (v == view.men3) switchFragment(personalFragment);
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

        view.men1.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }


    @Override
    public void jump() {
        //token过期
        if (vm != null)
            vm.clearOfflineNotificationConfig(ActivityManager.getActivityStack().pop(), BaseApp.inst().loginCertificate.userID);
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
        personalFragment = PersonalFragment.newInstance().setParentViewModel(vm);

        personalFragment.setPage(3);
        switchFragment(personalFragment);

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
}
