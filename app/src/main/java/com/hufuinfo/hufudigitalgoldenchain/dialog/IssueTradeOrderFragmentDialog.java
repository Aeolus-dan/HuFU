package com.hufuinfo.hufudigitalgoldenchain.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.hufuinfo.hufudigitalgoldenchain.R;

public class IssueTradeOrderFragmentDialog extends DialogFragment {

    private IssueTradeFragment issueTradeFragment;

    private ViewPager issueTradeVp;
    private TabLayout issueTabLayout;

    private IssueTradeFragment.CreateIssueOrder mCreateIssueOrder;
    private int transactionType;

    public static IssueTradeOrderFragmentDialog getInstance(IssueTradeFragment.CreateIssueOrder mCreateIssueOrder, int transactionType) {
        IssueTradeOrderFragmentDialog issueTradeOrderFragmentDialog = new IssueTradeOrderFragmentDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("transactionType", transactionType);
        issueTradeOrderFragmentDialog.mCreateIssueOrder = mCreateIssueOrder;
        issueTradeOrderFragmentDialog.setArguments(bundle);
        return issueTradeOrderFragmentDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionType = getArguments().getInt("transactionType");
        setStyle(DialogFragment.STYLE_NORMAL, R.style.IssueTradeFragmentDialog);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final Window window = getDialog().getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0及其以上
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4及其以上
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        View view = inflater.inflate(R.layout.issue_trade_order_fragment_dialog, (window.findViewById(android.R.id.content)), false);//需要用android.R.id.content这个view
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));//注意此处
        window.setLayout(-1, -2);//这2行,和上面的一样,注意顺序就行;
        issueTradeVp = view.findViewById(R.id.issue_trade_page_view);
        issueTabLayout = view.findViewById(R.id.issue_tab_layout);
        issueTradeVp.setAdapter(new IssueTradePageAdapter(getChildFragmentManager()));
        issueTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                issueTradeVp.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        issueTradeVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                issueTabLayout.setScrollPosition(i, v, true);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        view.findViewById(R.id.issue_trade_return_tv).setOnClickListener(listener -> {
            dismiss();
        });
        return view;
    }

    class IssueTradePageAdapter extends FragmentPagerAdapter {
        public IssueTradePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return IssueTradeFragment.getInstance(mCreateIssueOrder, transactionType, 0);
            } else if (i == 1) {
                return IssueTradeFragment.getInstance(mCreateIssueOrder, transactionType, 1);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
