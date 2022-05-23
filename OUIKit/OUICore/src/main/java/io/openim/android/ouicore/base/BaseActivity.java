package io.openim.android.ouicore.base;

import static io.openim.android.ouicore.utils.Constant.VM;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import io.openim.android.ouicore.utils.L;
import io.openim.android.ouicore.utils.SinkHelper;


public class BaseActivity<T extends BaseViewModel> extends AppCompatActivity implements IView {

    protected T vm;
    private String VMCanonicalName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        if (null != vm) {
            vm.viewCreate();
        }
    }


    protected void bindVM(Class<T> vm) {
        this.vm = new ViewModelProvider(this).get(vm);
        VMCanonicalName = this.vm.getClass().getCanonicalName();
        this.vm.setContext(this);
        this.vm.setIView(this);
    }

    protected void bindVM(Class<T> vm, boolean shareToCache) {
        bindVM(vm);
        if (shareToCache && !BaseApp.viewModels.containsKey(VMCanonicalName)) {
            BaseApp.viewModels.put(VMCanonicalName, this.vm);
        }

    }

    protected void setLightStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != vm) {
            vm.viewDestroy();
        }
    }

    boolean touchClearFocus = true;

    public void setTouchClearFocus(boolean touchClearFocus) {
        this.touchClearFocus = touchClearFocus;
    }


    public void bindVMByCache(Class<T> vm) {
        String key = vm.getCanonicalName();
        if (BaseApp.viewModels.containsKey(key))
            this.vm = (T) BaseApp.viewModels.get(key);
    }

    public void removeCacheVM() {
        String key = vm.getClass().getCanonicalName();
        BaseApp.viewModels.remove(key);
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
}
