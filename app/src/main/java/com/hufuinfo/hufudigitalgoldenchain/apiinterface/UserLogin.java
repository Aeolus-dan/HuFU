package com.hufuinfo.hufudigitalgoldenchain.apiinterface;

import com.google.gson.JsonObject;
import com.hufuinfo.hufudigitalgoldenchain.bean.CertRequstReslut;
import com.hufuinfo.hufudigitalgoldenchain.bean.CertifaicationResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.PaymentResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.RequestResult;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface UserLogin {
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/userLogin.do")
    Observable<RequestResult> login(@Body RequestBody info);

    /**
     * 获取支付二维码
     *
     * @param antiFake 登录状态码
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/getQRCode.do")
    Observable<RequestResult> getQRCode(@Header("antiFake") String antiFake);

    /**
     * 获取支付二维码
     *
     * @param info
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/getQRCode.do")
    Observable<JsonObject>getQRCode(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 查询支付结果
     *
     * @param antiFake 登录状态码
     * @param info     {"orderId", "12345678"},加密封装{"hufuCode","'},
     *                 创建 new json()
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/confirmationPay.do")
    Observable<RequestResult> confirmationPay(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 在线获取用户证书
     */

    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/getCertificationOnline.do")
    Observable<CertifaicationResult> getCertificationOnline(@Header("antiFake") String antiFake, @Body RequestBody info);

    /**
     * 安装证书成功回执
     *
     * @param antiFake 登录状态码
     * @param hufuCode 回执类型  0代表手机APP，1代表PC端
     * @return
     */
    @Headers({"Content-Type:application/json", "Accept:application/json"})
    @POST("business_demo/cmaservice/installCertificateSuccessful.do")
    Observable<CertRequstReslut> installCertificateSuccess(@Header("antiFake") String antiFake, @Body RequestBody hufuCode);
}
