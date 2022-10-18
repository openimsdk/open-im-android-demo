package io.openim.android.ouigroup.ui;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.yzq.zxinglibrary.encode.CodeCreator;

import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.Constant;
import io.openim.android.ouicore.utils.Routes;
import io.openim.android.ouigroup.R;
import io.openim.android.ouigroup.databinding.ActivityGroupQrCodeBinding;
import io.openim.android.ouicore.vm.GroupVM;
import io.openim.android.sdk.models.GroupInfo;

@Route(path = Routes.Group.SHARE_QRCODE)
public class ShareQrcodeActivity extends BaseActivity<GroupVM, ActivityGroupQrCodeBinding> {

    public static final String IS_QRCODE = "is_qrcode";
    private Bitmap qrCodeBitmap;
    private String tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindVMByCache(GroupVM.class);
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityGroupQrCodeBinding.inflate(getLayoutInflater()));
        boolean isQrcode = getIntent().getBooleanExtra(IS_QRCODE, true);
        String shareContent;
        if (null == vm) {
            //这里表示个人二维码分享 个人信息复制给群信息用于显示
            bindVM(GroupVM.class);
            LoginCertificate loginCertificate = LoginCertificate.getCache(this);
            shareContent = Constant.QR.QR_ADD_FRIEND + "/" + loginCertificate.userID;
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setGroupName(loginCertificate.nickname);
            groupInfo.setFaceURL(loginCertificate.faceURL);
            vm.groupsInfo.setValue(groupInfo);
            tips = getString(io.openim.android.ouicore.R.string.qr_tips2);
            view.title.setText(io.openim.android.ouicore.R.string.qr_code);
        } else {
            shareContent = Constant.QR.QR_JOIN_GROUP + "/" + vm.groupId;
            if (isQrcode) {
                view.title.setText(io.openim.android.ouicore.R.string.group_qrcode);
                tips = getString(io.openim.android.ouicore.R.string.share_group_tips2);
            } else {
                view.title.setText(R.string.group_id);
                tips = getString(io.openim.android.ouicore.R.string.share_group_tips1);
            }

        }
        sink();
        view.setGroupVM(vm);
        view.tips.setText(tips);
        if (isQrcode) {
            view.avatar.load(vm.groupsInfo.getValue().getFaceURL());
            qrCodeBitmap = CodeCreator.createQRCode(shareContent, 500, 500, null);
            view.qrCode.setImageBitmap(qrCodeBitmap);
        } else {
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
