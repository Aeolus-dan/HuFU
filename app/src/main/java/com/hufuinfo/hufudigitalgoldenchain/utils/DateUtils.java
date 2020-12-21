package com.hufuinfo.hufudigitalgoldenchain.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    /**
     * 获取时间戳   * 输出结果:1438692801766
     */

    public static long getNowTimeStamp() {
        Date date = new Date();
        long times = date.getTime();
        return times;
    }

    /**
     * 获取格式化的时间   * 输出格式：2015-08-04 20:55:35
     */
    public static String getFormatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }


    public static String getStringFloat(float mflaot) {
        DecimalFormat df = new DecimalFormat("#########.##");
        return df.format(mflaot);
    }

    /**
     *
     */

    public static String getYear() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy");
        return format.format(date);
    }

    /**
     * @param date
     * @return
     */
    public static String getFormateDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date mdate = formatter.parse(date);
            SimpleDateFormat formatterS = new SimpleDateFormat("yyyy/MM/dd");
            return formatterS.format(mdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 转换 Double数据为String
     *
     * @param data double数据
     * @return 如果为整数返回  .0,  小数返回 .00
     */
    public static String getStringDouble(double data) {
        BigDecimal bigDecimal = new BigDecimal(data).setScale(2, RoundingMode.HALF_UP);
        double num = bigDecimal.doubleValue();
        if (Math.round(num) - num == 0) {
            return String.valueOf(bigDecimal);
        }
        return String.valueOf(bigDecimal);
    }

    /**
     * 隐藏手机号码中的4位号码
     * if  String  lennth<7 reutrn "admin****"
     *
     * @param phone 手机号码  17316276965
     * @return 返回   173****6965   admin
     */
    public static String getStringHidePhone(String phone) {
        char[] phoneChar = phone.toCharArray();
        if (phoneChar.length < 6)
            return "adm****";
        for (int i = 3; i < 7; i++) {
            phoneChar[i] = '*';
        }
        return new String(phoneChar);
    }


}
