package com.hufuinfo.hufudigitalgoldenchain.bean;

public class PaymentResult {

    public boolean success;
    public String msg;
    public PaymentData data = null;

    public static class PaymentData {
        public String QRCode;
        public String orderId;
    }
}
