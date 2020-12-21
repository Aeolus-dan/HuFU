package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.hufuinfo.hufudigitalgoldenchain.bean.UpdateAppResult;

import io.reactivex.Observable;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UpdateApp {
    /**
     * 查询最新app 版本
     *
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/queryVersion.do")
    Observable<UpdateAppResult> queryVersion();
}
