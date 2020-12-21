package com.cpk.jni;
/**错误信息
 * */
public class ErrorInfo {
	/**执行成功 */
	public static final int  CPK_OK                  = 0;
	
	/**算法ID错误*/
	public static final int  ERROR_ALGORITHM        = 1;
	
	/**密钥或者密钥长度不正确*/
	public static final int  ERROR_KEY              = 2;
	
	/**输入数据或者长度不正确*/
	public static final int  ERROR_INPUT            = 3;
	
	/**输出空间不足*/
	public static final int  ERROR_OUTPUT           = 4;
	
	/**初始化CTX参数失败*/
	public static final int  ERROR_CTX	            = 5;
	
	/**曲线未初始化*/
	public static final int  ERROR_INIT	            = 6;
	
	/**空矩阵*/
	public static final int  ERROR_NULL_MATRIX      = 7;
	
	/**计算ecc key 失败*/
	public static final int  ERROR_EC_KEY           = 8;
	
	/**加密失败*/
	public static final int  ERROR_ENCRYPT          = 9;
	
	/**解密失败*/
	public static final int  ERROR_DECRYPT          = 10;
	
	/**结构转换失败，数据内容不正确*/
	public static final int  ERROR_STRUCT_CONVERT   = 11;
	
	/**接收方标识或者矩阵标识不对应*/
	public static final int  ERROR_RECEIVER		    = 12;
	
	/**证书数据不正确或者index不存在*/
	public static final int  ERROR_CERT             = 13;
	
	/**签名失败*/
	public static final int  ERROR_SIGNATURE        = 14;
	
	/**验签失败*/
	public static final int  ERROR_VERIFY_SIGNATURE = 15;
	
	/**没有矩阵签名或者签名长度不正确*/
	public static final int  ERROR_NO_MATRIX_SIGN   = 16;
	
	/**无效曲线*/
	public static final int  ERROR_INVALID_CURVE    = 17;
	
	/**生成矩阵失败*/
	public static final int  ERROR_GENERATE_MATRIX  = 18;
	
	/**导入矩阵失败,pin码或者矩阵数据不正确*/
	public static final int  ERROR_IMPORT_MATRIX    = 27;
	
	/**管理证书解密对称密钥失败*/
	public static final int  ERROR_MANAGER_DECRYPT  = 28;
	
	/**设备未找到或者设备类型错误*/
	public static final int  ERROR_DEV_NOT_FOUND    = 257;
	
	/**pin码错误*/
	public static final int  ERROR_PIN              = 258 ;
	
	/**获取证书列表失败*/
	public static final int  ERROR_GET_CERT_LIST    = 259 ;
	
	/**设备锁定*/
	public static final int  ERROR_DEV_LOCKED       = 260 ;
	
	/**错误的用户类型*/
	public static final int  ERROR_USER_TYPE        = 261 ;
	
	/**错误的设备类型*/
	public static final int  ERROR_DEV_TYPE         = 262 ;
	
	/**权限错误*/
	public static final int  ERROR_NO_PERMISSION    = 263 ;
	
	/**无证书*/
	public static final int  ERROR_NO_CERT          = 264 ;
	
	/**算法错误*/
	public static final int  ERROR_ALG              = 265 ;
	
	/**获取芯片id失败*/
	public static final int  ERROR_GET_ID           = 266;
	
	/**重复登陆*/
	public static final int  ERROR_RELOGIN	        = 267;
	
	/**需要先登录*/
	public static final int  ERROR_NEED_LOGIN       = 268;
	
	/**导入证书失败*/
	public static final int  ERROR_IMPORT_CERT      = 269;
	
	/**获得随机数失败*/
	public static final int  ERROR_GET_RANDOM       = 270;
	
	/**获得文件列表失败*/
	public static final int  ERROR_GET_FILE_LIST    = 271;
	
	/**文件已存在*/
	public static final int  ERROR_FILE_EXIST       = 272;
	
	/**请求发送失败,稍后重试，或者重新插拔key*/
	public static final int  ERROR_DEVICE_REQ_FAILED	= 20482;
}
