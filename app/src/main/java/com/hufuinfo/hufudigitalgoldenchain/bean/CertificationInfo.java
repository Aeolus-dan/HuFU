package com.hufuinfo.hufudigitalgoldenchain.bean;

/**
 * 在线申请证书请求参数
 */
public class CertificationInfo {
    /**
     * 手机唯一标识
     */
    private String keyId;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 2、	app_acquisition 代表手机端实名注册时获取证书；
     * 3、	app_ acquisition_again 代表手机端再次获取证书
     */
    private int name;
    /**
     * 短信验证码
     */
    private String pin;

    public CertificationInfo(String keyId, String mobile, int
            name) {
        this.keyId = keyId;
        this.mobile = mobile;
        this.name = name;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setName(int name) {
        this.name = name;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
