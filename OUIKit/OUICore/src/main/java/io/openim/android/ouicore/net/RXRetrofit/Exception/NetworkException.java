package io.openim.android.ouicore.net.RXRetrofit.Exception;


import io.openim.android.ouicore.R;
import io.openim.android.ouicore.base.BaseApp;

public class NetworkException extends Exception {

    private static final long serialVersionUID = 114946L;


    public NetworkException() {
        super(BaseApp.instance().getString(R.string.network_unavailable_tips));
    }

    public NetworkException(String message) {
        super(message);
    }


    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }


}
