package com.hufuinfo.hufudigitalgoldenchain.bean;

public class OrderInfoResult {

    public String msg;
    public boolean success;
    public Data data;

    public static class Data {
        public TransactionOrderInfo firm;
    }
}
