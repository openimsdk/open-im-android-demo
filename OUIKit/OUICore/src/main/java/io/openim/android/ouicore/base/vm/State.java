package io.openim.android.ouicore.base.vm;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

public class State<T> extends MutableLiveData<T> {

    public State() {super();}

    public State(T value) {
        super(value);
    }

    public T val() {
        return super.getValue();
    }

    public void update() {
        setValue(getValue());
    }
}
