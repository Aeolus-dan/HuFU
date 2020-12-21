package com.hufuinfo.hufudigitalgoldenchain.bean;

/**
 * 完善个人信息
 * 请求参数
 */
public class ConsummateInfo {
    /**
     * 用户标识
     */
    private String mobile;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 虎符盾牌芯片Id
     */
    private String keyId;
    /**
     * 真实姓名
     */
    private String name;
    /**
     * 支付密码
     */
    private String payPwd;

    /**
     * 银行卡号
     */
    private String bankCard;

    public ConsummateInfo() {
    }

    public void setBankCard(String bankCard) {
        this.bankCard = bankCard;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPayPwd(String payPwd) {
        this.payPwd = payPwd;
    }
}
