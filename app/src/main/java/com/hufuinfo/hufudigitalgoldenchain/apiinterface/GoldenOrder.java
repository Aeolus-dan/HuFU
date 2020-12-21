package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.hufuinfo.hufudigitalgoldenchain.bean.CertRequstReslut;
import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GoldenOrder {

    /**
     * 创建订单
     * 请求Random
     *
     * @param antiFake 登录状态码
     * @return 请求结果
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/createFirmBargain.do")
    Observable<RequestResult> createFirmBargin(@Header("antiFake") String antiFake);

    /**
     * 创建订单
     * 第二次请求
     *
     * @param antiFake 登录状态码
     * @param info     创建订单的信息，  对称加密数据信息
     *                 {"code":"decrypt  info"}
     * @return 创建结果 {"success":boolean, "msg": "result message","firmId":"订单号"}
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/createFirmBargain.do")
    Observable<RequestResult> createFirmBarginSecond(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 第一次请求取消订单，返回random数据
     *
     * @param antiFake 登录状态码
     * @return 返回加密数据
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/revokeOrder.do")
    Observable<RequestResult> revokeOrderFirst(@Header("antiFake") String antiFake);

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/revokeOrder.do")
    Observable<CertRequstReslut> revokeOrderSecond(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 第一次请求确认订单,返回random （对称加密秘钥）数据
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/confirmationOrder.do")
    Observable<RequestResult> confirmationOrderFirst(@Header("antiFake") String antiFake);

    /**
     * 确认交易订单
     *
     * @param info  请求确认信息
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/confirmationOrder.do")
    Observable<CertRequstReslut> confirmationOrderSecond(@Header("antiFake")String antiFake,@Body RequestBody info);
}
