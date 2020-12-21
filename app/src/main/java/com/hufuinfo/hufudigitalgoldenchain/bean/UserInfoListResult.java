package com.hufuinfo.hufudigitalgoldenchain.bean;

public class UserInfoListResult {
    public boolean success;
    public String msg;
    public Data data;

    public static class Data {
        public UserInfo userInfo;

        public UserInfo getUserInfo() {
            return userInfo;
        }

    }


    public static class UserInfo {
        public int id;
        public String name;
        public String password;
        public String email;
        public String mobile;
        public String status;
        public String associatedId;
        public String idCard;
        public int sex;
        public String  bankCard;
    }

}
