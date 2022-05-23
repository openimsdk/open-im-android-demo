package io.openim.android.ouicore.base;

import android.content.Context;

import androidx.lifecycle.ViewModel;

public class BaseViewModel<T extends IView>  extends ViewModel {
    protected Context context;
    protected T IView;

    public void setContext(Context context) {
        this.context = context;
    }
    public void setIView(T iView) {
        this.IView = iView;
    }

    //视图销毁时
    protected void viewDestroy() {
    }
    //视图已构建
    protected void viewCreate(){}
}
