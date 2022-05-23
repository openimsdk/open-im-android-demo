package io.openim.android.ouicore.net.RXRetrofit.Exception;


public class NetworkException extends Exception {

    private static final long serialVersionUID = 114946L;


    public NetworkException() {
        super("");
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
