package io.openim.android.ouicore.base;


import android.content.Context;

import androidx.lifecycle.ViewModel;

import java.lang.ref.WeakReference;

import io.openim.android.ouicore.widget.WaitDialog;
import io.openim.android.sdk.listener.OnBase;

public class BaseViewModel<T extends IView> extends ViewModel {
    public WeakReference<Context> context;
    private WeakReference<T> IView;

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
    protected void viewDestroy() {
    }

    //视图已构建
    protected void viewCreate() {
    }

    protected void viewPause() {

    }

    protected void viewResume() {

    }


}
