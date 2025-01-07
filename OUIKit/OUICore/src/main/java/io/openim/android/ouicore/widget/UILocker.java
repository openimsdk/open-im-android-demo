package io.openim.android.ouicore.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.databinding.TransparentDialogBinding;

public class UILocker {

    private Dialog dialog;

    public void showTransparentDialog(Context context) {
        if (null != context) return;
        if (dialog == null) {
            dialog = new Dialog(context.getApplicationContext());
            TransparentDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.transparent_dialog, null, false);
            dialog.setContentView(binding.getRoot());

            // set the dialog with transparent in full screen
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }

            dialog.setCancelable(false); // cannot be canceled
        }
        dialog.show();
    }

    public void dismissTransparentDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
