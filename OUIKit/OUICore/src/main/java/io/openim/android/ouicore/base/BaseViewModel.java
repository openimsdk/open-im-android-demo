package io.openim.android.ouicore.base;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.lang.ref.SoftReference;

public class BaseViewModel<T extends IView> extends ViewModel {
    public SoftReference<Context> context;
    protected T IView;

    public Context getContext() {
        return context.get();
    }

    public void setContext(Context context) {
        this.context = new SoftReference<>(context);
    }

    public void setIView(T iView) {
        this.IView = iView;
    }

    //视图销毁时
    protected void viewDestroy() {
    }

    //视图已构建
    protected void viewCreate() {
    }
}
