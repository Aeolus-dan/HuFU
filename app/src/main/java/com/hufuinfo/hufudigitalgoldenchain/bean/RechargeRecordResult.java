package com.hufuinfo.hufudigitalgoldenchain.bean;

import java.util.List;

public class RechargeRecordResult {
    public String msg;
    public boolean success;
    public Data data;

    public static class Data {
        private int total;
        private List<UserVoucherCenter> list;

        public int getTotal() {
            return total;
        }

        public List<UserVoucherCenter> getUserVoucherCenter() {
            return list;
        }
    }

    public static class UserVoucherCenter {
       // public int id;
        //public float voucherAmount;
        public String voucherTime;
        public String voucherAccount;
        public int status;
        public double goldNo;
        public double beforeBalance;

    }
}
