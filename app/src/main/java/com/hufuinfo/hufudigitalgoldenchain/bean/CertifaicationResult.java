package com.hufuinfo.hufudigitalgoldenchain.bean;

public class CertifaicationResult {
    public boolean success;
    public String msg;
    public CertificationData data;

    public static class CertificationData {
        /**
         * 申请时是用预置证书公钥进行加密后的BASE6编码的用户证书私钥
         */
        public String certification;
        /**
         * 预制证书
         */
        public String pkm;

        public String check;
    }
}
