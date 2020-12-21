package com.hufu.nzble;

import android.app.Application;
import android.content.Context;
import android.util.Log;


public class NzBleAndroidDriver {
    private static final String THIS_FILE = "NZBLEANDROIDDRIVER";

    private NzBleUtil mNzBleUtil;

    public NzBleAndroidDriver() {

    }

    static public NzBleAndroidDriver CreateNzBleAndroidDriver() {
        return new NzBleAndroidDriver();
    }

    public int openNzBleAndroidDriver(Context context, byte[] nzBleName, byte[] nzBlePin) {
        Log.e(THIS_FILE, "openNzBleAndroidDriver()");
        String name = new String(nzBleName);
        String pin = new String(nzBlePin);
        Application app = (Application) context.getApplicationContext();
        initNzBle(app, name, pin);

        return 0;
    }


    private void initNzBle(Application app, String name, String pin) {
        mNzBleUtil = NzBleUtil.getInstance(app);
        if (name == null || name.length() != 12 || pin == null || pin.length() != 6) {
            return;
        }
        toConnectNzBleSim(30, name, pin);
    }

    private void toConnectNzBleSim(int timeout, String name, String pin) {
        if (name == null || name.length() != 12 || pin == null
                || pin.length() != 6) {
            return;
        }
        if (mNzBleUtil != null) {
            mNzBleUtil.connect(name, pin, timeout);
        }
    }


    public int writeNzBleDriver(byte[] inputbyte, byte[] resultebyte, int[] resultlen) {
        byte[] nzbleResult = sendMessage(inputbyte);
        if (nzbleResult == null) {
            return -1;
        }
        System.arraycopy(nzbleResult, 0, resultebyte, 0, nzbleResult.length);
        resultlen[0] = nzbleResult.length;
        return 0;
    }

    private byte[] sendMessage(byte[] inputbyte) {
        byte[] rc = NzBleUtil.sendMsg(inputbyte);
        return rc;
    }


    public void nzBleDriverclose() {
        if (mNzBleUtil != null)
            mNzBleUtil.close();
    }
}