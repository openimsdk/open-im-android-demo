package io.openim.android.ouicore.base;


import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.lang.ref.WeakReference;

public class BaseViewModel<T extends IView> extends ViewModel {
    public WeakReference<Context> context;
    protected T IView;

    public Context getContext() {
        return context.get();
    }

    public void setContext(Context context) {
        if (null != this.context) {
            this.context.clear();
            this.context = null;
        }
        this.context = new WeakReference<>(context);
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
