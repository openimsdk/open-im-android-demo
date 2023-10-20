package io.openim.android.ouicore.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hjq.permissions.XXPermissions;

public class HasPermissions {
    private boolean isGranted;
    private final Context context;
    private final String[] permissions;

    public HasPermissions(Context context, @NonNull String... permissions) {
        this.context = context;
        this.permissions=permissions;
        isGranted = XXPermissions.isGranted(context, permissions);
    }

    public HasPermissions safeGo(OnGrantedListener onGrantedListener) {
        if (isGranted)
            onGrantedListener.onGranted();
        else {
            XXPermissions.with(context)
                .permission(permissions)
                .request((permissions1, allGranted) -> {
                    if (allGranted) {
                        isGranted = true;
                        onGrantedListener.onGranted();
                    }
                });
        }
        return this;
    }
    public interface OnGrantedListener {
        void onGranted();
    }
}
