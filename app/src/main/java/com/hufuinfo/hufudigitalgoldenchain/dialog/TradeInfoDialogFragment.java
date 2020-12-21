package com.hufuinfo.hufudigitalgoldenchain.dialog;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryTransactionOrder;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.RevokeOrderQuest;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.utils.AnimationUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import java.math.BigDecimal;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * A simple {@link Fragment} subclass.
 */
public class TradeInfoDialogFragment extends DialogFragment {

    private static final String TRANSACTION_TYPE = "transaction_type";
    private static final String FIRM_ID = "firm_id";
    private static final String TRANSACTION_STATUS = "transaction_status";


    private TextView recipientTv;
    private TextView goldNumTv;
    private TextView goldPriceTvx;

    private TextView orderPriceTv;
    private TextView orderNumTv;
    private TextView orderCreateTimeTv;

    private TextView tranNoteTV;
    private TextView orderCreateTv;
    private TextView tranModeTv;

    private Button transactionBtn;
    private Button transactionRevokeBtn;
    private Button againInfoBtn;
    /**
     * 交易类型
     * 1 挂盘交易
     * 0 点对点交易
     */
    private int transactionType = 1;

    /**
     * 完成状态
     * 0 未完成
     * 1 完成
     */
    private int transactionStatus = 0;

    private Activity mActivity;
    private String mUserPhoneNumber;
    private String antiFake;
    private VirtualCpk mVirtualCpk;
    private String firmId;
    private SharedPreferences mUserSharedPre;
    private TradeClick mTradeClick;
    private TransactionOrderInfo transactionOrderInfo;

    public TradeInfoDialogFragment() {
    }

    public static TradeInfoDialogFragment newInstance(int transactionType, int transactionStatus, String firmId) {
        TradeInfoDialogFragment tradeInfoDialogFragment = new TradeInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TRANSACTION_TYPE, transactionType);
        bundle.putString(FIRM_ID, firmId);
        bundle.putInt(TRANSACTION_STATUS, transactionStatus);
        tradeInfoDialogFragment.setArguments(bundle);
        return tradeInfoDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            transactionType = bundle.getInt(TRANSACTION_TYPE);
            firmId = bundle.getString(FIRM_ID);
            transactionStatus = bundle.getInt(TRANSACTION_STATUS);
        }
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
        mUserSharedPre = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_trade_info_dialog, container, false);
        initView(view);
        AnimationUtils.slideToUp(view);
        queryOrderInfo(firmId);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void initView(View view) {
        goldNumTv = view.findViewById(R.id.gold_number_tv);  //订单金本币数
        goldPriceTvx = view.findViewById(R.id.inter_gold_price_tv); //金本币单价
        orderPriceTv = view.findViewById(R.id.order_price_tv);  //交易金额

        orderCreateTv = view.findViewById(R.id.order_create_tv);//订单发起人
        recipientTv = view.findViewById(R.id.dialog_recipient_tv);  //订单接受人
        orderNumTv = view.findViewById(R.id.order_num_tv);  //订单号

        tranNoteTV = view.findViewById(R.id.tran_note_tv); //交易备注
        orderCreateTimeTv = view.findViewById(R.id.order_create_time_tv); //订单创创建时间
        tranModeTv = view.findViewById(R.id.tran_mode_tv); //交易类型

        transactionBtn = view.findViewById(R.id.trade_tran_btn);
        transactionBtn.setOnClickListener(listener -> {
            mTradeClick.onConfirmOrderClick(transactionOrderInfo, tranMode);
            dismiss();
        });
        transactionRevokeBtn = view.findViewById(R.id.revoke_tran_btn);
        transactionRevokeBtn.setOnClickListener(listener -> {
            RevokeOrderQuest revokeOrderQuest = createRevokeOrderInfo(transactionOrderInfo, tranMode);
            mTradeClick.onRevokOrderClick(revokeOrderQuest);
            dismiss();
        });
        againInfoBtn = view.findViewById(R.id.again_info_btn);
        againInfoBtn.setOnClickListener(listener -> {
            queryOrderInfo(firmId);
        });
    }

    private int tranMode = -1;   //交易类型  0 买入 1 卖出  以订单信息作为参照物

    public void showTradeInfoData(TransactionOrderInfo transactionInfo) {
        goldNumTv.setText("" + transactionInfo.transactionAmount);

        goldPriceTvx.setText("" + transactionInfo.customPrice);

        BigDecimal tranAmountBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.transactionAmount));
        BigDecimal goldPriceBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.customPrice));
        orderPriceTv.setText("" + tranAmountBig.multiply(goldPriceBig).floatValue());
        orderNumTv.setText("" + transactionInfo.firmId);

        orderCreateTimeTv.setText("" + transactionInfo.createTime);

        tranNoteTV.setText("" + transactionInfo.noteInformation);

        if (transactionInfo.initiatorId.equals(transactionInfo.buyerId)) {
            orderCreateTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.buyerId));
            tranModeTv.setText("买入");
            tranMode = 0;
        } else {
            orderCreateTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.sellerId));
            tranModeTv.setText("卖出");
            tranMode = 1;
        }
        if (transactionType == 1) {
            recipientTv.setText("HUFU金融交易品台");
        } else {
            recipientTv.setVisibility(View.VISIBLE);
            if (transactionInfo.initiatorId.equals(transactionInfo.sellerId)) {
                orderCreateTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.initiatorId));
                recipientTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.buyerId));
            } else {
                recipientTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.sellerId));
                orderCreateTv.setText("" + DateUtils.getStringHidePhone(transactionInfo.initiatorId));
            }
        }

        if (mUserPhoneNumber.equals(transactionInfo.initiatorId)) {
            if (transactionType == 1) {
                transactionBtn.setVisibility(View.GONE);
                transactionRevokeBtn.setVisibility(View.VISIBLE);
            } else {
                transactionBtn.setVisibility(View.GONE);
                transactionRevokeBtn.setVisibility(View.VISIBLE);
            }
        } else {
            if (transactionType == 1) {
                transactionBtn.setVisibility(View.VISIBLE);
                transactionRevokeBtn.setVisibility(View.GONE);
            } else {
                transactionBtn.setVisibility(View.VISIBLE);
                transactionRevokeBtn.setVisibility(View.VISIBLE);
            }
        }

        //完成状态设置交易按钮是否显示
        if (transactionStatus == 1) {
            transactionBtn.setVisibility(View.GONE);
            transactionRevokeBtn.setVisibility(View.GONE);
            if (mUserPhoneNumber.equals(transactionInfo.buyerId)) {
                tranModeTv.setText("买入");
            } else {
                tranModeTv.setText("卖出");
            }
        }
    }


    private void queryOrderInfo(String firmId) {
        String keyId = CombinationSecretKey.getSecretKey("queryFirm.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), CombinationSecretKey.assembleJsonFirmId(firmId));
        HuFuCode huFuCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(huFuCode));

        QueryTransactionOrder queryTransactionOrder = RetrofitUtils.create(QueryTransactionOrder.class);
        queryTransactionOrder.queryOrderInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryAllCertsResult -> {
                    //boolean issuccess = queryAllCertsResult.get("success").getAsBoolean();
                    if (queryAllCertsResult.success) {
                        transactionOrderInfo = queryAllCertsResult.data.firm;
                        showTradeInfoData(transactionOrderInfo);
                        againInfoBtn.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                        againInfoBtn.setVisibility(View.VISIBLE);
                    }
                }, error -> {
                    againInfoBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), R.string.net_error, Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 撤销订单 第二次请求的信息
     *
     * @param transactionInfo
     */
    private RevokeOrderQuest createRevokeOrderInfo(TransactionOrderInfo transactionInfo, int tranMode) {
        RevokeOrderQuest mRevokeOrderQuest = new RevokeOrderQuest();
        mRevokeOrderQuest.setFirmId(transactionInfo.firmId);
        mRevokeOrderQuest.setMobile(mUserPhoneNumber);
        mRevokeOrderQuest.setStatus(tranMode);
        mRevokeOrderQuest.setTransactionAmount(transactionInfo.transactionAmount);
        return mRevokeOrderQuest;
    }

    public void setmTradeClick(TradeClick tradeClick) {
        mTradeClick = tradeClick;
    }

    public interface TradeClick {
        void onConfirmOrderClick(TransactionOrderInfo transactionOrderInfo, int tranMode);

        void onRevokOrderClick(RevokeOrderQuest revokeOrderQuest);
    }
}
