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
public class HelpExplainFragment extends Fragment {

    private WebView webView;

    public HelpExplainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_explain, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        webView = view.findViewById(R.id.help_explain_wb);
        webView.loadUrl("file:///android_asset/help_explain.htm");
        view.findViewById(R.id.help_explain_return_tv).setOnClickListener(
                listener -> getActivity().onBackPressed());
    }

}
