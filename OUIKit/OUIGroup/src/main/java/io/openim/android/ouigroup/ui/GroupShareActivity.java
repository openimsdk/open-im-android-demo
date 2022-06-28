package io.openim.android.ouigroup.ui;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.yzq.zxinglibrary.encode.CodeCreator;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupQrCodeBinding;
import io.openim.android.ouigroup.vm.GroupVM;

public class GroupShareActivity extends BaseActivity<GroupVM, ActivityGroupQrCodeBinding> {

    public static final String IS_QRCODE = "is_qrcode";
    private Bitmap qrCodeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupQrCodeBinding.inflate(getLayoutInflater()));
        sink();
        view.setGroupVM(vm);

        boolean isQrcode = getIntent().getBooleanExtra(IS_QRCODE, true);
        if (isQrcode) {
            view.avatar.load(vm.groupsInfo.getValue().getFaceURL());
            qrCodeBitmap = CodeCreator.createQRCode(Constant.QR.QR_JOIN_GROUP + "/" + vm.groupId, 400, 400, null);
            view.qrCode.setImageBitmap(qrCodeBitmap);
        } else {
            view.tips.setText(io.openim.android.ouicore.R.string.share_group_tips1);
            view.qrCodeRl.setVisibility(View.GONE);
            view.groupId.setVisibility(View.VISIBLE);
            view.copy.setOnClickListener(v -> {
                Common.copy(vm.groupId);
                toast(getString(io.openim.android.ouicore.R.string.copy_succ));
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != qrCodeBitmap)
            qrCodeBitmap.recycle();
    }
}
