package com.hufuinfo.hufudigitalgoldenchain.bean;

public class RevokeOrderQuest {
    /**
     * 订单号
     * 创建订单时返回参数
     */
    private String firmId;
    /**
     * 用户标识
     * 订单确认人手机号
     */
    private String mobile;
    /**
     * 硬件签名数据
     * 订单确认人的硬件签名数据
     */
    private String safeStr;
    /**
     * 买卖状态值
     * 0：作为卖方确认订单
     * 1：作为买方确认订单
     */
    private int status;
    /**
     * 交易方式  0：点对点交易 1：挂盘交易
     */
    private int transactionType;
    /**
     * 安全设备证书ID
     */
    private String keyId;
    /**
     * 签名原始数据
     */
    //  private String oldStr;
    /**
     * 订单发起人 账号（手机号）
     */
    private String appointMobile;

    /**
     * 交易金额
     */
    private double transactionAmount;

    public RevokeOrderQuest() {
    }

    public void setFirmId(String firmId) {
        this.firmId = firmId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setSafeStr(String safeStr) {
        this.safeStr = safeStr;
    }

    public String getSafeStr() {
        return safeStr;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

   /* public void setOldStr(String oldStr) {
        this.oldStr = oldStr;
    }*/

    public void setAppointMobile(String appointMobile) {
        this.appointMobile = appointMobile;
    }

    public String getAppointMobile() {
        return appointMobile;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public String getFirmId() {
        return firmId;
    }
}
