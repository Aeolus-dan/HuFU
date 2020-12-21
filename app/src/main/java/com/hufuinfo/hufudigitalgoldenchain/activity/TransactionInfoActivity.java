package com.hufuinfo.hufudigitalgoldenchain.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.fragment.AllRecordFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.GlodCoinAdvantageFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.GoldNetworkCenterFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.HelpExplainFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.NewsFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.PartnerEquityFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.PartnerStockFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.PersonalTranInfoFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.PlatformTransactionFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.ProjectBackgroundFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.RechargeRecordFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.SettingFragment;
import com.hufuinfo.hufudigitalgoldenchain.fragment.TreamDetailFragment;

public class TransactionInfoActivity extends BaseDispatchTouchActivity {

    private FragmentManager supportFragmentManager;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        String transactionType = mIntent.getStringExtra("TRANSACTIONTYPE");
        setContentView(R.layout.activity_transcation_info);
        supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (transactionType.equals("platform")) {
            fragmentTransaction.replace(R.id.container_transaction, PlatformTransactionFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("personal")) {
            fragmentTransaction.replace(R.id.container_transaction, PersonalTranInfoFragment.newInstance())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("recharge")) {
            fragmentTransaction.replace(R.id.container_transaction, new RechargeRecordFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("allRecord")) {
            fragmentTransaction.replace(R.id.container_transaction, new AllRecordFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("teamDetail")) {
            fragmentTransaction.replace(R.id.container_transaction, new TreamDetailFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("PROJECT_BACKGROUND")) {
            fragmentTransaction.replace(R.id.container_transaction, new ProjectBackgroundFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("partnerEquity")) {
            fragmentTransaction.replace(R.id.container_transaction, new PartnerEquityFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("partnerStock")) {
            fragmentTransaction.replace(R.id.container_transaction, new PartnerStockFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("glodCoinAdvantage")) {
            fragmentTransaction.replace(R.id.container_transaction, new GlodCoinAdvantageFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("helpExplain")) {
            fragmentTransaction.replace(R.id.container_transaction, new HelpExplainFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("goldNetworkCenter")) {
            fragmentTransaction.replace(R.id.container_transaction, new GoldNetworkCenterFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("Setting")) {
            fragmentTransaction.replace(R.id.container_transaction, new SettingFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("1")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("2")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("3")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("4")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("5")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("6")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("7")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (transactionType.equals("8")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("9")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("10")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("11")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }else if (transactionType.equals("12")) {
            fragmentTransaction.replace(R.id.container_transaction, NewsFragment.newInstance(Integer.parseInt(transactionType)))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }
}
