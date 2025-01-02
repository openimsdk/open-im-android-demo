package io.openim.android.demo.ui.user;

import static io.openim.android.ouicore.utils.Common.UIHandler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cretin.www.cretinautoupdatelibrary.interfaces.AppUpdateInfoListener;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;

import java.util.ArrayList;

import io.openim.android.demo.BuildConfig;
import io.openim.android.demo.R;
import io.openim.android.demo.databinding.ActivityAboutUsBinding;
import io.openim.android.ouicore.base.BaseActivity;
import io.openim.android.ouicore.base.BaseViewModel;
import io.openim.android.ouicore.base.vm.injection.Easy;
import io.openim.android.ouicore.im.IMUtil;
import io.openim.android.ouicore.update.UpdateApp;
import io.openim.android.ouicore.utils.Common;
import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.OpenIMClient;
import open_im_sdk_callback.UploadLogProgress;

public class AboutUsActivity extends BaseActivity<BaseViewModel, ActivityAboutUsBinding> {
    private final UpdateApp updateApp = new UpdateApp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindViewDataBinding(ActivityAboutUsBinding.inflate(getLayoutInflater()));
        sink();
        PackageInfo packageInfo = Common.getAppPackageInfo();
        if (null != packageInfo) {
            view.version.setText(packageInfo.versionName);
        }
        view.version.setOnLongClickListener(v -> {
            toast(BuildConfig.BUILD_TYPE.toUpperCase());
            return false;
        });
        view.update.setOnClickListener(v -> {
            AppUpdateUtils.getInstance().addAppUpdateInfoListener(isLatest -> {
                if (isLatest) toast(getString(io.openim.android.ouicore.R.string.is_lastest));
            });
            updateApp.checkUpdate(this);
        });
        view.uploadLog.setOnClickListener(v -> {
            showWaiting();
            OpenIMClient.getInstance().uploadLogs(new IMUtil.IMCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    cancelWaiting();
                    toast(getString(io.openim.android.ouicore.R.string.upload_success));
                }
                @Override
                public void onError(int code, String error) {
                    cancelWaiting();
                    toast(getString(io.openim.android.ouicore.R.string.upload_err));
                    L.e("IMCallBack", "uploadLogs onError:(" + code + ")" + error);
                }
            }, new ArrayList<>(), 0,"", (l, l1) -> {
                Log.d("testprogress", "current:" + l + "total:" + l1);
            });
/*            UIHandler.postDelayed(() -> {
                waitDialog.dismiss();
                toast(getString(io.openim.android.ouicore.R.string.upload_success));
            }, 5 * 1000);*/
        });
    }

}
