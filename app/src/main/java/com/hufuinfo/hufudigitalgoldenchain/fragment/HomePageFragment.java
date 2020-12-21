package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.TransactionInfoActivity;
import com.hufuinfo.hufudigitalgoldenchain.utils.GlideImageLoader;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;


public class HomePageFragment extends Fragment {

    private Integer[] images = {R.drawable.banner_2, R.drawable.banner_1,R.drawable.banner_3,
            R.drawable.banner_4,R.drawable.banner_5};
    private TextView projectBackgroundTv;
    private WebView mWBHome;
    private WebSettings mWBSetting;

    public HomePageFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_page, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
 /*       Banner banner = view.findViewById(R.id.banner);
        List<Integer> listImages = new ArrayList<>();
        listImages.add(images[0]);
        listImages.add(images[1]);
        listImages.add(images[2]);
        listImages.add(images[3]);
        listImages.add(images[4]);
        banner.setDelayTime(3000);
        banner.setImageLoader(new GlideImageLoader());
        banner.setImages(listImages);
        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                //int banner= position +1;
                String banner = "1";
                switch (position){
                    case 0:
                        banner = "glodCoinAdvantage";
                        break;
                    case 1:
                        banner = "7";
                        break;
                    case 2:
                        banner = "6";
                        break;
                    case 3:
                        banner = "12";
                        break;
                    case 4:
                        banner = "11";
                        break;
                }

                Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
                personalIntent.putExtra("TRANSACTIONTYPE", banner);
                startActivity(personalIntent);
            }
        });
        banner.start();

        projectBackgroundTv = view.findViewById(R.id.project_background_tv);
        projectBackgroundTv.setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "PROJECT_BACKGROUND");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.partner_equity_tv).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "partnerEquity");
            startActivity(personalIntent);
        });

        view.findViewById(R.id.partner_stock_tv).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "partnerStock");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.gold_coin_advantage_tv).setOnClickListener(listener -> {
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "glodCoinAdvantage");
            startActivity(personalIntent);
        });

        view.findViewById(R.id.help_details).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "helpExplain");
            startActivity(personalIntent);
        });

        view.findViewById(R.id.network_trading_btn).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "goldNetworkCenter");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_1).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "1");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_2).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "2");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_3).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "3");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_4).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "4");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_5).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "5");
            startActivity(personalIntent);
        });

        view.findViewById(R.id.golden_angel).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "10");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.income_overview).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "9");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.cpk_explain).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "8");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_6).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "6");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_7).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "7");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_11).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "11");
            startActivity(personalIntent);
        });
        view.findViewById(R.id.news_12).setOnClickListener(listener->{
            Intent personalIntent = new Intent(getActivity(), TransactionInfoActivity.class);
            personalIntent.putExtra("TRANSACTIONTYPE", "12");
            startActivity(personalIntent);
        });*/


        mWBHome = view.findViewById(R.id.wb_home);
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
        mWBHome.loadUrl("http://47.94.231.98:8080/CMAServer/home/appHome.jsp");
        mWBHome.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWBHome.loadUrl(url);
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


}
