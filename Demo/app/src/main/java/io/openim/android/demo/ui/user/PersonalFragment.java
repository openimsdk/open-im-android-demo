package io.openim.android.demo.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.launcher.ARouter;

import java.util.Observable;
import java.util.Observer;

import io.openim.android.demo.databinding.FragmentPersonalBinding;
import io.openim.android.demo.ui.login.LoginActivity;
import io.openim.android.ouiconversation.ui.PreviewMediaActivity;
import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.BaseFragment;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.Obs;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.PreviewMediaVM;
import io.openim.android.ouicore.vm.UserLogic;
import io.openim.android.ouicore.widget.CommonDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnBase;

public class PersonalFragment extends BaseFragment implements Observer {
    public FragmentPersonalBinding view;
    private final UserLogic user = Easy.find(UserLogic.class);
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
        Obs.inst().addObserver(this);
        return view.getRoot();
    }

    private void initView() {
        user.info.observe(getActivity(),v-> {
            view.avatar.load(v.getFaceURL(),v.getNickname());
            view.name.setText(v.getNickname());
        });
        view.userId.setText("ID：" + BaseApp.inst().loginCertificate.userID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Obs.inst().deleteObserver(this);
    }

    private void listener() {
        view.avatar.setOnClickListener(v -> {
            PreviewMediaVM mediaVM = Easy.installVM(PreviewMediaVM.class);
            PreviewMediaVM .MediaData mediaData =new PreviewMediaVM.MediaData(user.info.val().getNickname());
            mediaData.mediaUrl=user.info.val().getFaceURL();
            mediaVM.previewSingle(mediaData);
            v.getContext().startActivity(
                new Intent(v.getContext(), PreviewMediaActivity.class));
        });
        view.accountSetting.setOnClickListener(v->{
            startActivity(new Intent(getActivity(),AccountSettingActivity.class));
        });
        view.aboutLy.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(),AboutUsActivity.class));
        });
        view.personalInfo.setOnClickListener(new OnDedrepClickListener(1000) {
            @Override
            public void click(View v) {
                startActivity(new Intent(getActivity(), PersonalInfoActivity.class));
            }
        });
        view.userIdLy.setOnClickListener(v -> {
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
                        IMUtil.logout((AppCompatActivity) getActivity(), LoginActivity.class);
                    }
                });

            });
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        Obs.Message message = (Obs.Message) o;
        if (message.tag == Constants.Event.USER_INFO_UPDATE) {
            view.avatar.load(BaseApp.inst().loginCertificate.faceURL,BaseApp.inst().loginCertificate.nickname);
            view.name.setText(BaseApp.inst().loginCertificate.nickname);
        }
    }
}
