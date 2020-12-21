package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DeviceUtils {

    public static String getUniqueId(Context context) {
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return id;
    }

    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] digest = messageDigest.digest(text.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            int digestInt = digest[i] & 0xff;
            String hexString = Integer.toHexString(digestInt);
            if (hexString.length() < 2) {
                sb.append(0);
            }
            sb.append(hexString);
        }

        return sb.toString();
    }
}
