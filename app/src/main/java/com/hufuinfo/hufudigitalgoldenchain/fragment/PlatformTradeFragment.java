package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.MainActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.PaymentActivity;
import com.hufuinfo.hufudigitalgoldenchain.adapter.PlatformTradeFinishAdapter;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.GoldenOrder;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryTransactionOrder;
import com.hufuinfo.hufudigitalgoldenchain.bean.ConfirmOrderInfoRequest;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetails;
import com.hufuinfo.hufudigitalgoldenchain.bean.RevokeOrderQuest;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.dialog.TradeInfoDialogFragment;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;
import com.hufuinfo.hufudigitalgoldenchain.widget.SoftRadioButton;
import com.hufuinfo.hufudigitalgoldenchain.widget.SoftRadioGroup;
import com.hufuinfo.hufudigitalgoldenchain.widget.TradeItemDecoration;

import org.apache.xerces.impl.dv.util.Base64;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 个人中心 平台订单查询
 * 查询 指定用户的平台订单信息
 */
public class PlatformTradeFragment extends Fragment implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TRANSACTION_STATUS = "transaction_status";
    private Activity mActivity;
    private RecyclerView mHanginTradeRv;
    private SwipeRefreshLayout mHangingTradeSR;
    private SoftRadioGroup tadeSoftRadioGroup;
    private SoftRadioButton  timeSoftRadioBtn;
    private Spinner platformTradeSpinner;
    private String sortType = "createTime"; //排序类型
    private int sortInt = 0; //排序方式  1:降序 0:升序

    //每页的行数
    private int rows = 10;
    //当前页面
    private int pageCurrently = 1;
    private String mUserPhoneNumber;
    /**
     * 交易方式
     * 0 全部， 1， 卖出， 2，买入
     */
    private int transactionMode = 0;
    /**
     * 完成状态
     * 0 未完成
     * 1 完成
     */
    private int status = 0;   //未完成   不需要改变
    /**
     * 交易类型
     * 1 挂盘交易
     * 0 点对点交易
     */
    private int transactionType = 1;


    private VirtualCpk mVirtualCpk;

    private TransactionAdapter mTransactionAdatper;

    private PlatformTradeFinishAdapter platformTradeFinishAdapter;

    private List<TransactionOrderInfo> transactionInfos = new ArrayList<>();

    private TransactionOrderInfo mTransactionInfo;

    private SharedPreferences mUserSharedPre;
    private String antiFake;

    private TradeInfoDialogFragment tradeInfoDialogFragment;

    public PlatformTradeFragment() {
    }

    public static PlatformTradeFragment newInstance(int transactionStatus) {
        PlatformTradeFragment fragment = new PlatformTradeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TRANSACTION_STATUS, transactionStatus);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mUserSharedPre = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        Bundle bundle = getArguments();
        if (bundle != null) {
            status = bundle.getInt(TRANSACTION_STATUS);
        }
        mVirtualCpk = VirtualCpk.getInstance(getActivity());
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trade, container, false);
        mHanginTradeRv = view.findViewById(R.id.trade_rv);
        mHangingTradeSR = view.findViewById(R.id.trade_swipe_refresh);
        mHangingTradeSR.setOnRefreshListener(this::onRefresh);
        tadeSoftRadioGroup = view.findViewById(R.id.trade_soft_rg);
        timeSoftRadioBtn =view.findViewById(R.id.create_time_srb);
        if(status ==1) {
            timeSoftRadioBtn.setText("完成时间");
        }
        timeSoftRadioBtn .setChecked(true, false);
        tadeSoftRadioGroup.setOnCheckedChangeListener(new SoftRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SoftRadioGroup group, int checkedId, boolean orientation) {
                switch (checkedId) {
                    case R.id.goldNum_srb:
                        sortType = "transactionAmount";
                        break;
                    case R.id.gold_price_srb:
                        sortType = "customPrice";
                        break;
                    case R.id.create_time_srb:
                        if(status ==0) {
                            sortType = "createTime";
                        }else {
                            sortType ="transactionDate";
                        }
                        break;
                }

                if (orientation) {
                    sortInt = 1;
                } else {
                    sortInt = 0;
                }
                onRefresh();
            }
        });
        platformTradeSpinner = view.findViewById(R.id.spinner_transaction_type);
        platformTradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transactionMode = position;
                onRefresh();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHanginTradeRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (status == 0) {
            mTransactionAdatper = new TransactionAdapter(transactionInfos);
            mHanginTradeRv.setAdapter(mTransactionAdatper);
            mTransactionAdatper.setOnLoadMoreListener(this, mHanginTradeRv);
            mTransactionAdatper.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    TransactionOrderInfo transactionInfo = (TransactionOrderInfo) adapter.getData().get(position);
                    //  showTransactionOrderInfo(transactionInfo);
                    tradeInfoDialogFragment = TradeInfoDialogFragment.newInstance(1, status, transactionInfo.firmId);
                    tradeInfoDialogFragment.show(getFragmentManager(), "tradeInfo");
                    if (tradeInfoDialogFragment != null) {
                        tradeInfoDialogFragment.setmTradeClick(new TradeInfoDialogFragment.TradeClick() {
                            @Override
                            public void onRevokOrderClick(RevokeOrderQuest revokeOrderQuest) {
                                showPaymentPassword(revokeOrderQuest);
                            }

                            @Override
                            public void onConfirmOrderClick(TransactionOrderInfo transactionOrderInfo, int transMode) {
                                if (transMode == 0) {
                                    //以自己为参照物， 作为卖出方确认订单
                                    mTransactionInfo = transactionOrderInfo;
                                    showConfirmPaymentPwdDialog(mTransactionInfo);
                                } else {
                                    //以自己为参照物, 作为买方确认订单

                                    mTransactionInfo = transactionOrderInfo;

                                    BigDecimal goldPriceBig = new BigDecimal(mTransactionInfo.customPrice);
                                    BigDecimal goldNumBig = new BigDecimal(mTransactionInfo.transactionAmount);
                                    String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNumBig).floatValue());

                                    Intent payIntent = new Intent(mActivity, PaymentActivity.class);
                                    Bundle paymentBundle = new Bundle();
                                    paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNumBig.doubleValue()));
                                    paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
                                    paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 2);
                                    paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
                                    payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
                                    startActivityForResult(payIntent, ConstantUtils.REQUEST_CODE_CONFIRM_ORDER_PAYMENT);


                                }
                            }

                        });
                    }
                }
            });
        } else {
            platformTradeFinishAdapter = new PlatformTradeFinishAdapter(transactionInfos, mUserPhoneNumber);
            mHanginTradeRv.setAdapter(platformTradeFinishAdapter);
            platformTradeFinishAdapter.setOnLoadMoreListener(this, mHanginTradeRv);
            platformTradeFinishAdapter.setOnItemClickListener((adapter, view, position) -> {
                TransactionOrderInfo transactionInfo = (TransactionOrderInfo) adapter.getData().get(position);
                tradeInfoDialogFragment = TradeInfoDialogFragment.newInstance(0, status, transactionInfo.firmId);
                tradeInfoDialogFragment.show(getFragmentManager(), "tradeInfo");
                if (tradeInfoDialogFragment != null) {
                    tradeInfoDialogFragment.setmTradeClick(new TradeInfoDialogFragment.TradeClick() {
                        @Override
                        public void onRevokOrderClick(RevokeOrderQuest revokeOrderQuest) {
                            showPaymentPassword(revokeOrderQuest);
                        }

                        @Override
                        public void onConfirmOrderClick(TransactionOrderInfo transactionOrderInfo, int transMode) {
                            confirmtranMode = tranMode;
                            if (transMode == 0) {
                                //以自己为参照物， 作为卖出方确认订单
                                mTransactionInfo = transactionOrderInfo;
                                showConfirmPaymentPwdDialog(mTransactionInfo);
                            } else {
                                //以自己为参照物, 作为买方确认订单

                                mTransactionInfo = transactionOrderInfo;

                                BigDecimal goldPriceBig = new BigDecimal(mTransactionInfo.customPrice);
                                BigDecimal goldNumBig = new BigDecimal(mTransactionInfo.transactionAmount);
                                String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNumBig).floatValue());

                                Intent payIntent = new Intent(mActivity, PaymentActivity.class);
                                Bundle paymentBundle = new Bundle();
                                paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNumBig.doubleValue()));
                                paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
                                paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 2);
                                paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
                                payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
                                startActivityForResult(payIntent, ConstantUtils.REQUEST_CODE_CONFIRM_ORDER_PAYMENT);


                            }
                        }

                    });
                }
            });


        }
        mHanginTradeRv.addItemDecoration(new TradeItemDecoration(mActivity));
        mHanginTradeRv.setHasFixedSize(true);
        onLoadFirstData(mUserPhoneNumber);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 查询交易信息
     *
     * @param userId          用户账号  查询平台交易可以null
     * @param page            页面
     * @param rows            行数
     * @param status          交易状态  0：未完成 1：完成
     * @param transactionType 交易类型 1-挂盘交易,0-点对点交易
     * @param transactionMode 交易方式  0 全部， 1， 卖出， 2，买入
     * @param sortType        createTime表示按照创建时间排序transactionAmount标识按交易时间排序customPrice按单价进行排序
     * @param sortInt         1:降序 0:升序
     */
    private void queryTransactionInfo(String userId, int page, int rows, int status,
                                      int transactionType,
                                      int transactionMode, String sortType, int sortInt,
                                      Observer<List<TransactionOrderInfo>> observer) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.userId = userId;
        orderDetails.page = page;
        orderDetails.rows = rows;
        orderDetails.status = status;
        orderDetails.transactionType = transactionType;
        if (status == 1) {
            if (transactionMode == 1)
                orderDetails.userType = 2;
            else if (transactionMode == 2)
                orderDetails.userType = 1;
        } else {
            if (transactionMode == 1) {
                orderDetails.sellerId = "none";
            } else if (transactionMode == 2) {
                orderDetails.buyerId = "none";
            }
        }
        if (sortType != null) {
            orderDetails.sorte = sortType;
            orderDetails.order = sortInt;
        } else {
            orderDetails.order = -1;
        }
        String keyId = CombinationSecretKey.getSecretKey("orderDetails.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(orderDetails));
        HuFuCode huFuCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(huFuCode));

        QueryTransactionOrder queryTransactionOrder = RetrofitUtils.create(QueryTransactionOrder.class);
        queryTransactionOrder.queryTransactionOrder(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryAllCertsResult -> {
                    if (queryAllCertsResult.success) {
                        observer.onNext(queryAllCertsResult.data.getList());
                    } else {
                        mHangingTradeSR.setRefreshing(false);
                        if (!MainActivity.isVisitor) {
                            Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }, error -> {
                    mHangingTradeSR.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onLoadMoreRequested() {
        pageCurrently++;
        mHangingTradeSR.setRefreshing(true);
        queryTransactionInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt,
                new Observer<List<TransactionOrderInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<TransactionOrderInfo> value) {
                        mHangingTradeSR.setRefreshing(false);
                        if (status == 0) {
                            if ((value != null && value.isEmpty())) {
                                Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                                mTransactionAdatper.addData(value);
                                mTransactionAdatper.loadMoreEnd();
                            } else {
                                // Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                                mTransactionAdatper.addData(value);
                                mTransactionAdatper.loadMoreComplete();
                            }
                            mTransactionAdatper.notifyDataSetChanged();
                        } else {
                            if ((value != null && value.isEmpty())) {
                                Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                                platformTradeFinishAdapter.addData(value);
                                platformTradeFinishAdapter.loadMoreEnd();
                            } else {
                                // Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                                platformTradeFinishAdapter.addData(value);
                                platformTradeFinishAdapter.loadMoreComplete();
                            }
                            platformTradeFinishAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void onRefresh() {
        mHangingTradeSR.setRefreshing(true);
        pageCurrently = 1;
        queryTransactionInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType,
                transactionMode, sortType, sortInt, new Observer<List<TransactionOrderInfo>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<TransactionOrderInfo> value) {
                        mHangingTradeSR.setRefreshing(false);
                        if (value == null) return;
                        if (status == 0) {
                            if (!mTransactionAdatper.isLoading()) {
                            }
                            mTransactionAdatper.getData().clear();
                            mTransactionAdatper.setNewData(value);
                            if (value.size() < 10) {
                                mTransactionAdatper.loadMoreEnd();
                            }
                            mTransactionAdatper.notifyDataSetChanged();
                        } else {
                            if (!platformTradeFinishAdapter.isLoading()) {
                            }
                            platformTradeFinishAdapter.getData().clear();
                            platformTradeFinishAdapter.setNewData(value);
                            if (value.size() < 10) {
                                platformTradeFinishAdapter.loadMoreEnd();
                            }
                            platformTradeFinishAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void onLoadFirstData(String userAccount) {
        pageCurrently = 1;
        mHangingTradeSR.setRefreshing(true);
        queryTransactionInfo(userAccount, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt,
                new Observer<List<TransactionOrderInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<TransactionOrderInfo> value) {
                        mHangingTradeSR.setRefreshing(false);
                        if (value == null) return;
                        if (status == 0) {
                            mTransactionAdatper.addData(value);
                            if (value.size() < 10) {
                                mTransactionAdatper.loadMoreEnd();
                            }

                            mTransactionAdatper.notifyDataSetChanged();
                        } else {
                            platformTradeFinishAdapter.getData().clear();
                            platformTradeFinishAdapter.addData(value);
                            if (value.size() < 10) {
                                platformTradeFinishAdapter.loadMoreEnd();
                            }

                            platformTradeFinishAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    private class TransactionAdapter extends BaseQuickAdapter<TransactionOrderInfo, TransactionAdapter.TradeHolder> {
        TransactionAdapter(List<TransactionOrderInfo> datas) {
            super(R.layout.item_transaction_info, datas);
        }

        @Override
        protected void convert(TransactionAdapter.TradeHolder helper, TransactionOrderInfo item) {
            helper.orderNumber.setText("订单号" + item.firmId);
            String sellerId = item.sellerId;
            String buyerId = item.buyerId;
            if ("".equals(buyerId) || buyerId == null) {
                helper.initiator.setText("发起人" + DateUtils.getStringHidePhone(sellerId));
                helper.transactionType.setText("卖出");
            } else {
                helper.initiator.setText("发起人" + DateUtils.getStringHidePhone(buyerId));
                helper.transactionType.setText("买入");
            }
            helper.goldCloud.setText(String.format("%.2f",Float.valueOf(DateUtils.getStringFloat(item.transactionAmount))));
            helper.price.setText(String.format("%.2f",Float.valueOf(DateUtils.getStringFloat(item.customPrice))));
            helper.startTime.setText(item.createTime);
            //helper.tadingNotes.setText(item.noteInformation);
        }

        class TradeHolder extends BaseViewHolder {

            private final TextView transactionType;
            private final TextView orderNumber;
            private final TextView initiator;
            private final TextView goldCloud;
            private final TextView price;
            private final TextView startTime;
            // private final TextView tadingNotes;

            public TradeHolder(View itemView) {
                super(itemView);
                transactionType = itemView.findViewById(R.id.transaction_type_txt);
                orderNumber = itemView.findViewById(R.id.order_number_txt);
                initiator = itemView.findViewById(R.id.initiator_txt);
                goldCloud = itemView.findViewById(R.id.gold_cloud_txt);
                price = itemView.findViewById(R.id.price_txt);
                startTime = itemView.findViewById(R.id.start_time_txt);
                // tadingNotes = itemView.findViewById(R.id.tading_notes_txt);
            }
        }
    }


    private boolean isRevoke = false;
    private int tranMode = 0;

    private int confirmtranMode = 0;


    private void showConfirmPaymentPwdDialog(TransactionOrderInfo confirmTransInfo) {
        AlertDialog confirmPaymentPwd = new AlertDialog.Builder(mActivity).create();
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_payment_confim, null);
        confirmPaymentPwd.setView(view);

        final Button confinBtn = view.findViewById(R.id.dialog_payment_confirm);
        confinBtn.setOnClickListener(listener -> {
            EditText password = view.findViewById(R.id.dialog_payment_Et);
            String payPassword = password.getText().toString();
            int[] result = mVirtualCpk.loginSimCos(1, payPassword);
            if (result[0] != 0 && result[0] != 20486) {
                Toast.makeText(mActivity, "支付密码输错错误，还剩余" + result[1] + "次", Toast.LENGTH_SHORT).show();
                return;
            }

            //create request confirm order info
            ConfirmOrderInfoRequest confirmOrderInfoRequest = assembleConfirmOrderInfo(confirmTransInfo);
            confirmationOrderRandom(confirmOrderInfoRequest);
            confirmPaymentPwd.dismiss();
        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            confirmPaymentPwd.dismiss();
        });

        confirmPaymentPwd.show();
    }


    /**
     * 组合确认订单的请求信息
     * 需要登录cos
     *
     * @param transactionInfo 确认订单信息
     * @return 确认订单的请求信息
     */
    private ConfirmOrderInfoRequest assembleConfirmOrderInfo(TransactionOrderInfo
                                                                     transactionInfo) {
        ConfirmOrderInfoRequest confirmOrderInfoRequest = new ConfirmOrderInfoRequest();

        String keyId = mVirtualCpk.getSimID();
        confirmOrderInfoRequest.setKeyId(keyId);

        String safeStr;   //订单创建者的签名数据
        String initatorAccount; // 订单创建者的账号
        String safeConfirmStr;   //确认订单的签名原始数据
        String safeOldStr;      //订单创建者的签名原始数据
        String tansactionAm = DateUtils.getStringDouble(transactionInfo.transactionAmount);
        String transactionTime = DateUtils.getFormateDate(transactionInfo.createTime);
        String appiontMobile;
        if (transactionType == 1) {
            appiontMobile = "system";
        } else {
            appiontMobile = mUserPhoneNumber;
        }
        if (confirmtranMode == 0) { //交易状态  0 作为卖方  1 作为买方
            initatorAccount = transactionInfo.buyerId;
            safeStr = transactionInfo.buyerSafeStr;
            confirmOrderInfoRequest.setStatus(0);
            safeOldStr = CombinationSecretKey.getSignOriginalData(initatorAccount, appiontMobile,
                    transactionType, tansactionAm, transactionTime);
            safeConfirmStr = CombinationSecretKey.getSignOriginalData(initatorAccount, mUserPhoneNumber,
                    transactionType, tansactionAm, transactionTime);
        } else {
            initatorAccount = transactionInfo.sellerId;
            safeStr = transactionInfo.sellerSafeStr;
            confirmOrderInfoRequest.setStatus(1);
            safeOldStr = CombinationSecretKey.getSignOriginalData(appiontMobile, initatorAccount,
                    transactionType, tansactionAm, transactionTime);
            //todo  交易确认时间 为交易发起时间
            safeConfirmStr = CombinationSecretKey.getSignOriginalData(mUserPhoneNumber, initatorAccount,
                    transactionType, tansactionAm, transactionTime);
        }

        boolean isSignSuccess = mVirtualCpk.SM2VerifySign(safeOldStr, safeStr);
        if (!isSignSuccess) {
            Toast.makeText(mActivity, "验签失败！", Toast.LENGTH_LONG).show();
            return null;
        }
        confirmOrderInfoRequest.setFirmId(transactionInfo.firmId);
        confirmOrderInfoRequest.setMobile(mUserPhoneNumber);
        confirmOrderInfoRequest.setSafeStr(mVirtualCpk.SM2Sign(safeConfirmStr));
        return confirmOrderInfoRequest;
    }

    private AlertDialog paymentAlertDialog;

    private void showPaymentPassword(RevokeOrderQuest revokeOder) {
        paymentAlertDialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_payment_confim, null);
        paymentAlertDialog.setView(view);

        final Button confinBtn = view.findViewById(R.id.dialog_payment_confirm);
        confinBtn.setOnClickListener(listener -> {
            EditText password = view.findViewById(R.id.dialog_payment_Et);
            String payPassword = password.getText().toString();

            int[] result = mVirtualCpk.loginSimCos(1, payPassword);
            if (result[0] != 0 && result[0] != 20486) {
                Toast.makeText(mActivity, "支付密码输错错误，还剩余" + result[1] + "次", Toast.LENGTH_SHORT).show();
                return;
            }
            String keyId = mVirtualCpk.getSimID();
            revokeOder.setKeyId(keyId);


            String safeOldStr = CombinationSecretKey.getRevokeOrderSignData(mUserPhoneNumber, revokeOder.getFirmId());
            String safeStr = mVirtualCpk.SM2Sign(safeOldStr);
            revokeOder.setSafeStr(safeStr);
            revokeOrderRandom(revokeOder);
            paymentAlertDialog.dismiss();
        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            paymentAlertDialog.dismiss();
        });

        paymentAlertDialog.show();
    }


    /**
     * 撤销订单 请求Random
     */
    private void revokeOrderRandom(RevokeOrderQuest revokeOrderQuest) {
        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.revokeOrderFirst(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(revokeOrderResult -> {
                    if (revokeOrderResult.success) {
                        byte[] revokeRandom = mVirtualCpk.SM2Decrypt(Base64.decode(revokeOrderResult.random));
                        String code = mVirtualCpk.EncryptData(revokeRandom, new Gson().toJson(revokeOrderQuest));
                        revokeOrderInfo(code);
                    } else {
                        closeVirtualAndRestart();
                        Toast.makeText(getActivity(), "撤销订单失败！" + revokeOrderResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 撤销订单 第二次请求，发送取消信息
     */
    private void revokeOrderInfo(String code) {
        String requestCode = CombinationSecretKey.assembleJsonCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), requestCode);

        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.revokeOrderSecond(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(revokeOrderResult -> {
                    closeVirtualAndRestart();
                    if (revokeOrderResult.success) {
                        Toast.makeText(getActivity(), "撤销订单成功！", Toast.LENGTH_LONG).show();
                        onRefresh();
                    } else {
                        Toast.makeText(getActivity(), "撤销订单失败！" + revokeOrderResult.msg, Toast.LENGTH_LONG).show();
                    }
                    if (paymentAlertDialog.isShowing())
                        paymentAlertDialog.dismiss();
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    /**
     * 确认订单第一次请求
     *
     * @param revokeOrderQuest 请求订单的信息
     */
    private void confirmationOrderRandom(ConfirmOrderInfoRequest revokeOrderQuest) {
        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.confirmationOrderFirst(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(revokeOrderResult -> {
                    if (revokeOrderResult.success) {
                        byte[] confirmRandom = mVirtualCpk.SM2Decrypt(Base64.decode(revokeOrderResult.random));
                        String confirm64 = Base64.encode(confirmRandom);
                        String code = mVirtualCpk.EncryptData(confirmRandom, new Gson().toJson(revokeOrderQuest));
                        confirmationOrderInfo(code);
                    } else {
                        closeVirtualAndRestart();
                        Toast.makeText(getActivity(), "确认订单失败！" + revokeOrderResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void confirmationOrderInfo(String code) {
        String confirmCode = CombinationSecretKey.assembleJsonCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), confirmCode);

        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.confirmationOrderSecond(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(revokeOrderResult -> {
                    closeVirtualAndRestart();
                    if (revokeOrderResult.success) {
                        onRefresh();
                        Toast.makeText(getActivity(), "确认订单成功！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), revokeOrderResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

}
