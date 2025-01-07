package io.openim.android.ouicore.net.RXRetrofit;



import android.content.Context;



import java.util.HashMap;
import java.util.concurrent.TimeUnit;


import io.openim.android.ouicore.utils.L;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ListCompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 网络请求类
 */
public class N {
    final static String TAG = "OpenIM-net";
    public static N instance = null;
    private static Retrofit mRetrofit;
    private static HashMap<String, ListCompositeDisposable> disposableHashMap = null;

    public static void init(HttpConfig config) {
        getInstance(config);
    }

    private N(HttpConfig httpConfig) {
        initMap();
        OkHttpClient.Builder build = new OkHttpClient.Builder()
            .connectTimeout(httpConfig.connectTimeOut, TimeUnit.SECONDS)
            .readTimeout(httpConfig.readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(httpConfig.writeTimeOut, TimeUnit.SECONDS);

        if (null != httpConfig.interceptors) {
            for (Interceptor interceptor : httpConfig.interceptors) {
                build.addInterceptor(interceptor);
            }
        }
        if (HttpConfig.isDebug)
            build.addInterceptor(LogInterceptor());//添加日志拦截器

        mRetrofit = new Retrofit.Builder()
            .baseUrl(httpConfig.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())//添加gson转换器
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//添加rxjava转换器
            .client(build.build())
            .build();
    }

    public static <T> T API(Class<T> service) {
        initException();
        return mRetrofit.create(service);
    }

    private static void initException() {
        if (null == mRetrofit)
            throw new NullPointerException("N is not initialized");
    }

    private static synchronized void getInstance(HttpConfig httpConfig) {
        if (null == instance) {
            instance = new N(httpConfig);
        }
    }
   public Retrofit copy(String baseUrl, OkHttpClient client){
       return mRetrofit.newBuilder().baseUrl(baseUrl).client(client).build();
    }

    public static void clearDispose(Context context) {
        String sign = context.getClass().getSimpleName();
        clearDispose(sign);
    }

    public static void addDispose(Context context, Disposable d) {
        String sign = context.getClass().getSimpleName();
        addDispose(sign, d);
    }

    public static void clearDispose(String sign) {
        initMap();
        ListCompositeDisposable disposable = disposableHashMap.get(sign);
        if (null != disposable)
            disposable.clear();
    }

    public static void addDispose(String sign, Disposable d) {
        initMap();
        ListCompositeDisposable disposable = disposableHashMap.get(sign);
        if (null != disposable)
            disposable.add(d);
        else {
            ListCompositeDisposable listCompositeDisposable = new ListCompositeDisposable();
            listCompositeDisposable.add(d);
            disposableHashMap.put(sign, listCompositeDisposable);
        }
    }

    private static synchronized void initMap() {
        if (null == disposableHashMap)
            disposableHashMap = new HashMap<>();
    }

    //日志拦截器
    public HttpLoggingInterceptor LogInterceptor() {
        return new HttpLoggingInterceptor(message -> L.w(TAG, message)).setLevel(HttpLoggingInterceptor.Level.HEADERS);//设置打印数据的级别
    }


    public static <T> ObservableTransformer<T, T> IOMain() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> ObservableTransformer<T, T> computationMain() {
        return upstream -> upstream.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }
    /**
     * using for download file, the upstream data must be emitted to non main thread by emitter
     */
    public static <T> ObservableTransformer<T, T> bothIO() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }
}
