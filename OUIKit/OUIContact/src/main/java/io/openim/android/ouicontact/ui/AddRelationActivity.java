package io.openim.android.ouicontact.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import io.openim.android.ouicontact.databinding.ActivityAddRelationBinding;
import io.openim.android.ouicore.base.BasicActivity;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.OnDedrepClickListener;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SelectTargetVM;

public class AddRelationActivity extends BasicActivity<ActivityAddRelationBinding> {

    private HasPermissions hasScanPermission;
    private final ActivityResultLauncher<Intent> resultLauncher = Common.getCaptureActivityLauncher(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding(ActivityAddRelationBinding.inflate(getLayoutInflater()));
        click();
        runOnUiThread(() -> hasScanPermission = new HasPermissions(AddRelationActivity.this,
            Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE));
    }

    private void click() {
        view.san.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                hasScanPermission.safeGo(() -> Common.jumpScan(AddRelationActivity.this, resultLauncher));
            }
        });
        view.addFriend.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(Routes.Main.ADD_CONVERS).navigation();
            }
        });
        view.createGroup.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                Easy.installVM(SelectTargetVM.class)
                    .setIntention(SelectTargetVM.Intention.isCreateGroup);
                ARouter.getInstance().build(Routes.Group.SELECT_TARGET).navigation();
            }
        });
        view.addGroup.setOnClickListener(new OnDedrepClickListener() {
            @Override
            public void click(View v) {
                ARouter.getInstance().build(Routes.Main.ADD_CONVERS).withBoolean(Constant.K_RESULT,
                    false).navigation();
            }
        });
    }
}
