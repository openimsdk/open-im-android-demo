package io.openim.android.ouiapplet.service;

import java.util.Random;

import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface NetService {
    /**
     * 获取小程序列表
     */
    @POST("/applet/find")
    Observable<ResponseBody> findApplet(@Body RequestBody requestBody);

}
