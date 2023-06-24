package io.openim.android.ouiapplet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import io.openim.android.ouiapplet.databinding.FragmentAppletBinding;
import io.openim.android.ouiapplet.vm.AppletVM;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.utils.Routes;


@Route(path = Routes.Applet.HOME)
public class AppletFragment extends BaseFragment {
    private FragmentAppletBinding view;
    private AppletVM appletVM;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = FragmentAppletBinding.inflate(getLayoutInflater());
        appletVM=Easy.installVM(getActivity(),AppletVM.class);
        appletVM.findApplet();

        initView();
        return view.getRoot();
    }

    private void initView() {

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
}
