package com.hufuinfo.hufudigitalgoldenchain.bean;

public class RegisterInfoUser {
    /**
     * 申请人手机号
     */
    public String mobile;
    /**
     * 登录密码
     */
    public String password;
    /**
     * 推存人手机号
     */
    public String associatedId;
    /**
     * 手机验证短信
     */
    public String pin;

    public RegisterInfoUser(String moblie, String password, String associatedId, String pin) {
        this.mobile = moblie;
        this.password = password;
        this.associatedId = associatedId;
        this.pin = pin;
    }
}
