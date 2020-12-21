package com.hufuinfo.hufudigitalgoldenchain.bean;

public class RechargeRecord {
    private String mobile;
    private int page;
    private int rows;
    /**
     * 0：时间倒序，1：时间正序，2交易金额倒序，3：交易金额正序0：时间倒序，1：时间正序，2交易金额倒序，3：交易金额正序
     */
    private int vouchSort;

    public RechargeRecord(String mobile, int page, int rows) {
        this.mobile = mobile;
        this.page = page;
        this.rows =rows;
    }

    public void setVouchSort(int vouchSort) {
        this.vouchSort = vouchSort;
    }
}
