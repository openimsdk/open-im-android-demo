package io.openim.android.ouicore.base;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class BaseFragment<T extends BaseViewModel> extends Fragment implements IView {
    protected T vm;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (null != vm) {
            vm.viewCreate();
        }
        super.onCreate(savedInstanceState);
    }

    protected void bindVM(Class<T> vm) {
        this.vm = new ViewModelProvider(this).get(vm);
        this.vm.setContext(getContext());
        this.vm.setIView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != vm) {
            vm.viewDestroy();
        }
    }
}
