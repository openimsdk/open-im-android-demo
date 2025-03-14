package io.openim.android.ouicore.net.RXRetrofit.interceptors;

import androidx.annotation.NonNull;

import java.io.IOException;

import io.openim.android.ouicore.net.RXRetrofit.annotations.IdentityEncoding;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

public class IdentityEncodingInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        if (invocation != null && invocation.method().getAnnotation(IdentityEncoding.class) != null) {
            Request newRequest = request.newBuilder()
                .header("Accept-Encoding", "identity")
                .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(request);
    }
}
