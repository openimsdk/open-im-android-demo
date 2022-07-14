package io.openim.android.ouicore.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;

public class CommonDialog extends BaseDialog {
    public CommonDialog(@NonNull Context context) {
        super(context);
        initView();
    }

    private LayoutCommonDialogBinding mainView;

    private void initView() {
        mainView = LayoutCommonDialogBinding.inflate(getLayoutInflater());
        setContentView(mainView.getRoot());


        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public LayoutCommonDialogBinding getMainView() {
        return mainView;
    }


    public void setCustomCentral(View customCentral) {
        mainView.tips.setVisibility(View.GONE);
        mainView.content.setVisibility(View.VISIBLE);
        mainView.content.addView(customCentral);
    }

}

