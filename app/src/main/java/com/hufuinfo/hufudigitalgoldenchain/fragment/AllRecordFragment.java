package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;


public class AllRecordFragment extends Fragment {
    private final static String THIS_FILE = "AllRecordFragment";
    private Activity mActivity;
    private SharedPreferences mUserSharedPre;
    private String antiFake;
    private String userMobile;
    private VirtualCpk mVirtualCpk;

    private ViewPager mAllRecordVp;
    private TabLayout mAllRecordTl;


    public AllRecordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mUserSharedPre = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        try {
            userMobile = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mVirtualCpk = VirtualCpk.getInstance(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_record, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mAllRecordTl = view.findViewById(R.id.all_record_tabl);
        mAllRecordVp = view.findViewById(R.id.all_record_container);
        mAllRecordVp.setAdapter(new AllRecordPageAdapter(getChildFragmentManager()));
        mAllRecordTl.setupWithViewPager(mAllRecordVp);
        mAllRecordTl.getTabAt(0).setText("全部账单");
        mAllRecordTl.getTabAt(1).setText("金本币账单");
        mAllRecordTl.getTabAt(2).setText("积分账单");
        mAllRecordTl.getTabAt(3).setText("信用账单");
        view.findViewById(R.id.all_record_return_tv).setOnClickListener(listener -> {
            mActivity.onBackPressed();
        });
    }

    class AllRecordPageAdapter extends FragmentPagerAdapter {
        public AllRecordPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return TotalBillInfoFragment.newInstance(0);
            } else if (i == 1) {
                return TotalBillInfoFragment.newInstance(1);
            } else if (i == 2) {
                return TotalBillInfoFragment.newInstance(2);
            } else if (i == 3) {
                return TotalBillInfoFragment.newInstance(3);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
