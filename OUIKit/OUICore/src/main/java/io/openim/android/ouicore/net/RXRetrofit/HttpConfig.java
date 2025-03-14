package io.openim.android.ouicore.net.RXRetrofit;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;

/**
 * There's only one corner of the universe you can be sure of improving, and that's your own self.
 */
public class HttpConfig {
    static boolean isDebug = true;
    String baseUrl;
    long writeTimeOut = 10;
    long readTimeOut = 10;
    long connectTimeOut = 15;
    List<Interceptor> interceptors;

    public HttpConfig addInterceptor(Interceptor interceptor) {
        synchronized (this) {
            if (null == interceptors)
                interceptors = new ArrayList<>();
        }
        interceptors.add(interceptor);
        return this;
    }

    public HttpConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public HttpConfig setWriteTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public HttpConfig setReadTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public HttpConfig setConnectTimeOut(long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    public HttpConfig setDebug(boolean isDebug) {
        HttpConfig.isDebug = isDebug;
        return this;
    }
}
