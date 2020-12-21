package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.hufuinfo.hufudigitalgoldenchain.utils.LoginCountTimer;

public class BaseDispatchTouchActivity extends AppCompatActivity {
    private LoginCountTimer loginCountTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigitalGoldenApplication) getApplication()).addActivity(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loginCountTimer = LoginCountTimer.getSingleInstance(this);
        if (!loginCountTimer.isStart()) {
            loginCountTimer.startTimer();
        } else {
            loginCountTimer.cancelTimer();
            loginCountTimer.startTimer();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                loginCountTimer.startTimer();
                break;
            default:
                loginCountTimer.cancelTimer();
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}
