package com.hufuinfo.hufudigitalgoldenchain.bean;

public class AttornGoldRequest {
    /**
     * 收款方数据签名
     * 生成二维码的收款方签名数据
     */
    private String safeBuyStr;
    /**
     * 积分数量
     * 需要支付的积分数量
     */
    private double integral;
    /**
     * 扫描二维码的用户签名
     */
    private String safeSellStr;
    /**
     * 收款方
     * 二维码生成方
     */
    private String gathUsr;

    public void setSafeBuyStr(String safeBuyStr) {
        this.safeBuyStr = safeBuyStr;
    }

    public void setIntegral(double integral) {
        this.integral = integral;
    }

    public void setSafeSellStr(String safeSellStr) {
        this.safeSellStr = safeSellStr;
    }

    public void setGathUsr(String gathUsr) {
        this.gathUsr = gathUsr;
    }
}
