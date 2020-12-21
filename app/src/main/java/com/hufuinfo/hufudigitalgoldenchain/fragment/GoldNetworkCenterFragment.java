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
public class GoldNetworkCenterFragment extends Fragment {

    private WebView goldNetworkCenterWb;

    public GoldNetworkCenterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gold_network_center, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        goldNetworkCenterWb = view.findViewById(R.id.gold_network_center_wb);
        //https://www.hufuinfo.net/public/pcGoldObject/goldLink.html
        //file:///android_asset/gold_network_trading_center.htm
        goldNetworkCenterWb.loadUrl("file:///android_asset/gold_network_trading_center.htm");
        view.findViewById(R.id.gold_network_center_return_tv).setOnClickListener(
                listener -> getActivity().onBackPressed());
    }
}
