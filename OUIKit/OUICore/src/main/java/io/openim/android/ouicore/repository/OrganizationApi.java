package io.openim.android.ouicore.repository;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OrganizationApi {

    @POST("organization/department/find")
    Observable<ResponseBody> getDepartment(@Body RequestBody requestBody);

    /**
     *  获取所在部门
     * @param requestBody
     * @return
     */
    @POST("organization/user/department")
    Observable<ResponseBody> getInDepartment(@Body RequestBody requestBody);
}
