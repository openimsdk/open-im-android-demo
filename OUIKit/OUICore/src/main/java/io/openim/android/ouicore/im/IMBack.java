package io.openim.android.ouicore.im;

import android.widget.Toast;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.sdk.listener.OnBase;

public class IMBack<T> implements OnBase<T> {
    @Override
    public void onError(int code, String error) {
        Toast.makeText(BaseApp.inst(), error+"("+code+")",
            Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(T data) {

    }
}
