/**
 * Copyright (c) 2015-2016 Nationz
 * <p>
 * zhai.yuehui
 * 2016-6-23
 * 下午1:38:02
 * zhai.yuehui
 * 2016-6-23
 * 下午1:38:02
 * zhai.yuehui
 * 2016-6-23
 * 下午1:38:02
 */
/**zhai.yuehui
 * 2016-6-23
 * 下午1:38:02
 */
package com.hufu.nzble;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.nationz.sim.sdk.NationzSim;
import com.nationz.sim.sdk.NationzSimCallback;

import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author 作者 E-mail:zhai.yuehui@nationz.com.cn
 * @version 创建时间：2016-6-23 下午1:38:02
 * 类说明
 */
public class NzBleUtil implements NationzSimCallback {
    private static final String TAG = "NzBleUtil";

    private static NationzSim mNationzSim;

    private static NzBleUtil mNzBleUtil;

    private static Application mApp;

    private Observable<String> loginObservable = Observable.create(new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(ObservableEmitter<String> e) {
            e.onNext("12345678");
        }
    });

    private NzBleUtil(Application app) {
        mApp = app;
        mNationzSim = NationzSim.initialize(mApp, this);
        NationzSim.setSdkParams(true, true, 30 * 60);
        NationzSim.setConnectTimeout(30);
        if (mNationzSim == null) {
            Toast.makeText(mApp, "您的手机不支持低功耗蓝牙功能", Toast.LENGTH_SHORT).show();
        }
    }

    public static synchronized NzBleUtil getInstance(Application app) {
        if (mNzBleUtil == null) {
            mNzBleUtil = new NzBleUtil(app);
        }
        return mNzBleUtil;
    }

    public static int getNzBleState() {
        return NationzSim.getConnectionState();
    }

    public void setTimeClose(int timeSecond) {
        NationzSim.setSdkParams(true, true, timeSecond);
    }

    public void connect(String name, String pin, int timeout) {
        if (name == null || name.length() != 12 || pin == null
                || pin.length() != 6) {
            return;
        }

        if (mNationzSim != null) {

            SharedPreferences mSp = mApp.getSharedPreferences(Constant.SP_NAME,
                    Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(Constant.BLE_NAME_CONNECTING, name);
            editor.putString(Constant.BLE_PIN_CONNECTING, pin);
            editor.commit();

            NationzSim.setConnectTimeout(timeout);//onConnectionStateChange返回27的时间
            Toast.makeText(mApp, "正在连接蓝牙卡，请稍候", Toast.LENGTH_LONG).show();
            mNationzSim.connect(name, pin);
        }
    }

    public void close() {
        if (mNationzSim != null) {
            mNationzSim.close();
        }
    }

    public void closeBle() {
        if (mNationzSim != null) {
            mNationzSim.closeBle();
        }
    }

    @Override
    public void onConnectionStateChange(int state) {
        Log.e(TAG, "onConnectionStateChange:" + state);
        //返回27时，请根据情况操作（可以调用close()停止连接过程，不调用的话还是会继续连接的）
        if (state == 27) {
            close();
            ConnectFailEvent mConnectFailEvent = new ConnectFailEvent();
            EventBus.getDefault().post(mConnectFailEvent);
        } else if (state == 16) {
            //连接成功，则保存正确的连接参数
            Toast.makeText(mApp, "蓝牙连接成功！", Toast.LENGTH_LONG).show();
            SharedPreferences mSp = mApp.getSharedPreferences(Constant.SP_NAME,
                    Activity.MODE_PRIVATE);
            String name = mSp.getString(Constant.BLE_NAME_CONNECTING, "");
            String pin = mSp.getString(Constant.BLE_PIN_CONNECTING, "");

            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(Constant.BLE_NAME_SUC, name);
            editor.putString(Constant.BLE_PIN_SUC, pin);
            editor.commit();
            sendMsg(StringUtil.hex2byte("00A404000ED196300077110010000000020101"));
            //byte[] result=sendMsg(StringUtil.hex2byte("10010100187C222FB2927D828AF22F592134E8932480637C0DBBC381B100"));
            //String longse =StringUtil.byte2String(result);
            //String seId =longse.substring(0,8);
            //sendMsg(StringUtil.hex2byte("102000004B" +seId+"875017CB0FF2D1F8360886186803D7E07E40245757A609E7F3FF2F37DBD6493E40EDC3E53727A7CC6682ABCF4F45AF66309CEBF6737385FDD578C12DFA7B6ADD676768ED4A87E400"));

        }
        BleStateEvent mBleEvent = new BleStateEvent(state);
        EventBus.getDefault().post(mBleEvent);
    }

    @Override
    @Deprecated
    public void onMsgWrite(int state) {
    }

    @Override
    @Deprecated
    public void onMsgBack(byte[] msgBack) {
    }

    @Override
    public void onMsgBack(String pacName, String appName) {
        Log.e(TAG, pacName + " " + appName + " 已连接");
        Toast.makeText(mApp, pacName + " " + appName + "已连接", Toast.LENGTH_SHORT).show();
    }

    public static byte[] sendMsg(byte[] msg) {
        if (NationzSim.getConnectionState() == 16) {
            //msg = StringUtil.hex2byte("10010000148A334C40914CABCDB4BA06190A14953B851522BE00");
            Log.e(TAG, "send msg is " + StringUtil.byte2String(msg));
            byte[] result = mNationzSim.wirteSync(msg);
            Log.e(TAG, "callback msg is " + StringUtil.byte2String(result));
            return result;
        }
        Log.e(TAG, "蓝牙未连接");
        EventBus.getDefault().post(new ToastEvent("蓝牙卡未连接"));
        return null;
    }

}
