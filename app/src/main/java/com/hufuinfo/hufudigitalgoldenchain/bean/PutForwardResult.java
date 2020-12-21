package com.hufuinfo.hufudigitalgoldenchain.bean;

public class PutForwardResult {
    public boolean success;
    public  String msg;
    public Data data;

    public static class Data {
        public double goldNo;
        public double cny;
        public double advanceGoldNo;
    }
}
