/**
 * Copyright (c) 2015-2016 Nationz
 *
 */
/**zhai.yuehui
 * 2016-6-22
 * 下午2:07:45
 */
package com.hufu.nzble;
/**
 * @author 作者 E-mail:zhai.yuehui@nationz.com.cn
 * @version 创建时间：2016-6-22 下午2:07:45
 * 类说明
 */
public class ToastEvent {
	private String toast;

	public ToastEvent(String tip) {
		this.toast = tip;
	}

	/**
	 * @return the toast
	 */
	public String getToast() {
		return toast;
	}

	/**
	 * @param toast the toast to set
	 */
	public void setToast(String toast) {
		this.toast = toast;
	}


}
