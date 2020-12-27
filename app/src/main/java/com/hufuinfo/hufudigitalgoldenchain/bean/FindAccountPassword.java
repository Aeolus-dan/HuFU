package com.hufuinfo.hufudigitalgoldenchain.bean;

public class FindAccountPassword {

    private String mobile;
    private String password;
    private String smsCode;

    public FindAccountPassword(String mobile, String password, String smsCode) {
        this.mobile = mobile;
        this.password = password;
        this.smsCode = smsCode;
    }
}
