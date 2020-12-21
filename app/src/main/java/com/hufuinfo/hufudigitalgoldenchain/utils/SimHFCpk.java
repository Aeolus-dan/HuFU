package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.hfcpk_jni;

import org.apache.xerces.impl.dv.util.Base64;

import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;


public class SimHFCpk {
    private static final String THIS_FILE = "SimHFCpk";
    private static volatile SimHFCpk singletonSimHFCPK;
    private hfcpk_jni mhfCpk;
    private Context mContext;
    public boolean isLoginCos;
    private SharedPreferences mUserSharedPre;

    private SimHFCpk(Context mContext) {
        mhfCpk = new hfcpk_jni();
        mhfCpk.SetAndroidContext(mContext);
        this.mContext = mContext.getApplicationContext();
        mUserSharedPre = mContext.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, MODE_PRIVATE);

        String simA = mUserSharedPre.getString(ConstantUtils.BLE_SIMA, null);
        String simB = mUserSharedPre.getString(ConstantUtils.BLE_SIMB, null);
        if ("".equals(simA) || "".equals(simB) || simA == null || simB == null) {
            Toast.makeText(mContext, "蓝牙卡初始化失败", Toast.LENGTH_LONG).show();
            return;
        }
        mhfCpk.Dev_OpenSim(simA.getBytes(), simB.getBytes());
        isLoginCos = false;
    }

    public static SimHFCpk getInstance(Context mContext) {
        if (singletonSimHFCPK == null) {
            synchronized (SimHFCpk.class) {
                if (singletonSimHFCPK == null) {
                    singletonSimHFCPK = new SimHFCpk(mContext);
                }
            }
        }
        return singletonSimHFCPK;
    }

    public String getSimID() {
        byte[][] simId = mhfCpk.Dev_GetCertList();
        if (mhfCpk.GetIntValue(simId[0]) != 0) {
            Log.e("SimHFCpk", "获取证书ID失败");
            return null;
        }
        if (mhfCpk.GetIntValue(simId[1]) <= 0) {
            Log.e("SimHFCpk", "该硬件没有证书");
            return null;
        }
        String result = new String(simId[2]);
        String[] resultArray = result.split(" ");

        return resultArray[1];
    }

    /**
     * sm2非对称解密
     *
     * @param inpuEncrypt 密文
     * @return sm2非对称解密后的 byte[]
     */
    public byte[] SM2Decrypt(byte[] inpuEncrypt) {
        int result = mhfCpk.CPK_Init();
        if (result != 0) {
            Log.e("SimHFCpk", "SM2曲线初始化失败，err=" + result);
        }
        byte[][] resultSM2D = mhfCpk.Dev_SM2_Decrypt(inpuEncrypt);
        result = mhfCpk.GetIntValue(resultSM2D[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2解密失败，err=" + result);
            return null;
        }
        return resultSM2D[1];
    }

    public void closeHfCpk() {
        mhfCpk.Dev_Close();
        isLoginCos = false;
        singletonSimHFCPK = null;
    }

    /**
     * 对称加密
     *
     * @param key       对称秘钥
     * @param plainText 要加密的数据
     * @return 加密后的数据，返回为Base64字符串
     */
    public String EncryptData(byte[] key, String plainText) {
        if (!isLoginCos) {
            Log.e("SimHFCpk", "未登陆！");
            return null;
        }
        byte[][] resultByte = mhfCpk.HF_EncryptData(hfcpk_jni.ALG_AES_ECB_256, key, plainText.getBytes());
        int result = mhfCpk.GetIntValue(resultByte[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "对称加密失败，err=" + result);
            return null;
        }
        String cipherText = Base64.encode(resultByte[1]);
        return cipherText;
    }

    /**
     * SM2 非对称加密
     *
     * @param reciver   对方标识
     * @param plainText 要加密的数据
     * @return 加密后的数据，返回为Base64字符串
     */
    public String SM2EncyptData(byte[] reciver, String plainText) {
        if (!isLoginCos) {
            Toast.makeText(mContext, "未登录", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (ImportPubKey() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        int result = mhfCpk.CPK_Init();
        if (result != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        if (ImportMatrix() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        byte[][] resultSecrt = mhfCpk.CPK_SM2_Encrypt(reciver, plainText.getBytes());
        result = mhfCpk.GetIntValue(resultSecrt[0]);
        if (result != 0) {
            Log.e(THIS_FILE, "对称加密失败！");
            return null;
        }
        mhfCpk.CPK_End();
        return Base64.encode(resultSecrt[1]);
    }


    /**
     * @param password 登录的pin码
     * @return 数组的大小为2， 0的位置为登录结果返回码，1的位置为pin剩余次数
     */
    public int[] loginSimCos(String password) {
        byte[][] devVerify = mhfCpk.Dev_VerifyPin(1, password.getBytes());
        int result = mhfCpk.GetIntValue(devVerify[0]);
        if (result == 0) {
            Log.e("SimHFCpk", "登录成功");
            isLoginCos = true;
        } else {
            Log.e("SimHFCpk", "登录失败，err=" + result + "剩余pin次数=" + mhfCpk.GetIntValue(devVerify[1]));
        }
        int[] loginResult = {result, mhfCpk.GetIntValue(devVerify[1])};
        return loginResult;
    }

    /**
     * sm 签名
     *
     * @param sigInput 签名原始数据
     * @return 签名后的base64数据
     */
    public String SM2Sign(String sigInput) {
        if (ImportPubKey() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        int result = mhfCpk.CPK_Init();
        if (result != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        if (ImportMatrix() != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败!");
        }
        byte[][] resultSign = mhfCpk.Dev_SM2_Sign(1, sigInput.getBytes());
        result = mhfCpk.GetIntValue(resultSign[0]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2Sign签名失败，err=" + result);
            return null;
        }
        String keyId = getSimID();
        byte sign[] = new byte[256];

        byte[][] signInfoRet = mhfCpk.CPK_GetSignatureInfo(resultSign[1]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2曲线初始化失败，err=" + result);
        }
        result = mhfCpk.CPK_SM2_VerifySign(signInfoRet[3], sigInput.getBytes(), resultSign[1]);
        if (result != 0) {
            Log.e("SimHFCpk", "SM2Sign签名失败，err=" + result);
        }
        mhfCpk.CPK_End();
        return Base64.encode(resultSign[1]);
    }

    public int[] validateUserPayPassword(String password, String simA, String simB) {
        if (isLoginCos) {
            closeHfCpk();
        }
        mhfCpk.Dev_OpenSim(simA.getBytes(), simB.getBytes());


        int[] result = loginSimCos(password);
        return result;

    }

    private int ImportMatrix() {
        byte[] pubMatrix = readPub();
        int result = mhfCpk.ImportPubMatrix(pubMatrix);
        if (result != 0) {
            Log.e(THIS_FILE, "导入公钥矩阵失败，err=" + result);
        }
        return result;
    }

    private int ImportPubKey() {
        byte[] pubMatrix = readPub();
        int result = mhfCpk.ImportPubKey(pubMatrix);
        if (result != 0) {
            Log.e(THIS_FILE, "导入公钥矩Key，err=" + result);
        }
        return result;
    }

    @Nullable
    private byte[] readPub() {
        AssetManager assetManager = mContext.getAssets();
        try {
            InputStream inputCertStream = assetManager.open("hf05.com.pkm");
            int hfLen = inputCertStream.available();
            byte[] certBuffer = new byte[hfLen];
            int byteReadCert;
            while ((byteReadCert = inputCertStream.read(certBuffer)) != -1) {

            }
            inputCertStream.close();
            return certBuffer;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

  /*  static {
        System.loadLibrary("hfcpkjni");
        System.loadLibrary("hfcos");
        System.loadLibrary("crypto");
    }*/

}
