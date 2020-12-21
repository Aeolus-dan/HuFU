package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.hufuinfo.hufudigitalgoldenchain.bean.CertRequstReslut;
import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RequestPhonePin {
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/sentMessage.do")
    Observable<CertRequstReslut> requestPhonePin(@Body RequestBody info);

}
