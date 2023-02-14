package io.openim.android.ouimoments;

import android.content.Context;
import android.view.KeyEvent;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.services.MomentsBridge;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouimoments.ui.CircleFragment;

@Route(path = Routes.Service.MOMENTS)
public class IBridgeImpl implements MomentsBridge {

    private CircleFragment circleFragment;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return circleFragment.onKeyDown(keyCode, event);
    }

    @Override
    public BaseFragment buildMomentsFragment() {
        return circleFragment = CircleFragment.newInstance(null);
    }

    @Override
    public void init(Context context) {

    }
}
