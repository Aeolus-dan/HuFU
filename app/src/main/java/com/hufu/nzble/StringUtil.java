/**
 * Copyright (C) 2015-2016 Nationz
 *
 */
package com.hufu.nzble;

import android.text.TextUtils;

/**
* @author ???? E-mail:cheng.xianxiong@nationz.com.cn
* @version ???????2016??3??31?? ????10:20:52
* ?????
*/
public class StringUtil {

	// 16 String to byte[](16 jingzhi String to byte[])
	public static  byte[] hex2byte(String hex)
			throws IllegalArgumentException {
		if (!TextUtils.isEmpty(hex)) {
			if (hex.length() % 2 != 0) {
				throw new IllegalArgumentException();
			}
			char[] arr = hex.toCharArray();
			byte[] b = new byte[hex.length() / 2];
			for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
				String swap = "" + arr[i++] + arr[i];
				int byteint = Integer.parseInt(swap, 16);
				b[j] = (byte) byteint;
			}
			return b;
		}

		return null;
	}
	//
	public static String byte2String(byte[] bytes){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bytes.length ; i++){
			sb.append(String.format("%02X", bytes[i]));
		}
		return sb.toString();
	}
}
