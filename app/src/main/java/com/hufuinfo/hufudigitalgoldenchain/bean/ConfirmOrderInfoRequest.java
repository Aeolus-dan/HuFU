package com.hufuinfo.hufudigitalgoldenchain.bean;

/**
 * 用户确认交易订单的时候,
 * client向服务器发送的原始请求数据。
 * 通过Gson封装后SM2对称加密数据
 * body {"code":"密文"}
 */
public class ConfirmOrderInfoRequest {
    /**
     * 用户标识
     * 订单确认人手机号
     */
    private String mobile;
    /**
     * 买卖状态值
     * 0.作为卖方确认订单
     * 1.作为买方确认订单
     */
    private int status;
    /**
     * 签名数据
     * 验证对方签名数据通过时，以对方签名数据的原始数据进行签名当
     * 对方签名原始数据为挂盘甲乙(交易方式是1时)
     * 则将原始数据的system改为当前用户手机号如果交易方式为点对点交易时签名数据不变。
     * <p>
     * 创建订单时的签名原始数据为
     * 买方:18701682983,卖方:system,交易类型:0,交易时间: 2018/12/5,交易金额:1.0
     * 则确认订单数据为
     * 买方:18701682983,卖方:【当前手机号】,交易类型:0,交易时间: 2018/12/5,交易金额:1.0
     */
    private String safeStr;

    /**
     * 订单号
     * 订单号，创建订单时返会参数
     */
    private String firmId;
    /**
     * 硬件标识Id
     */
    private String keyId;

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSafeStr(String safeStr) {
        this.safeStr = safeStr;
    }

    public void setFirmId(String firmId) {
        this.firmId = firmId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}
