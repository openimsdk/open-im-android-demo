package io.openim.android.ouicontact.ni;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface NiService {
    /**
     *  通用
     * @param requestBody
     * @return
     */
    @POST
    Observable<ResponseBody> CommNI(@Url String url, @Header("token") String token,
                                    @Body RequestBody requestBody);


}
