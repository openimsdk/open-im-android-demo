package io.openim.android.ouicore.base;


import android.content.Context;

import java.lang.ref.WeakReference;

import io.openim.android.ouicore.base.vm.injection.BaseVM;

@Deprecated
public class BaseViewModel<T extends IView> extends BaseVM {
    public WeakReference<Context> context;
    private WeakReference<T> IView;
    protected boolean isDestroy;

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
        this.IView = new WeakReference<T>(iView);
    }

    public T getIView() {
        return IView.get();
    }


    //视图销毁时
    protected void releaseRes() {
        isDestroy = true;
    }

    //视图已构建
    protected void viewCreate() {
        isDestroy = false;
    }

    protected void viewPause() {

    }

    protected void viewResume() {
        isDestroy = false;
    }

}
