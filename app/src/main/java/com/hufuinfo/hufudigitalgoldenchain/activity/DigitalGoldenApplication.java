package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class DigitalGoldenApplication extends Application {

    private List<Activity> mListActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        mListActivity = new ArrayList<>();
    }

    public void addActivity(Activity activity) {
        mListActivity.add(activity);
    }

    public void finishActivity() {
        try {
            for (Activity activity : mListActivity) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
