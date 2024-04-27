package io.openim.android.ouicore.api;

import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface OneselfService {
    static <T> Function<ResponseBody, T> turn(Class<T> tClass) {
        return responseBody -> {
            String body = responseBody.string();
            Base<T> base = GsonHel.dataObject(body, tClass);
            if (base.errCode == 0) return null == base.data ? tClass.newInstance() : base.data;
            throw new RXRetrofitException(base.errCode, base.errDlt);
        };
    }

    @POST
    Observable<ResponseBody> getUsersOnlineStatus(@Url String url, @Header("token") String token,
                                                  @Body RequestBody requestBody);

    /**
     * 下载文件
     *
     * @param fileUrl
     * @return
     */
    @GET
    Observable<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);


    @POST("user/search/public")
    Observable<ResponseBody> searchUser(@Body RequestBody requestBody);

    @POST("user/find/full")
    Observable<ResponseBody> getUsersFullInfo(@Body RequestBody requestBody);

    @POST("user/update")
    Observable<ResponseBody> updateUserInfo(@Body RequestBody requestBody);

    @POST("friend/search")
    Observable<ResponseBody> searchFriends(@Body RequestBody requestBody);

    @POST("office/work_moment/unread/count")
    Observable<ResponseBody> getMomentsUnreadCount(@Body RequestBody requestBody);
}
