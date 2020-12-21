package com.hufuinfo.hufudigitalgoldenchain.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.bean.FirmBargain;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;

public class IssueTradeFragment extends Fragment {
    private Activity mActivity;

    private EditText goldPriceEt;
    private EditText goldNumEt;
    private EditText tranNoteEt;
    private EditText accountOtherEt;
    private TextView goldVerageTv;
    private TextView goldOrIntegralTv;

    private CreateIssueOrder mCreateIssueOrder;
    private FirmBargain mFirmBargin;
    private String mUserPhoneNumber;
    private String goldVeragePrice;
    private String goldOrIntegral;

    private int transactionType;
    private int transactionMode;

    public IssueTradeFragment() {
    }

    public static IssueTradeFragment getInstance(CreateIssueOrder mCreateIssueOrder, int transactionType, int transactionMode) {
        IssueTradeFragment issueTradeFragment = new IssueTradeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("transactionType", transactionType);
        bundle.putInt("transactionMode", transactionMode);
        issueTradeFragment.mCreateIssueOrder = mCreateIssueOrder;
        issueTradeFragment.setArguments(bundle);
        return issueTradeFragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mUserPhoneNumber = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                .getString(ConstantUtils.USER_ACCOUNT, null);
        goldVeragePrice = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                .getString(ConstantUtils.GOLD_AVERAGE_PRICE, null);

        Bundle bundle = getArguments();
        transactionMode = bundle.getInt("transactionMode");
        transactionType = bundle.getInt("transactionType");
        if (transactionMode == 0) {
            goldOrIntegral = "积分余额：    " + String.format("%.2f",Float.valueOf(mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                    .getString(ConstantUtils.USER_INTEGRAL_NUM, null))) + "积分";
        } else {
            goldOrIntegral = "金本币余额：    " +String.format("%.2f",Float.valueOf(mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                    .getString(ConstantUtils.USER_GOLD_NUM, null))) + " gG";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_issue_transaction, container, false);
        goldPriceEt = view.findViewById(R.id.price_num_et);
        goldNumEt = view.findViewById(R.id.buyer_num_et);
        tranNoteEt = view.findViewById(R.id.tran_note_et);
        accountOtherEt = view.findViewById(R.id.account_other_et);
        goldOrIntegralTv = view.findViewById(R.id.gold_integral_tv);
        goldOrIntegralTv.setText(goldOrIntegral);
        if (transactionType == 1) {
            //accountOtherEt.setHint("HUFU金融交易平台");
            accountOtherEt.setFocusable(false);
            accountOtherEt.setFocusableInTouchMode(false);
        } else {
            accountOtherEt.setHint("对方账号");
        }
        goldVerageTv = view.findViewById(R.id.office_price_tv);
        Float price = Float.valueOf(goldVeragePrice);
        goldVerageTv.setText("￥"+ String.format("%.2f",price)+ "/克");

        view.findViewById(R.id.dialog_order_confirm_btn).setOnClickListener(listener -> {
            String goldNum = goldNumEt.getText().toString();
            String priceGold = goldPriceEt.getText().toString();
            String appointMobile;
            if (!ValidatorUtils.checkDecimals(goldNum)) {
                Toast.makeText(mActivity, "请输入正确的金本币数", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!ValidatorUtils.checkDecimals(priceGold)) {
                Toast.makeText(mActivity, "请输入正确的金本币单价", Toast.LENGTH_SHORT).show();
                return;
            }
            String goldNumOffice = DateUtils.getStringDouble(Double.parseDouble(goldNum));
            boolean isPlatformOrder;
            if (transactionType == 1) {
                appointMobile = null;
                isPlatformOrder = true;
            } else {
                appointMobile = accountOtherEt.getText().toString();
                if (!ValidatorUtils.isMobile(appointMobile)) {
                    Toast.makeText(mActivity, "请输入正确的对方账号", Toast.LENGTH_LONG).show();
                    return;
                }
                isPlatformOrder = false;
            }

            String transNote = tranNoteEt.getText().toString();
            mFirmBargin = assembleFirmTranData(goldNumOffice, priceGold, transactionMode,
                    transNote, appointMobile, isPlatformOrder);
            mCreateIssueOrder.onCreateIssueOreder(mFirmBargin, transactionMode);

        });
        return view;
    }

    /**
     * 组合创建订单信息
     *
     * @param goldNum       金本币数
     * @param priceGold     金本币价格
     * @param transStatus   交易类型   1.作为卖方创建订单;0作为买方创建订单
     * @param transNote     交易备注  可以为null
     * @param appointMobile 点对点交易  对方账号   存在，不能为null
     * @param transMode     交易方式  平台交易 true  点对点交易false
     * @return 金本币订单信息
     */

    private FirmBargain assembleFirmTranData(String goldNum, String priceGold, int transStatus,
                                             String transNote, String appointMobile, boolean transMode) {
        FirmBargain mfirmBargain = new FirmBargain();
        mfirmBargain.setMobile(mUserPhoneNumber);
        mfirmBargain.setStatus(transStatus);
        mfirmBargain.setNoteInfomation(transNote);
        mfirmBargain.setTransactionAmount(Double.parseDouble(goldNum));
        if (transMode) {
            mfirmBargain.setTransactionType(1);
        } else {
            mfirmBargain.setTransactionType(0);
            mfirmBargain.setAppointMobile(appointMobile);
        }
        mfirmBargain.setPrice(Float.parseFloat(priceGold));
        return mfirmBargain;
    }

    public interface CreateIssueOrder {
        void onCreateIssueOreder(FirmBargain firmBargain, int transactionMode);
    }

}
