package com.hufuinfo.hufudigitalgoldenchain.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hufu.nzble.Constant;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.PaymentActivity;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.ValidatorUtils;

import java.math.BigDecimal;
import java.util.Date;

public class RechargeFragmentDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {

    private EditText buyerET;
    private RadioGroup buyerRadioGroup;
    private RadioGroup buyerRadioGroup2;

    public static RechargeFragmentDialog getInstance() {
        return new RechargeFragmentDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FragmentDialog);
      /*  if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog);*/
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_buyer, container, false);
        buyerRadioGroup = view.findViewById(R.id.buyer_rg);
        buyerRadioGroup2 = view.findViewById(R.id.buyer_rg2);
        buyerET = view.findViewById(R.id.buyer_num_et);
        ((RadioButton) view.findViewById(R.id.one_rb)).setOnCheckedChangeListener(this);
        ((RadioButton) view.findViewById(R.id.five_rb)).setOnCheckedChangeListener(this);
        ((RadioButton) view.findViewById(R.id.ten_rb)).setOnCheckedChangeListener(this);
        ((RadioButton) view.findViewById(R.id.tw_rb)).setOnCheckedChangeListener(this);
        ((RadioButton) view.findViewById(R.id.fth_rb)).setOnCheckedChangeListener(this);
        ((RadioButton) view.findViewById(R.id.one_hundred_rb)).setOnCheckedChangeListener(this);

        TextView goldPriceNumberTv = view.findViewById(R.id.buyer_num_price_tv);
        buyerET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String goldPrice = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                        .getString(ConstantUtils.GOLD_PRICE, null);
                if (goldPrice == null) return;

                if (s.toString().isEmpty()) {
                    goldPriceNumberTv.setText("");
                    return;
                }
                BigDecimal goldPriceBig = new BigDecimal(goldPrice);
                BigDecimal goldNum = new BigDecimal(s.toString());
                float totalPrice = goldPriceBig.multiply(goldNum).floatValue();
                goldPriceNumberTv.setText(goldNum + "金本币*" + goldPrice + "=" + totalPrice + "元");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        view.findViewById(R.id.dialog_close_btn).setOnClickListener(listener -> {
            dismiss();
        });
        view.findViewById(R.id.dialog_buyer_btn).setOnClickListener(listener -> {
            String goldNumber = buyerET.getText().toString();
            if (!ValidatorUtils.checkDecimals(goldNumber)) {
                Toast.makeText(getActivity(), "请输入正确的金本币数", Toast.LENGTH_SHORT).show();
                return;
            }

            String goldPrice = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE)
                    .getString(ConstantUtils.GOLD_PRICE, null);
            if (goldPrice == null) return;
            BigDecimal goldPriceBig = new BigDecimal(goldPrice);
            BigDecimal goldNum = new BigDecimal(goldNumber);
            String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNum).floatValue());
            Intent payIntent = new Intent(getActivity(), PaymentActivity.class);
            Bundle paymentBundle = new Bundle();
            paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNum.doubleValue()));
            paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
            paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
            paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 1);
            payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
            startActivity(payIntent);
            dismiss();
        });
        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buyerRadioGroup2.clearCheck();
        buyerRadioGroup.clearCheck();
        String goldNum = buttonView.getText().toString().replace("个", "");
        buyerET.setText(goldNum);
    }
}
