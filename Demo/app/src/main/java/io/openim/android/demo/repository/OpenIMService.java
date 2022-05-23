package io.openim.android.demo.repository;


import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.http.Body;

import retrofit2.http.POST;
;


public interface OpenIMService{

    @POST("/demo/login")
    Observable<ResponseBody> login(@Body RequestBody requestBody);
}
