package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.fragment.RegisterAuthorizationFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.RegisterUserFragment;

public class RegisterActivity extends BaseDispatchTouchActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private RegisterAuthorizationFragment mRegisterAuthorizationFragment;
    private RegisterUserFragment mRegisterUserFragment;
    private WebView mWBHome;
    private WebSettings mWBSetting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

       /* Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.setCurrentItem(1);


        mWBHome = findViewById(R.id.wb_home);
        mWBSetting = mWBHome.getSettings();
        mWBSetting.setJavaScriptEnabled(true);
        mWBSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        mWBSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWBSetting.setDomStorageEnabled(true);
        mWBSetting.setDatabaseEnabled(true);
        mWBSetting.setAppCacheEnabled(true);
        mWBSetting.setAllowFileAccess(true);
        mWBSetting.setSavePassword(true);
        mWBSetting.setSupportZoom(true);
        mWBSetting.setBuiltInZoomControls(true);
        mWBSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWBSetting.setUseWideViewPort(true);
        mWBSetting.setLoadWithOverviewMode(true);
        mWBSetting.setDisplayZoomControls(false);

       mWBHome.setWebChromeClient(new WebChromeClient());
        mWBHome.loadUrl("http://www.gold2040.com/register.html");

      mWBHome.setWebViewClient(new WebViewClient(){
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
              mWBHome.stopLoading();
              Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
              startActivity(loginIntent);
              finish();
              return true;
          }

          @Override
          public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
              if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                  mWBHome.goBack();
              }
              return true;
          }
      });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                mRegisterAuthorizationFragment = RegisterAuthorizationFragment.newInstance("12", "233");
                return mRegisterAuthorizationFragment;
            } else {
                mRegisterUserFragment = RegisterUserFragment.newInstance();
                return mRegisterUserFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
