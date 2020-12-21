package com.cpk.jni;


public class CpkAPI {
	
	//对称加解密
	public static final int ALG_AES_ECB_128 = 0x0101;
	public static final int ALG_AES_ECB_256 = 0x0102;
	public static final int ALG_AES_CBC_128 = 0x0103;
	public static final int ALG_AES_CBC_256 = 0x0104;
	public static final int ALG_SM4_ECB = 0x0105;
	public static final int ALG_SM4_CBC = 0x0106;	
	public static final int ALG_DES = 0x0107;
	public static final int ALG_TRIDES = 0x0108;
	
	//哈希
	public static final int ALG_SHA1 = 0x0201;
	public static final int ALG_SHA256 = 0x0202;
	public static final int ALG_SHA512 = 0x0203;
	public static final int ALG_SM3 = 0x0204;
	public static final int ALG_MD5 = 0x0205;
	public static final int ALG_HMAC = 0x0206;

	
	/**对称加密算法
	 * @param alg_func：使用的对称加密算法
	 * @param key:对称密钥
	 * @param input:要加密的数据
	 * @return 返回二维数组。
	 * 			第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二维数组为密文数组。
	 */
	public native byte[][] EncryptData(int alg_func, byte[] key, byte[] input);
	
	/**对称解密算法
	 * @param alg_func：使用的对称解密算法
	 * @param key:对称密钥
	 * @param input:要解密的数据
	 * @return 返回二维数组。
	 * 			第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二维数组为解密后的明文数组。
	 */
	public native byte[][] DecryptData(int alg_func, byte[] key, byte[] input);

	/**哈希算法
	 * @param hash_id：使用的哈希算法
	 * @param input:要做哈希的数据
	 * @return 返回二维数组。
	 * 			第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二维数组为哈希值。
	 */
	public native byte[][] HASH(int hash_id, byte[] input);
	
	/**
	 * 将数组里的数据转为整数值。
	 * 
	 * @param num:要转为int值的数组。
	 * @return -1：转换失败,可能的错误原因：num长度不为4字节。其他值：转换后的int值。
	 */
	public native int GetIntValue(byte[] num);	
	
	/**获得密文信息
	 * @param input:密文
	 * @return 返回二维数组。
	 * 			第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二行数组为版本信息，由jni.GetIntValue()函数获得。
	 * 			第三行数组为矩阵标识。
	 * 			第四行为接收方标识。
	 */
	public native byte[][] CPK_GetCipherInfo(byte[] input);
	
	/**获得签名信息
	 * @param signature:签名数据
	 * @return 返回二维数组。
	 * 			第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二行数组为版本信息，由jni.GetIntValue()函数获得。
	 * 			第三行数组为矩阵标识。
	 * 			第四行为发送方标识。
	 */
	public native byte[][] CPK_GetSignatureInfo(byte[] signature);
	
		
	
	/**生成随机数
	 * @param len：生成随机数的字节数
	 * @return 返回二维数组。
	 * 			第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二维数组为随机数数组。
	 */
	//public native byte[][] CPK_Random(int len);
	
	/**初始化sm2曲线
	 * @return 0:成功，其他值：失败。
	 */
	//public native int CPK_Init();
	
	/**释放sm2所占资源
	 */
	//public native void CPK_End();
	
	/**导入公钥矩阵
	 * @param pubmatrix：公钥矩阵
	 * @return 0：成功。其他值：失败。
	 */
	//public native int ImportPubMatrix(byte[] pubmatrix);
	
	/**sm2非对称加密，使用接收方标识加密数据
	 * @param receiver：接收方标识
	 * @param input:要加密的数据
	 * @return 返回二维数组。
	 * 			第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。  
	 * 			第二维数组为输出的密文。
	 */
	//public native byte[][] CPK_SM2_Encrypt(byte[] receiver, byte[] input);
	
	/**sm2验证签名
	 * @param sender：发送方标识
	 * @param input:做签名前的原始数据
	 *@param sign:签名数据
	 * @return 0：成功。其他值：失败
	 */
	//public native int CPK_SM2_VerifySign(byte[] sender, byte[] input, byte[] sign);
	
	/**
	 * sm4解密用户证书，并验证私钥mac
	 * 
	 * @param key:对称密钥
	 * @param input:要解密的数据
	 * @return 返回二维数组。 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
	 *         第二维数组为解密后的明文数组。
	 */
	//public native byte[][] CPK_DecryptCert(byte[] key, byte[] input);

	//public native byte[][] CPK_SM2_Sign(byte[] input, byte[] cert);

	//public native byte[][] CPK_SM2_Decrypt(byte[] input, byte[] cert);

}
