package io.openim.android.ouicore.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseDialog;
import io.openim.android.ouicore.databinding.DialogPhotographAlbumBinding;
import io.openim.android.ouicore.databinding.LayoutBottompopV3Binding;
import io.openim.android.ouicore.databinding.LayoutCommonDialogBinding;

public class BottomPopDialog extends BaseDialog {
    private DialogPhotographAlbumBinding mainView;
    private View customMainView;

    public BottomPopDialog(@NonNull Context context, View mainView) {
        super(context);
        this.customMainView = mainView;
        initView();
    }

    public BottomPopDialog(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        mainView = DialogPhotographAlbumBinding.inflate(getLayoutInflater());
        if (null == customMainView) setContentView(mainView.getRoot());
        else setContentView(customMainView);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.windowAnimations = R.style.dialog_animation;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public DialogPhotographAlbumBinding getMainView() {
        return mainView;
    }

    public View getCustomMainView() {
        return customMainView;
    }


}
