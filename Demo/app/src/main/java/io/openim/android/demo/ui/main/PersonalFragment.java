package io.openim.android.demo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.launcher.ARouter;

import io.openim.android.demo.databinding.FragmentPersonalBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

public class PersonalFragment extends BaseFragment {
    public FragmentPersonalBinding view;

    public static PersonalFragment newInstance() {

        Bundle args = new Bundle();

        PersonalFragment fragment = new PersonalFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = FragmentPersonalBinding.inflate(inflater);

        initView();
        listener();
        return view.getRoot();
    }

    private void initView() {
        view.avatar.load(BaseApp.inst().loginCertificate.faceURL);
        view.name.setText(BaseApp.inst().loginCertificate.nickname);
        view.userId.setText("ID：" + BaseApp.inst().loginCertificate.userID);
    }

    private void listener() {
        view.personalInfo.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                startActivity(new Intent(getActivity(),PersonalInfoActivity.class));
            }
        });
        view.userId.setOnClickListener(v -> {
            Common.copy(BaseApp.inst().loginCertificate.userID);
            toast(getString(io.openim.android.ouicore.R.string.copy_succ));
        });
        view.qrCode.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Group.SHARE_QRCODE).navigation();
        });
        view.quit.setOnClickListener(v -> {
            CommonDialog commonDialog = new CommonDialog(getActivity()).atShow();
            commonDialog.getMainView().tips.
                setText(io.openim.android.ouicore.R.string.quit_tips);
            commonDialog.getMainView().cancel.setOnClickListener(v2 -> commonDialog.dismiss());
            commonDialog.getMainView().confirm.setOnClickListener(v2 -> {
                commonDialog.dismiss();
                WaitDialog waitDialog = new WaitDialog(getActivity());
                waitDialog.show();
                OpenIMClient.getInstance().logout(new OnBase<String>() {
                    @Override
                    public void onError(int code, String error) {
                        waitDialog.dismiss();
                        toast(error + code);
                    }

                    @Override
                    public void onSuccess(String data) {
                        waitDialog.dismiss();
                        IMUtil.logout((AppCompatActivity) getActivity(),LoginActivity.class);
                    }
                });

            });
        });
    }
}
