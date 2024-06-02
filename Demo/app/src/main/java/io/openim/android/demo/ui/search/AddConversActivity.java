package io.openim.android.demo.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hjq.permissions.Permission;

import io.openim.android.demo.databinding.ActivityAddFriendBinding;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constants;
import io.openim.android.ouicore.utils.HasPermissions;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouicore.vm.SearchVM;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.SinkHelper;

@Route(path = Routes.Main.ADD_CONVERS)
public class AddConversActivity extends BaseActivity<SearchVM, ActivityAddFriendBinding> {


    private HasPermissions hasScanPermission;
    private ActivityResultLauncher<Intent> resultLauncher=Common.getCaptureActivityLauncher(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVM(SearchVM.class, true);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAddFriendBinding.inflate(getLayoutInflater()));
        vm.isPerson = getIntent().getBooleanExtra(Constants.K_RESULT, true);
        runOnUiThread(() -> {
            hasScanPermission = new HasPermissions(this, Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE);
        });

        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());

        initView();
    }

    private void initView() {
        view.input.getEditText().setHint(vm.isPerson ? io.openim.android.ouicore.R.string.search_by_id : io.openim.android.ouicore.R.string.search_group_by_id);
        view.back.back.setOnClickListener(v -> finish());
        view.input.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchContactActivity.class));
        });

        if (vm.isPerson)
            view.myQr.setVisibility(View.VISIBLE);
        else
            view.myQr.setVisibility(View.GONE);

        view.myQr.setOnClickListener(v -> {
            ARouter.getInstance().build(Routes.Group.SHARE_QRCODE).navigation();
        });
        view.scan.setOnClickListener(v -> {
            hasScanPermission.safeGo(()->Common.jumpScan(this,resultLauncher));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCacheVM();
    }
}
