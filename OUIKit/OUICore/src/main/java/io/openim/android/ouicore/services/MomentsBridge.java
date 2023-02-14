package io.openim.android.ouicore.services;

import android.view.KeyEvent;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.utils.Routes;

public interface  MomentsBridge  extends IProvider {
    boolean onKeyDown(int keyCode, KeyEvent event);

    BaseFragment buildMomentsFragment();
}
