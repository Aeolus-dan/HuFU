package com.hufuinfo.hufudigitalgoldenchain.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CombinationSecretKey {

    /**
     * 对称密钥规则为url与时间拼接ex:访问url为 sentMessage.do
     * 时间为 2018/11/01/
     * 则对称密钥为
     * 2018/11/01/ag.d^&&^2018/11/01/ss.o
     * 共32个字节 对称密钥由客户端产生不存储
     * 客户端用此对称密钥对所有请求参数进行封装json,j
     * son对象名称叫sy 服务端以相同规则进行对称解密
     * 解密失败则被认为无效请求 解密成功方为有效请求。
     * <p>
     * ag.d  取 . 左侧的倒数第2位和倒数第3位 还有右侧的
     * 第一位 ss.o取的是.左侧的倒数第四、第五位 右侧的第二位
     *
     * @param combinationStr 接口的最后字符串 例如："sentMessage.do"
     * @return
     */
    public static String getSecretKey(String combinationStr) {
        String dateStr = getDate();
        String[] dividerStr = combinationStr.split("\\.");
        if (dividerStr.length < 2) {
            return null;
        }
        char[] firstDivider = dividerStr[0].toCharArray();
        char[] scondDivider = dividerStr[1].toCharArray();
        int firstLen = firstDivider.length;
        if (firstLen < 5) {
            return null;
        }
        String firstStr = String.valueOf(firstDivider[firstLen - 3]) + firstDivider[firstLen - 2] +
                "." + scondDivider[0];
        String secondStr = String.valueOf(firstDivider[firstLen - 5]) + firstDivider[firstLen - 4] +
                "." + scondDivider[1];
        String secretKey = dateStr + "/" + firstStr + "^&&^" + dateStr + "/" + secondStr;
        return secretKey;
    }

    /**
     * 返回当前的日期。
     * 例如： "2018/11/05"
     *
     * @return 日期字符串
     */
    public static String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String resutlStr = simpleDateFormat.format(date);
        return resutlStr;
    }

    /**
     * 获取创建订单原始数据类型
     * 对原始数据进行签名，原始数据不用传。
     * 原始数据格式固定为
     * “买方:X,卖方:Y,交易类型:0,交易时间: 2018/12/5,交易金额:”或
     * “买方:X,卖方:system,交易类型:1交易时间: 2018/12/05,交易金额:”
     *
     * @param buyer      买方账号
     * @param seller     卖方账号
     * @param tranType   交易类型
     * @param tranAmount 交易金额
     * @return 签名数据格式
     */
    public static String getSignOriginalData(String buyer, String seller, int tranType, String tranAmount) {
        String tranTime = getDate();
        StringBuffer singOriginal = new StringBuffer();
        singOriginal.append("买方:").append(buyer)
                .append(",卖方:").append(seller)
                .append(",交易类型:").append(tranType)
                .append(",交易时间:").append(tranTime)
                .append(",交易金额:").append(tranAmount);
        return singOriginal.toString();
    }


    /**
     * 获取创建订单原始数据类型
     * 对原始数据进行签名，原始数据不用传。
     * 原始数据格式固定为
     * “买方:X,卖方:Y,交易类型:0,交易时间: 2018/12/5,交易金额:”或
     * “买方:X,卖方:system,交易类型:1交易时间: 2018/12/05,交易金额:”
     *
     * @param buyer      买方账号
     * @param seller     卖方账号
     * @param tranType   交易类型
     * @param tranAmount 交易金额
     * @param createData 交易创建时间
     * @return 签名数据格式
     */
    public static String getSignOriginalData(String buyer, String seller, int tranType, String tranAmount, String createData) {
        return ("买方:") + (buyer) +
                (",卖方:") + (seller) +
                (",交易类型:") + (tranType) +
                (",交易时间:") + (createData) +
                (",交易金额:") + (tranAmount);
    }

    /**
     * 充值信息签名 原始数据生成
     *
     * @param mobile      用户账号
     * @param goldNo      充值的金本币数
     * @param paymentType 充值类型  1:平台充值， 2:平台代扣,3: 百倍充值
     * @return 充值的原始签名数据
     */
    public static String getSignPaymentData(String mobile, String goldNo, int paymentType) {
        String paymentDate = getDate();   //充值日期  例如：2018/12/21
        StringBuilder paymentData = new StringBuilder();
        paymentData.append("充值账号:").append(mobile)
                .append(",充值金额:").append(goldNo)
                .append(",充值类型:").append(paymentType)
                .append(",充值时间:").append(paymentDate);
        return paymentData.toString();
    }

    public static String getRevokeOrderSignData(String mobile, String firmId) {
        return "撤销方:" + mobile + ",撤销订单号:" + firmId;
    }

    /**
     * 组装 Gson()  {code:"dadffffadf"}
     *
     * @param code 要组合的数据
     * @return 返回组合后的Gson  String
     */
    public static String assembleJsonCode(String code) {
        JsonObject jsonObject = new
                JsonObject();
        jsonObject.add("code", new JsonPrimitive(code));
        String requestCode = new Gson().toJson(jsonObject);
        return requestCode;
    }


    public static String assembleJsonMobile(String code) {
        JsonObject jsonObject = new
                JsonObject();
        jsonObject.add("mobile", new JsonPrimitive(code));
        return new Gson().toJson(jsonObject);
    }

    public static String assembleJsonFirmId(String firmId) {
        JsonObject jsonObject = new
                JsonObject();
        jsonObject.add("firmId", new JsonPrimitive(firmId));
        return new Gson().toJson(jsonObject);
    }
}
