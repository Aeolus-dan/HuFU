package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.google.gson.JsonObject;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetailsResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderInfoResult;


import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface QueryTransactionOrder {
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/orderDetails.do")
    Observable<OrderDetailsResult> queryTransactionOrder(@Header("antiFake") String antiFake, @Body RequestBody info);

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/queryFirm.do")
    Observable<OrderInfoResult> queryOrderInfo(@Header("antiFake") String antiFake, @Body RequestBody info);
}
