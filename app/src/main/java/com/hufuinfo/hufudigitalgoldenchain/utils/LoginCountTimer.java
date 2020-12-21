package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;

import com.hufuinfo.hufudigitalgoldenchain.activity.DigitalGoldenApplication;
import com.hufuinfo.hufudigitalgoldenchain.activity.LoginActivity;

import java.lang.ref.WeakReference;

public class LoginCountTimer extends CountDownTimer {
    private static volatile LoginCountTimer mSingleInstance;
    private WeakReference<Activity> mActivity;
    private boolean isStart = false;

    public static LoginCountTimer getSingleInstance(Activity activity) {
        if (mSingleInstance == null) {
            synchronized (LoginCountTimer.class) {
                if (mSingleInstance == null) ;
                mSingleInstance = new LoginCountTimer(activity);
            }
        }
        return mSingleInstance;
    }

    private LoginCountTimer(Activity activity) {
        super(1000*60 * 5, 1000);
        mActivity = new WeakReference<>(activity);
    }

    public boolean isStart() {
        return isStart;
    }

    public void startTimer() {
        start();
        isStart = true;
    }

    public void cancelTimer() {
        cancel();
        isStart = false;
    }

    public void destoryTimer() {
        cancelTimer();
        if (mActivity == null)
            mSingleInstance = null;
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }


    @Override
    public void onFinish() {
        if (mActivity != null) {
            Intent loginIntent = new Intent(mActivity.get(), LoginActivity.class);
            mActivity.get().startActivity(loginIntent);
            ((DigitalGoldenApplication) mActivity.get().getApplication()).finishActivity();
            destoryTimer();
        }
    }
}
