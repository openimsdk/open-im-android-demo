package io.openim.android.ouimoments.api;

import java.util.HashMap;

import io.openim.android.ouicore.net.RXRetrofit.Parameter;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MomentsService {
    @POST("office/work_moment/find/recv")
    Observable<ResponseBody> getMyMoments(@Body RequestBody requestBody);

    @POST("office/work_moment/add")
    Observable<ResponseBody> pushMoments(@Body RequestBody requestBody);


   static Parameter buildPagination(int pageNumber,int showNumber){
       HashMap<String,Integer> pagination=new HashMap<>();
       pagination.put("pageNumber",pageNumber);
       pagination.put("showNumber",showNumber);
       return new Parameter().add("pagination",pagination);
   }
}
