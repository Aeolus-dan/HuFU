package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.hufuinfo.hufudigitalgoldenchain.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GlodCoinAdvantageFragment extends Fragment {


    public GlodCoinAdvantageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_glod_coin_advantage, container, false);
        WebView webView = (WebView)view.findViewById(R.id.news_web);
        webView.loadUrl("file:///android_asset/121622064278.htm");
        return view;
    }

}
