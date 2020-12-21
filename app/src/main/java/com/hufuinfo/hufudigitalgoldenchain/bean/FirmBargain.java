package com.hufuinfo.hufudigitalgoldenchain.bean;

public class FirmBargain {
    private String mobile;
    private int status;
    private String safeStr;
    private double transactionAmount;
    private int transactionType;
    private String appointMobile;
    private String noteInformation;
    private String keyId;
    private String oldStr;
    private float price;

    public float getPrice() {
        return price;
    }

    public FirmBargain() {
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSafeStr(String safeStr) {
        this.safeStr = safeStr;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public int getTransactionType() {
        return this.transactionType;
    }

    public int getStatus() {
        return status;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setAppointMobile(String appointMobile) {
        this.appointMobile = appointMobile;
    }

    public String getAppointMobile() {
        return appointMobile;
    }

    public void setNoteInfomation(String noteInformation) {
        this.noteInformation = noteInformation;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setOldStr(String oldStr) {
        this.oldStr = oldStr;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
