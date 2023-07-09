package io.openim.android.ouicore.base.vm.injection;

import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.base.vm.ISubscribe;
import io.openim.android.ouicore.base.vm.State;
import io.openim.android.ouicore.base.vm.Subject;

public class BaseVM extends ViewModel {
    private final State<Subject> channel = new State<>();

    public void toast(String content) {
        Toast.makeText(BaseApp.inst(), content, Toast.LENGTH_SHORT).show();
    }

    void removed() {
        onCleared();
    }

    protected void subject(String key) {
        channel.setValue(new Subject(key));
    }

    protected void subject(String key, Object value) {
        channel.setValue(new Subject(key, value));
    }

    protected void postSubject(String key) {
        channel.postValue(new Subject(key));
    }

    protected void postSubject(String key, Object value) {
        channel.postValue(new Subject(key, value));
    }

    public void subscribe(ISubscribe subscribe) {
        subscribe(null, subscribe);
    }

    public void subscribe(LifecycleOwner owner, ISubscribe subscribe) {
        if (null == owner)
            channel.observeForever(subscribe::onSubject);
        else
            channel.observe(owner, subscribe::onSubject);
    }

    public void unSubscribe(ISubscribe subscribe) {
        channel.removeObserver(subscribe::onSubject);
    }

}
