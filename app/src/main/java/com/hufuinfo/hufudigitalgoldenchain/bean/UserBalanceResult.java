package com.hufuinfo.hufudigitalgoldenchain.bean;

public class UserBalanceResult {
    public boolean success;
    public String msg;
    public Data data;

    public static class Data {
        public UserInfo userInfo;
    }

    public static class UserInfo {
        public int id;
        public float balance;
        public int userId;
        public float rewardAmount;
        public float rechargeAmount;
        public float creditAmount;
        public float freezeAmount;
        public float totalReward;

    }

}
