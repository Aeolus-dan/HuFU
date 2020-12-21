package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.google.gson.JsonObject;
import com.hufuinfo.hufudigitalgoldenchain.bean.RechargeRecordResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.TeamInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserInfoListResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface QueryUserInfo {
    /**
     * 查询个人信息列表
     *
     * @param info
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/userInfoList.do")
    Observable<UserInfoListResult> queryUserInfoList(@Header("antiFake") String antiFake, @Body RequestBody info);


    /**
     * 修改密码（校验密码）
     *
     * @param info
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/verifyPassword.do")
    Observable<RequestResult> changeAccountPassword(@Header("antiFake") String antiFake, @Body RequestBody info);

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/updatePin.do")
    Observable<RequestResult> changePaymentPassword(@Body RequestBody info);

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/verifyPin.do")
    Observable<RequestResult> pinChangePaymentPassword(@Body RequestBody info);

    /**
     * 查询用户充值记录
     *
     * @param info RechargeRecord   json
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/rechargeRecord.do")
    Observable<RechargeRecordResult> queryUserRechargeInfo(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 查询总账单记录
     *
     * @param info RechargeRecord   json
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/allRecord.do")
    Observable<RechargeRecordResult> queryAllRecordInfo(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 查询人民币账户记录
     *
     * @param info RechargeRecord   json
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/cnyRecord.do")
    Observable<RechargeRecordResult> queryCnyRecordInfo(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 查询信用额度账单记录
     *
     * @param info RechargeRecord   json
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/creRecord.do")
    Observable<RechargeRecordResult> queryCreRecordInfo(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 查询金本币账户账单记录
     *
     * @param info RechargeRecord   json
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/balRecord.do")
    Observable<RechargeRecordResult> queryBalRecordInfo(@Header("antiFake") String antiFake, @Body RequestBody info);


    /**
     * 查询 User的团队收益
     *
     * @param antiFake
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/ teamDetails.do")
    Observable<TeamInfo> queryTeamDetails(@Header("antiFake") String antiFake);
}
