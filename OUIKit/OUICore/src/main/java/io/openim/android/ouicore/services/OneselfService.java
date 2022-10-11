package io.openim.android.ouicore.services;

import com.alibaba.android.arouter.facade.template.IProvider;

import io.openim.android.ouicore.base.BaseApp;
import io.openim.android.ouicore.entity.LoginCertificate;
import io.openim.android.ouicore.net.RXRetrofit.Exception.RXRetrofitException;
import io.openim.android.ouicore.net.bage.Base;
import io.openim.android.ouicore.net.bage.GsonHel;
import io.openim.android.ouicore.utils.Constant;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OneselfService {
    @POST(Constant.IM_API_URL + "/user/get_users_online_status")
    Observable<ResponseBody> getUsersOnlineStatus(@Header("token") String token,
                                                  @Body RequestBody requestBody);
}
