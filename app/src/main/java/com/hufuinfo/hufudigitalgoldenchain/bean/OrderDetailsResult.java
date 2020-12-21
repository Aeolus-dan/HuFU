package com.hufuinfo.hufudigitalgoldenchain.bean;

import java.util.List;

public class OrderDetailsResult {
    public boolean success;
    public String msg;
    public Data data;

    public static class Data {
        int total;
        List<TransactionOrderInfo> list;

        public List<TransactionOrderInfo> getList() {
            return list;
        }

        public int getTotal() {
            return total;
        }
    }


}
