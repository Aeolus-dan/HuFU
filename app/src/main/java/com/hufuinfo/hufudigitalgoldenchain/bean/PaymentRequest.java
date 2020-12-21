package com.hufuinfo.hufudigitalgoldenchain.bean;

public class PaymentRequest {
    /**
     * 用户手机号
     */
    private String mobile;
    /**
     * 硬件序列号
     */
    private String keyId;
    /**
     * 人民币价格
     * 不能用科学计数法
     */
    private float price;
    /**
     * 金本币数量
     */
    private String goldNo;
    /**
     * 交易类型
     * 1：平台充值
     * 2：平台代扣
     * 3：百倍充值
     */
    private int type;
    /**
     * 签名数据的原始数据
     */
    private String oldStr;
    /**
     * 签名数据
     */
    private String safeStr;

    public PaymentRequest() {

    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setGoldNo(String goldNo) {
        this.goldNo = goldNo;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setOldStr(String oldStr) {
        this.oldStr = oldStr;
    }

    public void setSafeStr(String safeStr) {
        this.safeStr = safeStr;
    }
}
