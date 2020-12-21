package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cpk.jni.CpkAPI;
import com.cpk.jni.KeyAPI;

import org.apache.xerces.impl.dv.util.Base64;

import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

public class VirtualCpk {
    private static final String THIS_FILE = "VirtualCpk";
    private static volatile VirtualCpk singletonVirtualCpk;
    private KeyAPI mKeyApi;
    private Context mContext;
    public boolean isLoginCos;
    private SharedPreferences mUserSharedPre;
    private String mPath;

    private VirtualCpk(Context mContext) {
        mKeyApi = new KeyAPI();
        this.mContext = mContext.getApplicationContext();
        mUserSharedPre = mContext.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);
        mPath = mUserSharedPre.getString(ConstantUtils.VIRTUAL_PATH, "null") + "/";
        mKeyApi.Dev_OpenPath(mPath.getBytes());
        isLoginCos = false;
    }

    public static VirtualCpk getInstance(Context mContext) {
        if (singletonVirtualCpk == null) {
            synchronized (VirtualCpk.class) {
                if (singletonVirtualCpk == null) {
                    singletonVirtualCpk = new VirtualCpk(mContext);
                }
            }
        }
        return singletonVirtualCpk;
    }

    /**
     * 获取硬件ID
     *
     * @return keyID 证书的ID号  null  不存在用户ID
     */
    public String getSimID() {
        byte[][] simId = mKeyApi.Dev_GetCertList();
        int resultInt = mKeyApi.GetIntValue(simId[0]);
        if (resultInt != 0) {
            Log.e("SimHFCpk", "获取证书ID失败" + resultInt);
            return null;
        }
        if (mKeyApi.GetIntValue(simId[1]) <= 0) {
            Log.e("SimHFCpk", "该硬件没有证书");
            return null;
        }
        String result = new String(simId[2]);
        String[] resultArray = result.split(" ");

        return resultArray[1];
    }

    private int getCertificateIndex() {
        byte[][] simId = mKeyApi.Dev_GetCertList();
        int resultInt = mKeyApi.GetIntValue(simId[0]);
        if (resultInt != 0) {
            Log.e("SimHFCpk", "获取证书ID失败" + resultInt);
            return -1;
        }
        if (mKeyApi.GetIntValue(simId[1]) <= 0) {
            Log.e("SimHFCpk", "该硬件没有证书");
            return -1;
        }
        String result = new String(simId[2]);
        String[] resultArray = result.split(" ");
        String indexResult = resultArray[0];
        if (indexResult != null) {
            return Integer.parseInt(indexResult);
        }
        return -1;
    }

    /**
     * sm2非对称解密
     *
     * @param inpuEncrypt 密文
     * @return sm2非对称解密后的 byte[]
     */
    public byte[] SM2Decrypt(byte[] inpuEncrypt) {
        byte[][] resultSM2D = mKeyApi.Dev_SM2_Decrypt(inpuEncrypt);
        int result = mKeyApi.GetIntValue(resultSM2D[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2解密失败，err=" + result);
            return null;
        }
        return resultSM2D[1];
    }


    /**
     * 对称加密
     *
     * @param key       对称秘钥
     * @param plainText 要加密的数据
     * @return 加密后的数据，返回为Base64字符串
     */
    public String EncryptData(byte[] key, String plainText) {
        CpkAPI mCpkApi = new CpkAPI();
        byte[][] resultByte = mCpkApi.EncryptData(CpkAPI.ALG_AES_ECB_256, key, plainText.getBytes());
        int result = mCpkApi.GetIntValue(resultByte[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "对称加密失败，err=" + result);
            return null;
        }
        return Base64.encode(resultByte[1]);
    }

    /**
     * 对称解密
     *
     * @param key        对称密钥
     * @param cipherText Base64字符串 的加密数据
     * @return 解密后的字符串数据， null 标识解密失败
     */
    public String DecryptData(byte[] key, String cipherText) {
        byte[] cipherTextB = Base64.decode(cipherText);
        CpkAPI mCpkApi = new CpkAPI();
        byte[][] resultByte = mCpkApi.DecryptData(CpkAPI.ALG_AES_ECB_256, key, cipherTextB);
        int result = mCpkApi.GetIntValue(resultByte[0]);
        if (result != 0) {
            Log.e(THIS_FILE, "对称解密失败， err=" + result);
            return null;
        }
        return new String(resultByte[1]);
    }

    /**
     * SM2 非对称加密
     *
     * @param reciver   对方标识
     * @param plainText 要加密的数据
     * @return 加密后的数据，返回为Base64字符串
     */
    public String SM2EncyptData(byte[] reciver, String plainText) {
        if (ImportMatrix() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        byte[][] resultSecrt = mKeyApi.Dev_SM2_Encrypt(reciver, plainText.getBytes());
        int result = mKeyApi.GetIntValue(resultSecrt[0]);
        if (result != 0) {
            Log.e(THIS_FILE, "对称加密失败！");
            return null;
        }
        return Base64.encode(resultSecrt[1]);
    }

    /**
     * SM4 解密函数
     *
     * @param keyId     解密key
     * @param plainText 加密密文
     * @return 解密byte数组
     */
    public byte[] SM4Decrypt(String keyId, byte[] plainText) {
        byte[][] resultSecrt = mKeyApi.Dev_Decrypt(KeyAPI.KEY_SM4_ECB, keyId.getBytes(), plainText);
        int result = mKeyApi.GetIntValue(resultSecrt[0]);
        if (result != 0) {
            Log.e(THIS_FILE, " SM4 解密失败！");
            return null;
        }
        return resultSecrt[1];
    }

    /**
     * sm3 验签
     *
     * @param keyId     验签keyId
     * @param plainText 验签数据
     * @return 验签结果
     */
    public byte[] SM3Decrypt(String keyId, String plainText) {
        byte[][] resultSecrt = mKeyApi.Dev_Decrypt(KeyAPI.KEY_SM3, keyId.getBytes(), plainText.getBytes());
        int result = mKeyApi.GetIntValue(resultSecrt[0]);
        if (result != 0) {
            Log.e(THIS_FILE, " SM3 验签失败！");
            return null;
        }
        return resultSecrt[1];
    }

    /**
     * @param userType 用户类型
     * @param password 登录的pin码
     * @return 数组的大小为2， 0的位置为登录结果返回码，1的位置为pin剩余次数
     */
    public int[] loginSimCos(int userType, String password) {
        byte[][] devVerify = mKeyApi.Dev_VerifyPin(userType, password.getBytes());
        int result = mKeyApi.GetIntValue(devVerify[0]);
        if (result == 0 || result == 20486) {
            Log.e("SimHFCpk", "登录成功");
            result = 0;
            isLoginCos = true;
        } else {
            Log.e("SimHFCpk", "登录失败，err=" + result + "剩余pin次数=" + mKeyApi.GetIntValue(devVerify[1]));
        }
        int[] loginResult = {result, mKeyApi.GetIntValue(devVerify[1])};
        return loginResult;
    }

    /**
     * 关闭设备
     */
    public void closeVirtualCos() {
        mKeyApi.Dev_Close();
        isLoginCos = false;
        singletonVirtualCpk = null;

    }

    /**
     * sm2 签名
     *
     * @param sigInput 签名原始数据
     * @return 签名后的base64数据
     */
    public String SM2Sign(String sigInput) {
        if (ImportMatrix() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        int indexCert = getCertificateIndex();
        if (indexCert < 0) {
            Log.e(THIS_FILE, "查询证书索引失败");
            return null;
        }
        byte[][] resultSign = mKeyApi.Dev_SM2_Sign(indexCert, sigInput.getBytes());
        int result = mKeyApi.GetIntValue(resultSign[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2Sign签名失败，err=" + result);
            return null;
        }
        result = mKeyApi.Dev_SM2_VerifySign(sigInput.getBytes(), resultSign[1]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2Sign签名失败，err=" + result);
        }
        return Base64.encode(resultSign[1]);
    }

    /**
     * SM2 验签
     *
     * @param plainText  验签的原始数据
     * @param cipherText 验签的数据
     * @return true  验签成功， false 验签失败
     */
    public boolean SM2VerifySign(String plainText, String cipherText) {
        ImportMatrix();
        byte[] cipherArrayByte = Base64.decode(cipherText);
        int result = mKeyApi.Dev_SM2_VerifySign(plainText.getBytes(), cipherArrayByte);
        return result == 0;
    }

    /**
     * 导入用户证书
     *
     * @param userCert 用户证书byte数组
     * @return 0表示成功，其他失败
     */
    public int ImportUserCert(byte[] userCert) {
        return mKeyApi.Dev_ImportUserCert(userCert);
    }

    public int ClearUserCert() {
        return mKeyApi.Dev_ClearCert();
    }

    /**
     * 导入预制证书
     *
     * @param preCert 预制证书byte数组
     * @return 0表示成功，其他失败
     */
    public int ImportPreCert(byte[] preCert) {
        return mKeyApi.Dev_ImportPreCert(preCert);
    }

    private int ImportMatrix() {
        byte[] pubMatrix = readPub();
        int result = mKeyApi.Dev_ImportPubMatrix(pubMatrix);
        if (result != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败，err=" + result);
        }
        return result;
    }

    /**
     * 判断 是否有用户证书
     * 需要登录cos
     *
     * @return true  有用户证书， false 无用户证书
     */
    public boolean isUserCertifciation() {
        byte[][] certInfo = mKeyApi.Dev_GetCertList();
        int result = mKeyApi.GetIntValue(certInfo[0]);
        if (result != 0) {
            return false;
        }
        int certNum = mKeyApi.GetIntValue(certInfo[1]);
        if (certNum < 1)
            return false;
        Log.e(THIS_FILE, "证书信息：" + new String(certInfo[2]));
        return true;
    }

    /**
     * 修改 cos 的登录密码
     *
     * @param oldPwd 原始密码
     * @param newPwd 修改密码
     * @return 0 修改成功， 其他错误
     */
    public int reviseCosLoginPwd(String oldPwd, String newPwd) {
        byte[][] resultPinByte = mKeyApi.Dev_ChangePin(1, oldPwd.getBytes(), newPwd.getBytes());
        return mKeyApi.GetIntValue(resultPinByte[0]);
    }

    @Nullable
    private byte[] readPub() {
        AssetManager assetManager = mContext.getAssets();
        try {
            InputStream inputCertStream = assetManager.open("digitalcash.pkm");
            int hfLen = inputCertStream.available();
            byte[] certBuffer = new byte[hfLen];
            while (inputCertStream.read(certBuffer) != -1) {
            }
            inputCertStream.close();
            return certBuffer;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        System.loadLibrary("hfcpkJni");
        System.loadLibrary("hfcos");
        System.loadLibrary("crypto");
    }
}
