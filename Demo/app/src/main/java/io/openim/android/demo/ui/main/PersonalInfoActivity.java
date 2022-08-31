package io.openim.android.demo.ui.main;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;

import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityMainBinding;
import io.openim.android.demo.databinding.ActivityPersonalInfoBinding;
import io.openim.android.demo.vm.PersonalVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.TimeUtil;
import io.openim.android.ouicore.widget.BottomPopDialog;
import io.openim.android.ouicore.widget.PhotographAlbumDialog;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import io.openim.android.sdk.listener.OnFileUploadProgressListener;
import io.openim.android.sdk.models.Message;
import io.openim.android.sdk.models.UserInfo;

public class PersonalInfoActivity extends BaseActivity<PersonalVM, ActivityPersonalInfoBinding> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(PersonalVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityPersonalInfoBinding.inflate(getLayoutInflater()));
        sink();

        vm.getSelfUserInfo();

        initView();
        click();
    }

    private void click() {
        PhotographAlbumDialog albumDialog = new PhotographAlbumDialog(this);
        view.avatarLy.setOnClickListener(view -> {
            vm.waitDialog.show();
            albumDialog.setOnSelectResultListener(path -> {
                OpenIMClient.getInstance().uploadFile(new OnFileUploadProgressListener() {
                    @Override
                    public void onError(int code, String error) {
                        vm.waitDialog.dismiss();
                    }

                    @Override
                    public void onProgress(long progress) {
                        vm.waitDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String s) {
                        vm.setFaceURL(s);
                    }
                }, path);

            });
            albumDialog.show();
        });
        view.nickNameLy.setOnClickListener(v -> resultLauncher.launch(new Intent(this, EditTextActivity.class)));
    }

    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK) return;
        String resultStr = result.getData().getStringExtra(Constant.K_RESULT);
        vm.setNickname(resultStr);
    });

    private void initView() {
        vm.userInfo.observe(this, userInfo -> {
            if (null == userInfo) return;
            view.avatar.load(userInfo.getFaceURL());
            view.nickName.setText(userInfo.getNickname());
            view.gender.setText(userInfo.getGender() + "");
            if (userInfo.getBirth() > 0) {
                TimeUtil.getTime(userInfo.getBirth(), TimeUtil.yearMonthDayFormat);
            }
            view.identity.setText(userInfo.getUserID());
        });
    }
}
