package io.openim.android.ouicore.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.Stack;

public class BaseDialog extends Dialog implements DialogInterface {
    private static final Stack<Dialog> dialogs = new Stack<>();

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BaseDialog(@NonNull Context context, boolean cancelable,
                         @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public static void dismissAll() {
        Iterator<Dialog> iterator = dialogs.iterator();
        while (iterator.hasNext()) {
            Dialog dialog = iterator.next();
            iterator.remove();
            dialog.dismiss();
        }
    }

    @Override
    public void show() {
        try {
            if (!isShowing()) {
                super.show();
                dialogs.add(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            if (isShowing()) {
                super.dismiss();
                dialogs.remove(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
