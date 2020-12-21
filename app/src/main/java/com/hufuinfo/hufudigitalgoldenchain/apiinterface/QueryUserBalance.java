package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.google.gson.JsonObject;
import com.hufuinfo.hufudigitalgoldenchain.bean.PutForwardResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserBalanceResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface QueryUserBalance {
    /**
     * 查询账号余额
     *
     * @param info 用户标识， mobile
     *             对称加密封装   hufuCode
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/userBalance.do")
    Observable<UserBalanceResult> queryUserBalance(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 提现额度查询
     *
     * @param antiFake 登录状态码
     * @param info
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/putForward.do")
    Observable<PutForwardResult> queryPutForward(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 提现确认  请求
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/putForwardConmfirm.do")
    Observable<RequestResult> queryPutForwardConfirm(@Header("antiFake") String antiFake);

    /**
     * 提现确认  请求 第二次
     *
     * @param info
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/putForwardConmfirm.do")
    Observable<JsonObject> queryPutForwardConfirmSecond(@Header("antiFake") String antiFake, @Body RequestBody info);


    /**
     * 是否是合伙人权限用于查看是否有充值权限
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/memberLevel.do")
    Observable<JsonObject> queryUserMemberLevel(@Header("antiFake") String antiFake);


    /**
     * 普通用户将人民币账户转化为金本币并获得相应奖励
     * 第一次请求 数据Random
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/ goldConversion.do")
    Observable<JsonObject> goldConversionFirst(@Header("antiFake") String antiFake);

    /**
     * 普通用户将人民币账户转化为金本币并获得相应奖励
     * 第二次请求  传入加密后的数据
     *
     * @param antiFake 登录状态码
     * @param info     请求信息:(人民币数量）
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/ goldConversion.do")
    Observable<JsonObject> goldConversionSecond(@Header("antiFake") String antiFake, @Body RequestBody info);
}
