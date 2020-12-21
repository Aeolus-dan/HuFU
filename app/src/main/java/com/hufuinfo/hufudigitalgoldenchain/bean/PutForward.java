package com.hufuinfo.hufudigitalgoldenchain.bean;

public class PutForward {
    private String mobile;
    private int status;
    private double priceNum;
    private int type;
    public PutForward(){

    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPriceNum(double priceNum) {
        this.priceNum = priceNum;
    }

    public void setType(int type) {
        this.type = type;
    }
}
