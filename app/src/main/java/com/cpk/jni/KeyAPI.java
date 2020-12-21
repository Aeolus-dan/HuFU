package com.cpk.jni;

public class KeyAPI {
	
	public static final int KEY_AES_ECB_128 = 0x0101;
	public static final int KEY_SM4_ECB = 0x0105;
	public static final int KEY_DES = 0x0107;
	public static final int KEY_SHA1 = 0x0201;
	public static final int KEY_SM3 = 0x0204;
	/**查找设备
	 * @param path:设备路径。
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_OpenPath(byte[] path);

	/**查找usb key设备
	 * @param type:usb key: 1。
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_Open(int type);
	
	/**
	 * 验证pin码，登录设备,如果验证失败次数超过限制次数，设备将会被锁定无法使用。
	 * 
	 * @param userType:用户类型。1：用户。
	 * @param pin:pin码。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为验证pin码失败后剩余次数，由jni.GetIntValue()函数获得。
	 */
	public native byte[][] Dev_VerifyPin(int userType, byte[] pin);

	/**
	 * 获得设备ID。
	 * 
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为设备ID。
	 */
	public native byte[][] Dev_GetID();

	/**
	 * 设备产生随机数。
	 * 
	 * @param len:随机数数组长度。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为随机数数组。
	 */
	public native byte[][] Dev_GetRandom(int len);

	/**
	 * 修改设备pin码,Pin码为8位可见字符。
	 * 
	 * @param userType:用户类型。1：用户。
	 * @param oldPin:原pin码。
	 * @param newPin:新pin码。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为验证pin码后剩余尝试次数，由jni.GetIntValue()函数获得。
	 */
	public native byte[][] Dev_ChangePin(int userType, byte[] oldPin, byte[] newPin);

	/**
	 * 获得用户证书列表。
	 * 
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为用户证书个数，由jni.GetIntValue()函数获得。 第三行为第一个用户证书，结构为
	 *         索引+空格+用户标识+空格+矩阵标识。 第四行为第二个用户证书，结构为 索引+空格+用户标识+空格+矩阵标识，依次类推
	 */
	public native byte[][] Dev_GetCertList();
	
	/**
	 * 非对称加密
	 * 
	 * @param receiver:接收方标识。
	 * @param data:明文数据。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为密文。
	 */
	public native byte[][] Dev_SM2_Encrypt(byte[] receiver, byte[] data);

	/**
	 * 使用设备用户证书解密数据。
	 * 
	 * @param input:要解密的密文。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为解密后的明文。
	 */
	public native byte[][] Dev_SM2_Decrypt(byte[] input);

	/**
	 * 使用设备用户证书生成签名。
	 * 
	 * @param certIndex:用户证书索引。
	 * @param input:要签名的数据。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为生成的签名。
	 */
	public native byte[][] Dev_SM2_Sign(int certIndex, byte[] input);
	
	/**
	 * SM验证签名
	 * 
	 * @param data:生成签名时输入的原文。
	 * @param sign:签名数据。
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_SM2_VerifySign(byte[] data, byte[] sign);

	/**
	 * 关闭设备。
	 * 
	 */
	public native void Dev_Close();

	/**
	 * 将数组里的数据转为整数值。
	 * 
	 * @param num:要转为int值的数组。
	 * @return -1：转换失败,可能的错误原因：num长度不为4字节。其他值：转换后的int值。
	 */
	public native int GetIntValue(byte[] num);	
	/**
	 * 获得公钥矩阵列表
	 *
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为列表个数，第三行为第一个矩阵名称依次往下排。
	 */
	public native byte[][] Dev_GetPubMatrixList();
	
	/**
	 * 导入用户证书
	 * 
	 * @param userCert:接收到的用户证书密文
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_ImportUserCert(byte[] userCert);
	
	/**
	 * 导入预置证书
	 * 
	 * @param preCert:接收到的预置证书
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_ImportPreCert(byte[] preCert);

	/**
	 * 导入公钥矩阵
	 * 
	 * @param pubMatrix:接收到的公钥矩阵
	 * @return 0：成功。其他值：失败。
	 */
	public native int Dev_ImportPubMatrix(byte[] pubMatrix);
	
	/**
	 * 哈希函数
	 * 
	 * @param algID:算法ID。
	 * @param data:做哈希的数据。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为哈希值。
	 */
	public native byte[][] Dev_Hash(int algID, byte[] data);
	
	/**
	 * 对称加密函数
	 * 
	 * @param algID:算法ID。
	 * @param key:对称密钥。
	 * @param data:做哈希的数据。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为密文。
	 */
	public native byte[][] Dev_Encrypt(int algID, byte[] key, byte[] data);
	
	/**
	 * 对称解密函数
	 * 
	 * @param algID:算法ID。
	 * @param key:对称密钥。
	 * @param data:做哈希的数据。
	 * @return 返回二维数组。 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二行为解密后的数据。
	 */
	public native byte[][] Dev_Decrypt(int algID, byte[] key, byte[] data);
	
	/**获得SM2密文信息
	 * @param input:密文
	 * @return 返回二维数组。
	 * 			第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二行数组为版本信息，由jni.GetIntValue()函数获得。
	 * 			第三行数组为矩阵标识。
	 * 			第四行为接收方标识。
	 */
	public native byte[][] Dev_GetCipherInfo(byte[] input);
	
	/**获得SM2签名信息
	 * @param signature:签名数据
	 * @return 返回二维数组。
	 * 			第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二行数组为版本信息，由jni.GetIntValue()函数获得。
	 * 			第三行数组为矩阵标识。
	 * 			第四行为发送方标识。
	 */
	public native byte[][] Dev_GetSignatureInfo(byte[] signature);

	/*清除所有用户证书
	 */
	public native int Dev_ClearCert();
}
