package io.openim.android.ouicore.base;


import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProvider;

import io.openim.android.ouicore.R;
import io.openim.android.ouicore.utils.SinkHelper;


public class BaseActivity<T extends BaseViewModel, A extends ViewDataBinding> extends AppCompatActivity implements IView {

    protected T vm;
    protected A view;
    private String vmCanonicalName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        if (null != vm) {
            vm.viewCreate();
        }
    }

    protected void bindViewDataBinding(A viewDataBinding) {
        bindViewDataBinding(viewDataBinding, true);
    }

    protected void bindViewDataBinding(A viewDataBinding, boolean lifecycleOwner) {
        view = viewDataBinding;
        setContentView(view.getRoot());
        if (lifecycleOwner) {
            view.setLifecycleOwner(this);
        }
    }

    protected void bindVM(Class<T> vm) {
        this.vm = new ViewModelProvider(this).get(vm);
        vmCanonicalName = this.vm.getClass().getCanonicalName();
        bind();
    }

    protected void bindVM(Class<T> vm, boolean shareVM) {
        bindVM(vm);
        if (shareVM && !BaseApp.viewModels.containsKey(vmCanonicalName)) {
            BaseApp.viewModels.put(vmCanonicalName, this.vm);
        }
    }

    private void bind() {
        if (null == this.vm) return;
        this.vm.setContext(this);
        this.vm.setIView(this);

    }

    protected void setLightStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && null != vm) {
            vm.viewDestroy();
        }
    }

    boolean touchClearFocus = true;

    public void setTouchClearFocus(boolean touchClearFocus) {
        this.touchClearFocus = touchClearFocus;
    }


    public void bindVMByCache(Class<T> vm) {
        String key = vm.getCanonicalName();
        if (BaseApp.viewModels.containsKey(key)) {
            this.vm = (T) BaseApp.viewModels.get(key);
            bind();
        }
    }

    public void removeCacheVM() {
        String key = vm.getClass().getCanonicalName();
        BaseApp.viewModels.remove(key);
        vm.context.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bind();
    }


    /**
     * 点击非获取焦点EditText隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!touchClearFocus) return super.dispatchTouchEvent(ev);
        View v = getCurrentFocus();
        if (v != null && v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                v.clearFocus(); //在根布局添加focusableInTouchMode="true"
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 沉侵式状态栏
     */
    public void sink() {
        setLightStatus();
        SinkHelper.get(this).setTranslucentStatus(view.getRoot());
    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onSuccess(Object body) {

    }

    @Override
    public void toast(String tips) {
        Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();
    }
}
