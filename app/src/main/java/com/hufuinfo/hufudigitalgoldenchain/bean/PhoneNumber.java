package com.hufuinfo.hufudigitalgoldenchain.bean;

public class PhoneNumber {
    private String mobile;
    /**
     * 虎符盾牌芯片ID
     */
    private String keyId;

    /**
     * 加密信息
     */
    private String code;

    public PhoneNumber(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
