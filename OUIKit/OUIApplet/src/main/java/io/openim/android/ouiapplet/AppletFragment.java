package io.openim.android.ouiapplet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouiapplet.databinding.ActivityAppletBinding;
import io.openim.android.ouiapplet.databinding.FragmentAppletBinding;
import io.openim.android.ouiapplet.vm.AppletVM;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.UserLogic;


@Route(path = Routes.Applet.HOME)
public class AppletFragment extends BaseFragment {
    private ActivityAppletBinding view;
    private AppletVM appletVM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=ActivityAppletBinding.inflate(getLayoutInflater());

        initView();
        return view.getRoot();
    }

    private void initView() {
        Easy.find(UserLogic.class).discoverPageURL
            .observe(getViewLifecycleOwner(),
            s -> view.webView.loadUrl(s));
    }


}
