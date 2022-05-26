package io.openim.android.ouicore.base;

import android.widget.Toast;

/**
 * View
 */
public interface IView {
    void onError(String error);

    void onSuccess(String body);

    void toast(String tips);

}
