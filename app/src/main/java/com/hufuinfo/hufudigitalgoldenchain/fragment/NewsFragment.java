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
 * Use the {@link NewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsFragment extends Fragment {
    private static final String NEWS_NUM = "NEWS_NUM";

    private int newsNum;
    private WebView newsWebView;

    public NewsFragment() {
    }

    public static NewsFragment newInstance(int newsNum) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putInt(NEWS_NUM, newsNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            newsNum = getArguments().getInt(NEWS_NUM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        newsWebView = view.findViewById(R.id.news_web);
        String webpath = null;
        switch (newsNum) {
            case 1:
                webpath = "file:///android_asset/news1/121510131328.htm";
                break;
            case 2:
                webpath = "file:///android_asset/news2/" +
                        "121510425793.htm";
                break;
            case 3:
                webpath = "file:///android_asset/news3/" +
                        "121510472585.htm";
                break;
            case 4:
                webpath = "file:///android_asset/news4/" +
                        "121510481035.htm";
                break;
            case 5:
                webpath = "file:///android_asset/news5/" +
                        "121510485765.htm";
                break;
            case 6:
                webpath = "file:///android_asset/news6/" +
                        "122912231026.htm";
                break;
            case 7:
                webpath = "file:///android_asset/news7/" +
                        "121510501965.htm";
                break;
            case 8:
                webpath = "file:///android_asset/cpk_explain_html/" +
                        "cpk_explain.htm";
                break;
            case 9:
                webpath = "file:///android_asset/income_overview_html/" +
                        "122913312728.htm";
                break;
            case 10:
                webpath = "file:///android_asset/golden_angel_html/" +
                        "golden_angel.htm";
                break;
            case 11:
                webpath = "file:///android_asset/news11/" +
                        "121622325620.htm";
                break;
            case 12:
                webpath = "file:///android_asset/news13/" +
                        "121622363812.htm";
                break;

        }
        if (webpath != null)
            newsWebView.loadUrl(webpath);
    }
}
