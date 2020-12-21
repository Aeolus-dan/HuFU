package com.hufuinfo.hufudigitalgoldenchain.bean;

public class AuthPriceResult {
    public boolean success;
    public String msg;
    public Data data;

    public static class Data {
        public float averagePrice;
        public float internatPrice;
        public float officePrice;
    }

}
