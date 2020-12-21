package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.hufuinfo.hufudigitalgoldenchain.bean.AuthPriceResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.CertRequstReslut;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface QueryAuthPrice {
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/ authPrice.do")
    Observable<AuthPriceResult> queryAuthPrice(@Body RequestBody info);

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/attornGold.do")
    Observable<CertRequstReslut> attornGold(@Header("antiFake") String antiFake, @Body RequestBody info);
}
