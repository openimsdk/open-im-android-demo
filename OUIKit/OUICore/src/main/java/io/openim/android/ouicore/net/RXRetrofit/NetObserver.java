package io.openim.android.ouicore.net.RXRetrofit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import io.openim.android.ouicore.net.RXRetrofit.Exception.NetworkException;
import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.base.BaseApp;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * There's only one corner of the universe you can be sure of improving, and that's your own self.
 */
public abstract class NetObserver<T> implements Observer<T> {
    private Context context;
    private String sign; //用于dispose的sign

    /**
     * @param context 用于dispose的key
     */
    public NetObserver(Context context) {
        this.sign = context.getClass().getSimpleName();
    }

    /**
     * @param sign 用于dispose的key
     */
    public NetObserver(String sign) {
        this.sign = sign;
    }

    @Override
    public void onSubscribe(@NonNull final Disposable d) {
        if (null != context) {
            sign = context.getClass().getSimpleName();
        }
        N.addDispose(sign, d);
    }


    @Override
    public void onNext(@NonNull T o) {
        if (HttpConfig.isDebug) {
            onSuccess(o);
        } else {
            try {
                onSuccess(o);
            } catch (Exception e) {
                try {
                    throw new RXRetrofitException(e);
                } catch (RXRetrofitException error) {
                    error.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (!isConnected()) {
            e = new NetworkException();
        }
        e.printStackTrace();
        if (HttpConfig.isDebug) {
            onFailure(e);
        } else {
            try {
                onFailure(e);
            } catch (Exception e1) {
                try {
                    throw new RXRetrofitException(e1);
                } catch (RXRetrofitException error) {
                    error.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onComplete() {

    }
    public abstract void onSuccess(@NonNull T o);

    protected void onFailure(Throwable e) {
    }

    /**
     * 判断网络是否连接
     * @return
     */
    public static boolean isConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) BaseApp.instance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }
}
