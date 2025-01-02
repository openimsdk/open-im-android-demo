package io.openim.android.ouicore.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface NiService {

    static <T> Function<ResponseBody, T> turn(Class<T> tClass) {
        return responseBody -> {
            String body = responseBody.string();
            Base<T> base = (Base<T>) GsonHel.dataObject(body, tClass);
            if (base.errCode == 0) return null == base.data ? tClass.newInstance() : base.data;
            throw new RXRetrofitException(base.errCode, base.errMsg);
        };
    }

    static <T> Function<ResponseBody, List<T>> listTurn(Class<T> tClass) {
        return responseBody -> {
            String body = responseBody.string();
            Base<List<T>> base = GsonHel.dataArray(body, tClass);
            if (base.errCode == 0) return null == base.data ? new ArrayList<>() : base.data;
            throw new RXRetrofitException(base.errCode, base.errMsg);
        };
    }

    /**
     * 通用
     *
     * @param requestBody
     * @return
     */
    @POST
    Observable<ResponseBody> CommNI(@Url String url, @Header("token") String token,
                                    @Body RequestBody requestBody);

    static Parameter buildParameter() {
        return new Parameter().add("operationID",
            System.currentTimeMillis() + (new Random().nextInt(9999)) + "");
    }
}
