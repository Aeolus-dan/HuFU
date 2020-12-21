package com.hufuinfo.hufudigitalgoldenchain.bean;


public class UpdateAppResult {
    public boolean success;
    public String msg;
    public UpdateAppData data;

    public static class UpdateAppData {
        public boolean enforcement;
        public int versionCode;
        public String updateInfo;
        public String versionName;
    }
}
