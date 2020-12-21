package com;

public class hfcpk_jni {

    public static final int ALG_AES_ECB_128 = 0x0101;
    public static final int ALG_AES_ECB_256 = 0x0102;
    public static final int ALG_AES_CBC_128 = 0x0103;
    public static final int ALG_AES_CBC_256 = 0x0104;
    public static final int ALG_SM4_ECB = 0x0105;
    public static final int ALG_SM4_CBC = 0x0106;
    public static final int ALG_DES = 0x0107;
    public static final int ALG_TRIDES = 0x0108;

    public static final int ALG_SHA1 = 0x0201;
    public static final int ALG_SHA256 = 0x0202;
    public static final int ALG_SHA512 = 0x0203;
    public static final int ALG_SM3 = 0x0204;
    public static final int ALG_MD5 = 0x0205;
    public static final int ALG_HMAC = 0x0206;

    /**
     * 生成随机数
     *
     * @param len：生成随机数的个数
     * @return 返回二维数组。
     * 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二维数组为随机数数组。
     */
    public native byte[][] CPK_Random(int len);

    /**
     * 对称加密算法
     *
     * @param alg_func：使用的加密算法
     * @param key:对称密钥
     * @param input:要加密的数据
     * @return 返回二维数组。
     * 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二维数组为密文数组。
     */
    public native byte[][] HF_EncryptData(int alg_func, byte[] key, byte[] input);

    /**
     * 对称解密算法
     *
     * @param alg_func：使用的解密算法
     * @param key:对称密钥
     * @param input:要解密的数据
     * @return 返回二维数组。
     * 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二维数组为解密后的明文数组。
     */
    public native byte[][] HF_DecryptData(int alg_func, byte[] key, byte[] input);

    /**
     * 哈希算法
     *
     * @param hash_id：使用的哈希算法
     * @param input:要做哈希的数据
     * @return 返回二维数组。
     * 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二维数组为哈希值。
     */
    public native byte[][] HF_HASH(int hash_id, byte[] input);

    /**
     * 初始化sm2曲线
     *
     * @return 0:成功，其他值：失败。
     */
    public native int CPK_Init();

    /**
     * 释放sm2所占资源
     */
    public native void CPK_End();

    /**
     * 导入公钥矩阵
     *
     * @param pubmatrix：公钥矩阵
     * @return 0：成功。其他值：失败。
     */
    public native int ImportPubMatrix(byte[] pubmatrix);

    /**
     * 导入公共钥匙
     *
     * @param pubmatrix 公共矩阵
     * @return 0：成功 。其他值：失败
     */
    public native int ImportPubKey(byte[] pubmatrix);

    /**
     * sm2非对称加密，使用接收方标识加密数据
     *
     * @param receiver：接收方标识
     * @param input:要加密的数据
     * @return 返回二维数组。
     * 第一维数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二维数组为输出的密文。
     */
    public native byte[][] CPK_SM2_Encrypt(byte[] receiver, byte[] input);

    /**
     * sm2验证签名
     *
     * @param sender：发送方标识
     * @param input:要签名前的原始数据
     * @param sign:签名数据
     * @return 0：成功。其他值：失败
     */
    public native int CPK_SM2_VerifySign(byte[] sender, byte[] input, byte[] sign);

    /**
     * 获得密文信息
     *
     * @param input:密文
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行数组为版本信息，由jni.GetIntValue()函数获得。
     * 第三行数组为矩阵标识。
     * 第四行为接收方标识。
     */
    public native byte[][] CPK_GetCipherInfo(byte[] input);

    /**
     * 获得签名信息
     *
     * @param signature:签名数据
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行数组为版本信息，由jni.GetIntValue()函数获得。
     * 第三行数组为矩阵标识。
     * 第四行为发送方标识。
     */
    public native byte[][] CPK_GetSignatureInfo(byte[] signature);

    //硬件key函数

    /**
     * 获得签名信息
     *
     * @param dev_Type:用户设备类型。1：U key。2：TF card。
     * @return 0：成功。其他值：失败。
     */
    public native int Dev_Open(int dev_Type, String path);

    public native int Dev_OpenSim(byte[] simA, byte[] simB);

    /**
     * 验证pin码，登录设备。
     *
     * @param userType:用户类型。 0：表示root user ， 1：表示 普通 user
     * @param pin:pin码。
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为验证pin码失败后剩余次数，由jni.GetIntValue()函数获得。
     */
    public native byte[][] Dev_VerifyPin(int userType, byte[] pin);

    /**
     * 获得设备ID。
     *
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为设备ID。
     */
    public native byte[][] Dev_GetID();

    /**
     * 设备产生随机数。
     *
     * @param len:随机数数组长度。
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为随机数数组。
     */
    public native byte[][] Dev_GetRandom(int len);

    /**
     * 修改设备pin码。
     *
     * @param userType:用户设备类型。1：U key。2：TF card。
     * @param oldPin:原pin码。
     * @param newPin:新pin码。
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为验证pin码后剩余尝试次数，由jni.GetIntValue()函数获得。
     */
    public native byte[][] Dev_ChangePin(int userType, byte[] oldPin, byte[] newPin);

    /**
     * 获得用户证书列表。
     *
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为用户证书个数，由jni.GetIntValue()函数获得。
     * 第三行为第一个用户证书，结构为 索引+空格+用户标识+空格+矩阵标识。
     * 第四行为第二个用户证书，结构为 索引+空格+用户标识+空格+矩阵标识，依次类推
     */
    public native byte[][] Dev_GetCertList();

    /**
     * 使用设备用户证书解密数据。
     *
     * @param input:要解密的密文。
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为解密后的明文。
     */
    public native byte[][] Dev_SM2_Decrypt(byte[] input);

    /**
     * 使用设备用户证书生成签名。
     *
     * @param cert_index:用户证书索引。
     * @param input:要解密的密文。
     * @return 返回二维数组。
     * 第一行数组为函数返回值，由jni.GetIntValue()函数获得,值为0，表示成功，其他值为错误码。
     * 第二行为生成的签名。
     */
    public native byte[][] Dev_SM2_Sign(int cert_index, byte[] input);

    /**
     * 关闭设备。
     */
    public native void Dev_Close();

    /**
     * 将数组里的数据转为整数值。
     *
     * @param num:要转为int值的数组。
     * @return -1：转换失败。其他值：转换后的int值。
     */
    public native int GetIntValue(byte[] num);

    /**
     * 一些设备需要设置Android层的 Context 对象
     * 例如  双头key、蓝牙
     *
     * @param context: Android系统的 Context
     */
    public native void SetAndroidContext(Object context);
}
