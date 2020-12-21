package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.os.Bundle;

import com.hufuinfo.hufudigitalgoldenchain.R;

public class AboutActivity extends BaseDispatchTouchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.about_return_tv)
                .setOnClickListener(
                        listener -> onBackPressed());
    }
}
