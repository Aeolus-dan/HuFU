package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.MainActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.PaymentActivity;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.GoldenOrder;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryTransactionOrder;
import com.hufuinfo.hufudigitalgoldenchain.bean.ConfirmOrderInfoRequest;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetails;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetailsResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.RevokeOrderQuest;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;

import org.apache.xerces.impl.dv.util.Base64;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PersonalTranInfoFragment extends Fragment {
    final static String THIS_FILE = "PersonalTranInfo";
    private Activity mActivity;
    private RecyclerView transactionInfoRe;
    private boolean firstTransactionType = true;
    private boolean firstShowFragment = true;
    private View divider;
    private Button nextPageBtn, prePageBtn, firstPageBtn, endPageBtn;
    private TextView personalTimeTv;
    private TextView infoPageText;
    private Switch mTransactionTypeSwitch;
    private Spinner dividePageSpinner, spinnerTransactionType;
    private SwipeRefreshLayout personalTrainSrl;
    private String priceGold;

    private View goldCouldView;
    private ImageView goldCouldIv;
    private View priceGoldView;
    private ImageView priceGoldIv;
    private View goldTimeView;
    private ImageView goldTimeIv;

    private boolean isSortGoldCould = false;
    private boolean isSortPriceGold = false;
    private boolean isSortGoldTime = false;
    // 分页参数
    //页面的总数
    private int totalPage = 0;
    //每页的行数
    private int rows = 10;
    // 数据的总量
    private int totalData = 0;
    //当前页面
    private int pageCurrently = 1;
    private String mUserPhoneNumber;
    private int status = 1;  //status ==0 未完成， status== 1 已完成
    private int transactionType = 0; //0 :点对点交易 1： 挂盘交易
    private int transactionMode = 0;  // 0：全部交易， 1：买入 2：买出

    private VirtualCpk mVirtualCpk;
    private SharedPreferences mUserSharedPre;
    private String antiFake;
    private String sortType = "createTime"; //排序类型
    private int sortInt = 0; //排序方式  1:降序 0:升序

    private TabLayout personalTradeTl;
    private ViewPager personalTradeVp;

    private TransactionOrderInfo mTransactionInfo;

    public PersonalTranInfoFragment() {
    }

    public static PersonalTranInfoFragment newInstance() {
        PersonalTranInfoFragment fragment = new PersonalTranInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mUserSharedPre = getActivity().getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        priceGold = mUserSharedPre.getString(ConstantUtils.GOLD_PRICE, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_tran_info, container, false);
        initView(view);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantUtils.REQUEST_CODE_CONFIRM_ORDER_PAYMENT) {
            if (resultCode == ConstantUtils.PAYMENT_RESULT_CODE && data != null) {
                boolean paymentReuslt = data.getBooleanExtra(ConstantUtils.PAYMENT_RESULT, false);
                if (paymentReuslt) {
                    //创建订单
                    ConfirmOrderInfoRequest confirmOrderInfoRequest = assembleConfirmOrderInfo(mTransactionInfo);
                    if (confirmOrderInfoRequest == null) {
                        closeVirtualAndRestart();
                        return;
                    }
                    confirmationOrderRandom(confirmOrderInfoRequest);
                } else {
                    //创建订单失败
                    showCreateOrderResultDialog("订单信息", "交易失败！");
                    closeVirtualAndRestart();
                }
            } else {
                //创建订单失败！
                showCreateOrderResultDialog("订单信息", "交易失败！");
                closeVirtualAndRestart();
            }
        }
    }

    private void initView(View view) {
        personalTradeTl = view.findViewById(R.id.personal_trade_tab_layout);
        personalTradeVp = view.findViewById(R.id.personal_trade_vp);
        personalTradeVp.setAdapter(new PersonalTradeAdapter(getChildFragmentManager()));
        personalTradeTl.setupWithViewPager(personalTradeVp);
        personalTradeTl.getTabAt(0).setText("已完成");
        personalTradeTl.getTabAt(1).setText("未完成");
        view.findViewById(R.id.personal_trade_return_tv).setOnClickListener(listener -> {
            mActivity.onBackPressed();
        });
       /* transactionInfoRe = view.findViewById(R.id.transaction_order_info_re);
        divider = view.findViewById(R.id.divider);
        mTransactionTypeSwitch = view.findViewById(R.id.switch_transaction_type);
        mTransactionTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sortInt = -1;
                sortType = null;
                showWhiteSortView();
                if (b) {
                    //已完成到未完成
                    //   Log.e(THIS_FILE, " M swtich  is true   ");
                    personalTimeTv.setText(R.string.create_time);
                    status = 0;
                    transactionMode = 0;
                    firstShowFragment = true;
                    pageCurrently = 1;
                    queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
                } else {
                    //未完成到已完成
                    personalTimeTv.setText(R.string.complete_time);
                    status = 1;
                    transactionMode = 0;
                    firstShowFragment = true;
                    pageCurrently = 1;
                    queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
                }
            }
        });
        firstPageBtn = view.findViewById(R.id.first_page_btn);
        firstPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageCurrently == 1) return;
                pageCurrently = 1;
                dividePageSpinner.setSelection(pageCurrently - 1, true);
            }
        });
        nextPageBtn = view.findViewById(R.id.next_page_btn);
        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageCurrently + 1 > totalPage) return;
                pageCurrently = pageCurrently + 1;
                dividePageSpinner.setSelection(pageCurrently - 1, true);
            }
        });
        prePageBtn = view.findViewById(R.id.pre_page_btn);
        prePageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageCurrently == 1) return;
                pageCurrently = pageCurrently - 1;
                dividePageSpinner.setSelection(pageCurrently - 1, true);
            }
        });
        endPageBtn = view.findViewById(R.id.end_page_btn);
        endPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageCurrently = totalPage;
                dividePageSpinner.setSelection(pageCurrently - 1, true);
            }
        });
        infoPageText = view.findViewById(R.id.info_page_txt);
        dividePageSpinner = view.findViewById(R.id.spinner_divide_page);
        spinnerTransactionType = view.findViewById(R.id.spinner_transaction_type);
        spinnerTransactionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (firstTransactionType) {
                    firstTransactionType = false;
                    return;
                }
                transactionMode = i;
                firstShowFragment = true;
                pageCurrently = 1;
                queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        personalTrainSrl = view.findViewById(R.id.personal_train_srl);
        personalTrainSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCurrently = 1;
                queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
            }
        });

        goldCouldView = view.findViewById(R.id.gold_cloud_ll);
        goldCouldIv = view.findViewById(R.id.gold_cloud_iv);
        goldCouldView.setOnClickListener(listener -> {
            if (isSortGoldCould) {
                sortInt = 0;
                goldCouldIv.setImageResource(R.drawable.arrow_up);
                isSortGoldCould = false;
            } else {
                sortInt = 1;
                goldCouldIv.setImageResource(R.drawable.arrow_down);
                isSortGoldCould = true;
            }
            goldCouldView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            priceGoldView.setBackgroundColor(getResources().getColor(R.color.white));
            goldTimeView.setBackgroundColor(getResources().getColor(R.color.white));
            sortType = "transactionAmount";
            pageCurrently = 1;
            firstShowFragment = true;
            queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
        });
        goldTimeIv = view.findViewById(R.id.tran_complete_time_iv);
        goldTimeView = view.findViewById(R.id.tran_complete_time_ll);
        goldTimeView.setOnClickListener(listener -> {
            if (isSortGoldTime) {
                sortInt = 0;
                goldTimeIv.setImageResource(R.drawable.arrow_up);
                isSortGoldTime = false;
            } else {
                sortInt = 1;
                goldTimeIv.setImageResource(R.drawable.arrow_down);
                isSortGoldTime = true;
            }
            goldCouldView.setBackgroundColor(getResources().getColor(R.color.white));
            goldTimeView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            priceGoldView.setBackgroundColor(getResources().getColor(R.color.white));
            sortType = "createTime";
            pageCurrently = 1;
            firstShowFragment = true;
            queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
        });

        priceGoldView = view.findViewById(R.id.tran_price_ll);
        priceGoldIv = view.findViewById(R.id.tran_price_iv);
        priceGoldView.setOnClickListener(listener -> {
            if (isSortPriceGold) {
                sortInt = 1;
                priceGoldIv.setImageResource(R.drawable.arrow_up);
                isSortPriceGold = false;
            } else {
                sortInt = 0;
                priceGoldIv.setImageResource(R.drawable.arrow_down);
                isSortPriceGold = true;
            }
            sortType = "customPrice";
            goldCouldView.setBackgroundColor(getResources().getColor(R.color.white));
            goldTimeView.setBackgroundColor(getResources().getColor(R.color.white));
            priceGoldView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            pageCurrently = 1;
            firstShowFragment = true;
            queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
        });

        personalTimeTv = view.findViewById(R.id.personal_time_tv);*/

    }

    private void refreshPersonalTranInfo() {
        pageCurrently = 1;
        queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
    }

    private void showWhiteSortView() {
        goldCouldView.setBackgroundColor(getResources().getColor(R.color.white));
        goldTimeView.setBackgroundColor(getResources().getColor(R.color.white));
        priceGoldView.setBackgroundColor(getResources().getColor(R.color.white));
    }

    /**
     * 查询指定用户订单信息
     *
     * @param userId          用户账号Id
     * @param page            查询页面
     * @param rows            页面的行数
     * @param status          订单状态  1:已完成 0：未完成
     * @param transactionType 成交类型  0：点对点；1：挂盘交易
     * @param transactionMode 交易方式 0：全部交易， 1：买入交易 2：买出交易
     * @param sorte           createTime表示按照创建时间排序transactionAmount标识按交易时间排序customPrice按单价进行排序
     * @param orderInt        1:降序 0:升序
     */
    private void queryPersoanlTranInfo(String userId, int page, int rows, int status, int transactionType, int transactionMode, String sorte, int orderInt) {
        OrderDetails orderDetails = new OrderDetails();
        if (transactionMode == 0) {
            orderDetails.userId = userId;
        } else if (transactionMode == 1) {
            orderDetails.buyerId = userId;
        } else {
            orderDetails.sellerId = userId;
        }
        if (sorte != null) {  //是否排序  不排序 null
            orderDetails.sorte = sorte;
            orderDetails.order = orderInt;
        }
        orderDetails.page = page;
        orderDetails.rows = rows;
        orderDetails.status = status;
        orderDetails.transactionType = transactionType;
        String keyId = CombinationSecretKey.getSecretKey("orderDetails.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(orderDetails));
        if (MainActivity.isVisitor) {
            hufuCode = "";
        }
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryTransactionOrder queryTransactionOrder = RetrofitUtils.create(QueryTransactionOrder.class);
        queryTransactionOrder.queryTransactionOrder(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryAllCertsResult -> {
                    personalTrainSrl.setRefreshing(false);
                    if (queryAllCertsResult.success) {
                          showRecyclerView(queryAllCertsResult.data);
                    } else {
                        if (!MainActivity.isVisitor) {
                            Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }, error -> {
                    personalTrainSrl.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

    private PersonalTrasactionItem mPersonaTrasactionItem;


    private void showRecyclerView(OrderDetailsResult.Data datas) {
        divider.setVisibility(View.VISIBLE);
        if (mPersonaTrasactionItem == null) mPersonaTrasactionItem = new PersonalTrasactionItem();
        transactionInfoRe.removeItemDecoration(mPersonaTrasactionItem);
        transactionInfoRe.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionInfoRe.setAdapter(new PersonalTransactionAdapter(datas));
        transactionInfoRe.addItemDecoration(mPersonaTrasactionItem);
        transactionInfoRe.setHasFixedSize(true);
        totalData = datas.getTotal();
        totalPage = totalData / rows;
        int lastPageDataItem = totalData % rows;
        if (lastPageDataItem != 0) totalPage = totalPage + 1;
        if (totalPage == 0) totalPage = 1;
        infoPageText.setText("共" + totalPage + "页  " + "共" + totalData + "条记录");
        if (firstShowFragment) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                    getPageData(totalPage));
            dividePageSpinner.setAdapter(adapter);
            dividePageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (firstShowFragment) {
                        firstShowFragment = false;
                        return;
                    }
                    pageCurrently = i + 1;
                    queryPersoanlTranInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }

    }


    private List<String> getPageData(int totalPage) {
        List<String> dataList = new ArrayList<String>();
        for (int i = 1; i < totalPage + 1; i++) {
            dataList.add("" + i);
        }
        return dataList;
    }

    private AlertDialog paymentAlertDialog;

    private void showPaymentPassword(RevokeOrderQuest revokeOder, boolean isRevoke, AlertDialog dialogPlus) {
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
            dialogPlus.dismiss();
            paymentAlertDialog.dismiss();
        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            paymentAlertDialog.dismiss();
        });

        paymentAlertDialog.show();
    }

    private class PersonalTransactionAdapter extends RecyclerView.Adapter<PersonalTransactionAdapter.PersoanlTranHolder> {
        OrderDetailsResult.Data datas;

        PersonalTransactionAdapter(OrderDetailsResult.Data datas) {
            this.datas = datas;
        }


        @Override
        public PersonalTransactionAdapter.PersoanlTranHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PersonalTransactionAdapter.PersoanlTranHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_platform_transaction_info, parent, false));
        }

        @Override
        public void onBindViewHolder(PersonalTransactionAdapter.PersoanlTranHolder holder, int position) {

            holder.itemView.setOnClickListener(view -> {
                showTransactionOrderInfo(datas.getList().get(position));
            });
            holder.orderNumber.setText(datas.getList().get(position).firmId);
            String sellerId = datas.getList().get(position).sellerId;
            String buyerId = datas.getList().get(position).buyerId;
            String initiatorAccount = datas.getList().get(position).initiatorId;
            if (mUserPhoneNumber.equals(buyerId)) {
                holder.transactionType.setText("买入");
            } else {
                holder.transactionType.setText("卖出");
            }

            if (initiatorAccount.equals(buyerId)) {
                holder.traderTv.setText(DateUtils.getStringHidePhone(DateUtils.getStringHidePhone(sellerId)));
            } else {
                holder.traderTv.setText(DateUtils.getStringHidePhone(DateUtils.getStringHidePhone(buyerId)));
            }

            holder.initiator.setText(DateUtils.getStringHidePhone(datas.getList().get(position).initiatorId));
            holder.goldCloud.setText(DateUtils.getStringFloat(datas.getList().get(position).transactionAmount));
            holder.price.setText(DateUtils.getStringFloat(datas.getList().get(position).customPrice));
            if (datas.getList().get(position).status == 0) {
                holder.startTime.setText(datas.getList().get(position).createTime);
            } else {
                holder.startTime.setText(datas.getList().get(position).transactionDate);
            }
            holder.tadingNotes.setText(datas.getList().get(position).noteInformation);

        }

        @Override
        public int getItemCount() {
            return datas.getList().size();
        }

        class PersoanlTranHolder extends RecyclerView.ViewHolder {

            private final TextView transactionType;
            private final TextView orderNumber;
            private final TextView initiator;
            private final TextView goldCloud;
            private final TextView price;
            private final TextView startTime;
            private final TextView tadingNotes;
            private final TextView traderTv;

            public PersoanlTranHolder(View itemView) {
                super(itemView);
                transactionType = itemView.findViewById(R.id.transaction_type_txt);
                orderNumber = itemView.findViewById(R.id.order_number_txt);
                initiator = itemView.findViewById(R.id.initiator_txt);
                goldCloud = itemView.findViewById(R.id.gold_cloud_txt);
                price = itemView.findViewById(R.id.price_txt);
                startTime = itemView.findViewById(R.id.start_time_txt);
                tadingNotes = itemView.findViewById(R.id.tading_notes_txt);
                traderTv = itemView.findViewById(R.id.trader_txt);
            }
        }
    }

    private void showTransactionOrderInfo(TransactionOrderInfo transactionInfo) {
        final AlertDialog dialogPlusBuilder = new AlertDialog.Builder(getActivity()).create();
        View view = View.inflate(mActivity, R.layout.dialog_personal_imcomplete_tran_info, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getId() == R.id.trade_tran_btn) {
                    // 交易

                    if (tranMode == 0) {
                        //以自己为参照物， 作为卖出方确认订单
                        showConfirmPaymentPwdDialog(transactionInfo);
                        dialogPlusBuilder.dismiss();
                    } else {
                        //以自己为参照物, 作为买方确认订单

                        mTransactionInfo = transactionInfo;

                        BigDecimal goldPriceBig = new BigDecimal(transactionInfo.customPrice);
                        BigDecimal goldNumBig = new BigDecimal(transactionInfo.transactionAmount);
                        String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNumBig).floatValue());

                        Intent payIntent = new Intent(mActivity, PaymentActivity.class);
                        Bundle paymentBundle = new Bundle();
                        paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNumBig.doubleValue()));
                        paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
                        paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 2);
                        paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
                        payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
                        startActivityForResult(payIntent, ConstantUtils.REQUEST_CODE_CONFIRM_ORDER_PAYMENT);

                        dialogPlusBuilder.dismiss();
                    }
                } else if (view.getId() == R.id.revoke_tran_btn) {
                    //撤销交易
                    RevokeOrderQuest mReOrder = createRevokeOrderInfo(transactionInfo, tranMode);
                    showPaymentPassword(mReOrder, true, dialogPlusBuilder);
                }
            }
        });
        dialogPlusBuilder.setView(view);
        dialogPlusBuilder.show();
        writeDataDialog(transactionInfo);
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

    int tranMode = -1;

    private void writeDataDialog(TransactionOrderInfo transactionInfo) {
        TextView nowGoldPriceTv = mActivity.findViewById(R.id.now_gold_price_tv);
        nowGoldPriceTv.setText("今日平均金价：" + priceGold + "元/金本币");
        TextView goldNumTv = mActivity.findViewById(R.id.gold_num_tv);
        goldNumTv.setText("订单金本币数：" + transactionInfo.transactionAmount);
        TextView goldPriceTv = mActivity.findViewById(R.id.inter_gold_price_tv);
        goldPriceTv.setText("金本币单价：" + transactionInfo.customPrice);
        TextView orderPriceTv = mActivity.findViewById(R.id.order_price_tv);
        BigDecimal tranAmountBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.transactionAmount));
        BigDecimal goldPriceBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.customPrice));
        orderPriceTv.setText("交易金额：" + tranAmountBig.multiply(goldPriceBig).floatValue());
        TextView orderCreateTv = mActivity.findViewById(R.id.order_seller_tv);
        TextView orderBuyerTv = mActivity.findViewById(R.id.order_buyer_tv);
        TextView orderNumTv = mActivity.findViewById(R.id.order_num_tv);
        orderNumTv.setText("订单号：" + transactionInfo.firmId);
        TextView tranModeTv = mActivity.findViewById(R.id.tran_mode_tv);
        orderCreateTv.setText("订单发起人：" + DateUtils.getStringHidePhone(transactionInfo.initiatorId));
        if (mUserPhoneNumber.equals(transactionInfo.buyerId)) {
            tranModeTv.setText("交易类型：买入");
            tranMode = 0;
        } else {
            tranModeTv.setText("交易类型：卖出");
            tranMode = 1;
        }
        if (transactionInfo.initiatorId.equals(transactionInfo.buyerId)) {
            orderBuyerTv.setText("订单接受人：" + DateUtils.getStringHidePhone(transactionInfo.sellerId));
        } else {
            orderBuyerTv.setText("订单接受人：" + DateUtils.getStringHidePhone(transactionInfo.buyerId));
        }
        if (status == 1) {   //完成
            getActivity().findViewById(R.id.trade_tran_btn).setVisibility(View.GONE);
            getActivity().findViewById(R.id.trade_tran_btn).setVisibility(View.GONE);
        } else {  //未完成
            if (transactionInfo.initiatorId.equals(mUserPhoneNumber)) {
                getActivity().findViewById(R.id.trade_tran_btn).setVisibility(View.GONE);
            } else {
                getActivity().findViewById(R.id.trade_tran_btn).setVisibility(View.VISIBLE);
            }
        }

        TextView tranNoteTV = mActivity.findViewById(R.id.order_note_tv);
        tranNoteTV.setText("交易备注:" + transactionInfo.noteInformation);

    }

    @SuppressWarnings("deprecation")
    private class PersonalTrasactionItem extends RecyclerView.ItemDecoration {
        Paint dividerPaint;
        int dividerHeight;

        public PersonalTrasactionItem() {
            //paint
            dividerPaint = new Paint();
            dividerPaint.setColor(getResources().getColor(R.color.dividerColor));
            dividerHeight = getResources().getDimensionPixelSize(R.dimen.divider_height);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = dividerHeight;
        }


        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            int right = parent.getWidth() - parent.getPaddingRight();
            int left = parent.getPaddingLeft();

            for (int i = 0; i < childCount; i++) {
                View childAt = parent.getChildAt(i);
                int top = childAt.getBottom();
                int bottom = top + dividerHeight;
                c.drawRect(left, top, right, bottom, dividerPaint);
            }
        }
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
                        refreshPersonalTranInfo();
                        personalTrainSrl.setRefreshing(true);

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
     * 组合确认订单的请求信息
     * 需要登录cos
     *
     * @param transactionInfo 确认订单信息
     * @return 确认订单的请求信息
     */
    private ConfirmOrderInfoRequest assembleConfirmOrderInfo(TransactionOrderInfo transactionInfo) {
        ConfirmOrderInfoRequest confirmOrderInfoRequest = new ConfirmOrderInfoRequest();

        String keyId = mVirtualCpk.getSimID();
        confirmOrderInfoRequest.setKeyId(keyId);

        String safeStr;   //订单创建者的签名数据
        String initatorAccount; // 订单创建者的账号
        String safeConfirmStr;   //确认订单的签名原始数据
        String safeOldStr;      //订单创建者的签名原始数据
        String tansactionAm = DateUtils.getStringDouble(transactionInfo.transactionAmount);
        String transactionTime = DateUtils.getFormateDate(transactionInfo.createTime);
        int transactionType = 0; //交易类型  0 点对点交易  1 平台挂盘交易

        if (transactionInfo.status == 1) { //交易状态  0 作为卖方  1 作为买方
            initatorAccount = transactionInfo.buyerId;
            safeStr = transactionInfo.buyerSafeStr;
            confirmOrderInfoRequest.setStatus(0);
            safeOldStr = CombinationSecretKey.getSignOriginalData(initatorAccount, mUserPhoneNumber,
                    transactionType, tansactionAm, transactionTime);
            //todo  确认订单 以创建订单的时间为准
            safeConfirmStr = CombinationSecretKey.getSignOriginalData(initatorAccount, mUserPhoneNumber,
                    transactionType, tansactionAm, transactionTime);
        } else {
            initatorAccount = transactionInfo.sellerId;
            safeStr = transactionInfo.sellerSafeStr;
            confirmOrderInfoRequest.setStatus(1);
            //Todo createTime   ???
            safeOldStr = CombinationSecretKey.getSignOriginalData(mUserPhoneNumber, initatorAccount,
                    transactionType, tansactionAm, transactionTime);
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

    /**
     * 确认订单 输入支付密码
     *
     * @param confirmTransInfo
     */
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
                        Toast.makeText(getActivity(), "确认订单成功！", Toast.LENGTH_LONG).show();
                        refreshPersonalTranInfo();
                        personalTrainSrl.setRefreshing(true);
                    } else {
                        Toast.makeText(getActivity(), revokeOrderResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    closeVirtualAndRestart();
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void showCreateOrderResultDialog(String title, String msg) {
        AlertDialog.Builder cancelAlertBuilder = new AlertDialog.Builder(mActivity);
        cancelAlertBuilder.setTitle(title);
        cancelAlertBuilder.setMessage(msg);
        cancelAlertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        cancelAlertBuilder.create().show();
    }

    /**
     * 关闭 Virtual  Cos , 退出登录状态
     * init  Cos
     */
    private void closeVirtualAndRestart() {
        mVirtualCpk.closeVirtualCos();
        mVirtualCpk = VirtualCpk.getInstance(mActivity);
    }

    class PersonalTradeAdapter extends FragmentPagerAdapter {
        public PersonalTradeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return TradeFragment.newInstance(0, 1);
            } else if (i == 1) {
                return TradeFragment.newInstance(0, 0);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
