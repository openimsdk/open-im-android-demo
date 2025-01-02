package io.openim.android.ouicore.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.openim.android.ouicore.databinding.DialogProgressBinding;

public class ProgressDialog extends Dialog {
    private final DialogProgressBinding binding;
    private boolean isNotDismiss = false;
    private OnCancelListener cancelListener;
    public ProgressDialog(@NonNull Context context) {
        super(context, io.openim.android.ouicore.R.style.dialog_tran2);
        binding = DialogProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initDialog(context, binding);
    }

    public void setInfo(String str) {
        binding.dialogInfo.setText(str);
    }

    public ProgressDialog setNotDismiss() {
        this.isNotDismiss = true;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        return this;
    }

    private void initDialog(Context context, DialogProgressBinding binding) {
        WindowManager.LayoutParams params = getWindow() != null ?
            getWindow().getAttributes() : new WindowManager.LayoutParams();
        params.gravity = Gravity.CENTER;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.windowAnimations = io.openim.android.ouicore.R.style.dialog_animation;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        binding.areaOutside.setOnClickListener(v -> { if (!isNotDismiss && cancelListener != null) cancelListener.onCancel(this); });
    }

    @Override
    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        super.setOnCancelListener(listener);
        cancelListener = listener;
    }
}
