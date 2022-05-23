package io.openim.android.demo.ui.main;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.fragment.app.Fragment;


import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.databinding.LayoutAddActionBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.ui.search.AddFriendActivity;
import io.openim.android.demo.vm.LoginVM;
import io.openim.android.demo.vm.MainVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.utils.SinkHelper;

public class MainActivity extends BaseActivity<MainVM> implements LoginVM.ViewAction {
    ActivityMainBinding view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(view.getRoot());
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
        bindVM(MainVM.class);
        view.setMainVM(vm);
        super.onCreate(savedInstanceState);

        vm.visibility.observe(this, v -> view.isOnline.setVisibility(v));
        click();
    }

    private void click() {
        showPopupWindow();
    }

    private void showPopupWindow() {
        view.addFriend.setOnClickListener(v -> {
            //初始化一个PopupWindow，width和height都是WRAP_CONTENT
            PopupWindow popupWindow = new PopupWindow(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutAddActionBinding view = LayoutAddActionBinding.inflate(getLayoutInflater());
            view.addFriend.setOnClickListener(c->{
                popupWindow.dismiss();
                startActivity(new Intent(this, AddFriendActivity.class));
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
        Fragment contactListFragment = (Fragment) ARouter.getInstance().build(Routes.Contact.CONTACT_LIST).navigation();
        if (null != contactListFragment)
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, contactListFragment).commit();
    }
}
