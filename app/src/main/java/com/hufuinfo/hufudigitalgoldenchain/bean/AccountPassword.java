package com.hufuinfo.hufudigitalgoldenchain.bean;

public class AccountPassword {
    private String oldPwd;
    private String newPwd;
    private String mobile;

    public AccountPassword(String oldPwd, String newPwd, String mobile) {
        this.oldPwd = oldPwd;
        this.newPwd = newPwd;
        this.mobile = mobile;
    }
}
