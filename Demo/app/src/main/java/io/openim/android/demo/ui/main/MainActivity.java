package io.openim.android.demo.ui.main;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentTransaction;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.util.Observable;
import java.util.Observer;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.databinding.LayoutAddActionBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.search.AddConversActivity;
import io.openim.android.demo.ui.search.PersonDetailActivity;
import io.openim.android.demo.ui.user.PersonalFragment;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicontact.ui.ContactFragment;
import io.openim.android.ouicontact.vm.ContactVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;
import io.openim.android.ouicore.utils.Constant;

@Route(path = Routes.Main.HOME)
public class MainActivity extends BaseActivity<MainVM, ActivityMainBinding> implements LoginVM.ViewAction, Observer {

    private int mCurrentTabIndex;
    private BaseFragment lastFragment, conversationListFragment, contactFragment, personalFragment;
    private boolean hasScanPermission, hasShoot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hasShoot = AndPermission.hasPermissions(this, Permission.CAMERA, Permission.RECORD_AUDIO);
        AndPermission.with(this).overlay().start();

        bindVM(MainVM.class);
        vm.fromLogin = getIntent().getBooleanExtra(LoginActivity.FORM_LOGIN, false);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityMainBinding.inflate(getLayoutInflater()));

        sink();

        view.setMainVM(vm);
        initView();
        Obs.inst().addObserver(this);

        vm.visibility.observe(this, v -> view.isOnline.setVisibility(v));
        click();

        hasScanPermission = AndPermission.hasPermissions(this, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Obs.inst().deleteObserver(this);
    }

    private void initView() {
        view.avatar.load(BaseApp.inst().loginCertificate.faceURL);
        view.nickname.setText(BaseApp.inst().loginCertificate.nickname);
    }

    private void bindDot() {
        ContactVM contactVM = ((ContactFragment) contactFragment).getVM();
        if (null == contactVM) return;
        contactVM.friendDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.groupDotNum.getValue() > 0) ? View.VISIBLE : View.GONE);
        });
        contactVM.groupDotNum.observe(this, integer -> {
            view.badge.setVisibility((integer > 0 || contactVM.friendDotNum.getValue() > 0) ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasShoot) {
            AndPermission.with(this)
                .runtime()
                .permission(Permission.CAMERA, Permission.RECORD_AUDIO)
                .onGranted(permissions -> {
                    // Storage permission are allowed.
                    hasShoot = true;
                })
                .onDenied(permissions -> {
                    // Storage permission are not allowed.
                })
                .start();
        }
    }

    private void click() {
        view.callRecord.setOnClickListener(view -> {
            startActivity(new Intent(this,CallHistoryActivity.class));
        });
        showPopupWindow();
        view.menuGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.men1)
                switchFragment(conversationListFragment);
            if (checkedId == R.id.men2)
                switchFragment(contactFragment);

            view.header.setVisibility(View.VISIBLE);
            SinkHelper.get(this).setTranslucentStatus(view.getRoot());
            if (checkedId == R.id.men3) {
                switchFragment(personalFragment);
                view.header.setVisibility(View.GONE);
                view.getRoot().setPadding(0, 0, 0, 0);
            }
        });
    }

    private void showPopupWindow() {
        view.addFriend.setOnClickListener(v -> {
            //初始化一个PopupWindow，width和height都是WRAP_CONTENT
            PopupWindow popupWindow = new PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutAddActionBinding view = LayoutAddActionBinding.inflate(getLayoutInflater());
            view.scan.setOnClickListener(c -> {
                popupWindow.dismiss();
                if (hasScanPermission)
                    jumpScan();
                else {
                    AndPermission.with(this).runtime()
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .onGranted(data -> {
                            jumpScan();
                        })
                        .onDenied(data -> {
                        }).start();
                }

            });
            view.addFriend.setOnClickListener(c -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, AddConversActivity.class));
            });
            view.addGroup.setOnClickListener(c -> {
                popupWindow.dismiss();
                startActivity(new Intent(this, AddConversActivity.class)
                    .putExtra(AddConversActivity.IS_PERSON, false));
            });
            view.createGroup.setOnClickListener(c -> {
                popupWindow.dismiss();
                ARouter.getInstance().build(Routes.Group.CREATE_GROUP).navigation();
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


    private final ActivityResultLauncher<Intent> captureActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || null == result.getData()) return;
        String content = result.getData().getStringExtra(com.yzq.zxinglibrary.common.Constant.CODED_CONTENT);

        if (content.contains(Constant.QR.QR_ADD_FRIEND)) {
            String userId = content.substring(content.lastIndexOf("/") + 1);
            if (!TextUtils.isEmpty(userId))
                startActivity(new Intent(this, PersonDetailActivity.class).putExtra(Constant.K_ID, userId));
        } else if (content.contains(Constant.QR.QR_JOIN_GROUP)) {
            String groupId = content.substring(content.lastIndexOf("/") + 1);
            if (!TextUtils.isEmpty(groupId))
                ARouter.getInstance().build(Routes.Group.DETAIL)
                    .withString(io.openim.android.ouicore.utils.Constant.K_GROUP_ID, groupId).navigation();
        }
    });

    /**
     * 跳转到扫一扫
     */
    private void jumpScan() {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        ZxingConfig config = new ZxingConfig();
        config.setPlayBeep(true);//是否播放扫描声音 默认为true
        config.setShake(true);//是否震动  默认为true
        config.setDecodeBarCode(true);//是否扫描条形码 默认为true
        config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
        intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);
        captureActivityLauncher.launch(intent);
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
        contactFragment = (BaseFragment) ARouter.getInstance().build(Routes.Contact.HOME).navigation();
        conversationListFragment = (BaseFragment) ARouter.getInstance().build(Routes.Conversation.CONTACT_LIST).navigation();
        personalFragment = PersonalFragment.newInstance();

        personalFragment.setPage(3);
        switchFragment(personalFragment);

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
    public void update(Observable observable, Object o) {
        Obs.Message message = (Obs.Message) o;
        if (message.tag == Constant.Event.USER_INFO_UPDATA) {
            initView();
        }
    }
}
