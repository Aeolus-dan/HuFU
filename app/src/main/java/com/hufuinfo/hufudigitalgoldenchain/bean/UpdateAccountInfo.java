package com.hufuinfo.hufudigitalgoldenchain.bean;


public class UpdateAccountInfo {
    private String mobile;
    private String idCard;
    private String bankId;


    public UpdateAccountInfo(String mobile, String idCard, String bankId) {
        this.mobile = mobile;
        this.idCard = idCard;
        this.bankId = bankId;
    }
}
