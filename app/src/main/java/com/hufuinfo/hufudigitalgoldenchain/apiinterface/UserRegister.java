package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserRegister {
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/userRegister.do")
    Observable<RequestResult> userRegister(@Body RequestBody info);

    /**
     * 请求完善个人信息 第一次请求Random
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/consummateInfo.do")
    Observable<RequestResult> perfectPersonalInfoRandom(@Header("antiFake") String antiFake);

    /**
     * 请求完善个人信息
     *
     * @param antiFake 登录状态码
     * @param info     请求完善个人信息
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/consummateInfo.do")
    Observable<RequestResult> perfectPersonalInfo(@Header("antiFake") String antiFake, @Body RequestBody info);
}
