package io.openim.android.ouicore.net.RXRetrofit.Exception;


public class RXRetrofitException extends Exception {

    private static final long serialVersionUID = 114946L;

    public RXRetrofitException() {}

    public RXRetrofitException(String message) {
        super(message);
    }

    public RXRetrofitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RXRetrofitException(Throwable cause) {
        super(cause);
    }

}
