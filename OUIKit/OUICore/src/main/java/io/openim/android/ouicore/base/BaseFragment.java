package io.openim.android.ouicore.base;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class BaseFragment<T extends BaseViewModel> extends Fragment implements IView {

    private int page;
    private View rootLayout;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

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
    public void onPause() {
        super.onPause();
        if (getActivity().isFinishing() && null != vm) {
            vm.releaseRes();
        }
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onSuccess(Object body) {

    }

    @Override
    public void toast(String tips) {
        Toast.makeText(getContext(), tips, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void close() {

    }
}
