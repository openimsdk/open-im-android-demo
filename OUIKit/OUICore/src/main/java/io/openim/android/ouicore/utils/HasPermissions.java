package io.openim.android.ouicore.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;

public class HasPermissions {
    private boolean isAllGranted;
    private final Context context;
    private final String[] permissions;

    public boolean isAllGranted() {
        return isAllGranted;
    }

    public HasPermissions(Context context, @NonNull String... permissions) {
        this.context = context;
        this.permissions = permissions;
        isAllGranted = XXPermissions.isGranted(context, permissions);
    }

    public HasPermissions safeGo(OnGrantedListener onGrantedListener) {
        if (isAllGranted)
            onGrantedListener.onGranted();
        else {
            XXPermissions.with(context)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (allGranted) {
                            isAllGranted = true;
                            onGrantedListener.onGranted();
                        } else {
                            onGrantedListener.onGrantedPart(permissions);
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        onGrantedListener.onDeniedPart(permissions, doNotAskAgain);
                    }
                });
        }
        return this;
    }

    public interface OnGrantedListener {
        /**
         * 全部授予
         */
        void onGranted();

        /**
         * 有权限被同意授予时回调
         *
         * @param permissions 请求成功的权限组
         */
        default void onGrantedPart(@NonNull List<String> permissions) {}

        /**
         * 有权限被拒绝授予时回调
         *
         * @param permissions   请求失败的权限组
         * @param doNotAskAgain 是否勾选了不再询问选项
         */
        default void onDeniedPart(@NonNull List<String> permissions, boolean doNotAskAgain) {
            if (doNotAskAgain) {
                StringBuilder tips = new StringBuilder();
                for (String permission : permissions) {
                    tips.append(permission);
                    tips.append(",");
                }
                String toSetting =
                    String.format(BaseApp.inst().getString(R.string.open_setting_permissions_tips),
                        tips);
                Toast.makeText(BaseApp.inst(),
                    toSetting
                    , Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BaseApp.inst(),
                    BaseApp.inst().getString(R.string.open_permissions_tips)
                    , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
