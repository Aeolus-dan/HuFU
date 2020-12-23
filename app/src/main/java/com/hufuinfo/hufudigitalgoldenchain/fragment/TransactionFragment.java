package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.activity.MainActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.PaymentActivity;
import com.hufuinfo.hufudigitalgoldenchain.activity.ScanPaymentActivity;
import com.hufuinfo.hufudigitalgoldenchain.adapter.IndExpandableAdapter;
import com.hufuinfo.hufudigitalgoldenchain.adapter.PersonalTranAdatper;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.GoldenOrder;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryAuthPrice;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryTransactionOrder;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserBalance;
import com.hufuinfo.hufudigitalgoldenchain.bean.AuthPriceResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.ConfirmOrderInfoRequest;
import com.hufuinfo.hufudigitalgoldenchain.bean.FirmBargain;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetails;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetailsResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.PhoneNumber;
import com.hufuinfo.hufudigitalgoldenchain.bean.RevokeOrderQuest;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.UserBalanceResult;
import com.hufuinfo.hufudigitalgoldenchain.dialog.IssueTradeFragment;
import com.hufuinfo.hufudigitalgoldenchain.dialog.IssueTradeOrderFragmentDialog;
import com.hufuinfo.hufudigitalgoldenchain.scancode.CaptureActivity;
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

import static android.app.Activity.RESULT_OK;


public class TransactionFragment extends Fragment {
    private final static String THIS_FILE = "TransactionFragment";
    private Activity mActivity;

    private RecyclerView transactionInfoRe;

    private AuthPriceResult.Data mAuthPriceData;
    private UserBalanceResult.Data mUserBalanceData;
    private ExpandableListView mExpandableAuth;
    private ExpandableListView mExpandableUserBa;
    private Switch mTransactionTypeSwitch;
    private View divider, recipinetView;
    private Button nextPageBtn, prePageBtn, firstPageBtn, endPageBtn;
    private Button issueTranBtn;
    private Spinner dividePageSpinner, spinnerTransactionType;
    private TextView infoPageText, recipientTv;
    private String priceGold;
    private SwipeRefreshLayout mTransSwipeRL;

    private FirmBargain mFirmBargin; //创建订单信息
    private TransactionOrderInfo mTransactionInfo;

    private View goldCouldView;
    private ImageView goldCouldIv;
    private View priceGoldView;
    private ImageView priceGoldIv;
    private View goldTimeView;
    private ImageView goldTimeIv;

    private TextView interGoldPriceTv, officeGoldPriceTv, averageGoldPriceTv;

    private boolean isSortGoldCould = false;
    private boolean isSortPriceGold = false;
    private boolean isSortGoldTime = false;
    private String sortType = "createTime"; //排序类型
    private int sortInt = 0; //排序方式  1:降序 0:升序

    // 分页参数
    //页面的总数
    private int totalPage = 0;
    //每页的行数
    private int rows = 10;
    // 数据的总量
    private int totalData = 0;
    //当前页面
    private int pageCurrently = 1;

    private boolean showDivider = false;
    //是否需要重新计算总页数
    private boolean firstShowFragment = true;
    private boolean isPlatformOrder = true;
    private boolean firstTransactionType = true;
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

    private int createTransactionMode = 0;


    private VirtualCpk mVirtualCpk;
    private byte[] random;

    private SharedPreferences mUserSharedPre;
    private String antiFake;

    private TabLayout mTransactionTabLayout;
    private ViewPager mTransactionViewPage;
    private TransactionPageAdapter mTranasactionPageAdapter;

    private IssueTradeOrderFragmentDialog issueTradeOrderFragmentDialog;

    private boolean isVisitor = false;

    public TransactionFragment() {

    }

    @SuppressLint("ValidFragment")
    public  TransactionFragment(boolean isVisitor){
        this.isVisitor = isVisitor;
    }

    @Override
    public void onAttach(Context context) {
        mActivity = getActivity();
        mUserSharedPre = mActivity.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE);
        mVirtualCpk = VirtualCpk.getInstance(getActivity());
        mUserPhoneNumber = mUserSharedPre.getString(ConstantUtils.USER_ACCOUNT, null);
        antiFake = mUserSharedPre.getString(ConstantUtils.ANTI_FAKE, null);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_transaction, container, false);

        initView(root);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryAuthPrice();
        queryUserBalance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView(View view) {

        interGoldPriceTv = view.findViewById(R.id.inter_gold_price_tv);
        officeGoldPriceTv = view.findViewById(R.id.office_gold_price_tv);
        averageGoldPriceTv = view.findViewById(R.id.average_gold_price_tv);
        mTransactionTabLayout = view.findViewById(R.id.transaction_tab_layout);
        mTransactionViewPage = view.findViewById(R.id.transaction_container);
        if (isVisitor){
            view.findViewById(R.id.create_order_ibtn).setVisibility(View.GONE);
        }
        mTranasactionPageAdapter = new TransactionPageAdapter(getChildFragmentManager());
        mTransactionViewPage.setAdapter(mTranasactionPageAdapter);
        mTransactionTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mTransactionViewPage.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mTransactionViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                mTransactionTabLayout.setScrollPosition(i, v, true);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        transactionInfoRe = view.findViewById(R.id.transaction_order_info_re);
        divider = view.findViewById(R.id.divider);
        view.findViewById(R.id.create_order_ibtn).setOnClickListener(listener -> {
            int selectedItem = mTransactionViewPage.getCurrentItem();
            if (selectedItem == 0) {
                selectedItem = 1;
            } else {
                selectedItem = 0;
            }
            issueTradeOrderFragmentDialog = IssueTradeOrderFragmentDialog.getInstance(new IssueTradeFragment.CreateIssueOrder() {
                @Override
                public void onCreateIssueOreder(FirmBargain firmBargain, int transactionMode) {
                    mFirmBargin = firmBargain;
                    createTransactionMode = transactionMode;
                    if (transactionMode == 0) {
                        //作为买方创建订单
                        Double goldNum = mFirmBargin.getTransactionAmount();
                        Float priceGoldf = mFirmBargin.getPrice();
                        BigDecimal goldPriceBig = new BigDecimal(priceGoldf);
                        BigDecimal goldNumBig = new BigDecimal(goldNum);
                        String totalMoney = DateUtils.getStringDouble(goldPriceBig.multiply(goldNumBig).floatValue());

                        Intent payIntent = new Intent(mActivity, PaymentActivity.class);
                        Bundle paymentBundle = new Bundle();
                        paymentBundle.putString(ConstantUtils.GOLE_NUMBER, DateUtils.getStringDouble(goldNumBig.doubleValue()));
                        paymentBundle.putString(ConstantUtils.GOLD_TOTAL_MONEY, totalMoney);
                        paymentBundle.putInt(ConstantUtils.PAYMENT_TYPE, 2);
                        paymentBundle.putString(ConstantUtils.BUYER_GOLD_PRICE, DateUtils.getStringDouble(goldPriceBig.doubleValue()));
                        payIntent.putExtra(ConstantUtils.PAYMENT_DATA, paymentBundle);
                        startActivityForResult(payIntent, ConstantUtils.REQUEST_CODE_PAYMENT);

                    } else {
                        showDialogPassword(firmBargain);
                    }
                }
            }, selectedItem);
            issueTradeOrderFragmentDialog.show(getFragmentManager(), "IssueOrder");
        });

        view.findViewById(R.id.scan_attorn_btn).setOnClickListener(listener -> {
            Intent openCameraIntent = new Intent(getActivity(), CaptureActivity.class);
            startActivityForResult(openCameraIntent, CAMERA_REQUEST);
        });

    }

    private TradeFragment hangingPlatformTradeFragment;
    private TradeFragment hangingPersonalTradeFragment;

    class TransactionPageAdapter extends FragmentPagerAdapter {
        public TransactionPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                if (hangingPlatformTradeFragment == null) {
                    hangingPlatformTradeFragment = TradeFragment.newInstance(1, 0);
                }
                return hangingPlatformTradeFragment;
            } else if (i == 1) {
                if (hangingPersonalTradeFragment == null)
                    hangingPersonalTradeFragment = TradeFragment.newInstance(0, 0);
                return hangingPersonalTradeFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


    public void refreshTransactionAndUserInfo() {
        firstShowFragment = true;
        if (isPlatformOrder) {
            queryTransactionInfo(null, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
        } else {
            queryTransactionInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
        }
        queryUserBalance();
        queryAuthPrice();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConstantUtils.REQUEST_CODE_PAYMENT) {
            if (resultCode == ConstantUtils.PAYMENT_RESULT_CODE && data != null) {
                Log.e(THIS_FILE, "request_create_order_code");
                boolean paymentReuslt = data.getBooleanExtra(ConstantUtils.PAYMENT_RESULT, false);
                if (paymentReuslt) {
                    //创建订单
                    requestCreateOrderRandom(mFirmBargin);
                } else {
                    //创建订单失败
                    showCreateOrderResultDialog("订单信息", "创建订单失败！");
                    closeVirtualAndRestart();
                }
            } else {
                //创建订单失败！
                showCreateOrderResultDialog("订单信息", "创建订单失败！");
                closeVirtualAndRestart();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                String result = bundle.getString("result");
                if (result == null) {
                    Toast.makeText(mActivity, "扫描失败", Toast.LENGTH_LONG).show();
                }
                String key = DateUtils.getYear() + "Income&Collection&By&MErchants";
                String receiverQr = mVirtualCpk.DecryptData(key.getBytes(), result);
                showIsPaynent(receiverQr);
            }
        }
    }

    private void showIsPaynent(String receiverInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.stat_sys_warning);
        builder.setMessage("是否选择提款");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(mActivity, ScanPaymentActivity.class);
                intent.putExtra("receiverInfo", receiverInfo);
                startActivity(intent);
                dialog.dismiss();
            }
        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private AlertDialog paymentAlertDialog;
    private final static int CAMERA_REQUEST = 102;

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

            if (isRevoke) {
                String safeOldStr = CombinationSecretKey.getRevokeOrderSignData(mUserPhoneNumber, revokeOder.getFirmId());
                String safeStr = mVirtualCpk.SM2Sign(safeOldStr);
                revokeOder.setSafeStr(safeStr);
                revokeOrderRandom(revokeOder);
            }
            paymentAlertDialog.dismiss();
        });
        final Button cancelBtn = view.findViewById(R.id.dialog_payment_cancel);
        cancelBtn.setOnClickListener(listener -> {
            paymentAlertDialog.dismiss();
        });
        paymentAlertDialog.show();
    }


    /**
     * 显示输入支付密码对话框！
     *
     * @param firmBargain 创建订单信息对象
     */
    private void showDialogPassword(FirmBargain firmBargain) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final EditText editText = new EditText(mActivity);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        builder.setTitle("请输入8位支付密码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pin = editText.getText().toString();
                        int[] loginResult = mVirtualCpk.loginSimCos(1, pin);
                        if (loginResult[0] != 0 && loginResult[0] != 20486) {
                            Toast.makeText(mActivity, "请输入正确的支付密码码!剩余次数为" + loginResult[1] + "次", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mVirtualCpk.isUserCertifciation();
                        requestCreateOrderRandom(firmBargain);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 请求创建交易随机数Random
     *
     * @param firmBargain 交易订单信息
     */
    private void requestCreateOrderRandom(FirmBargain firmBargain) {

        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.createFirmBargin(antiFake)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        isPlatformOrder = mTransactionViewPage.getCurrentItem() == 0;
                        String keyId = mVirtualCpk.getSimID();
                        if (keyId == null) {
                            Toast.makeText(mActivity, "请完善个人资料并获取证书！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        firmBargain.setKeyId(keyId);

                        String safeOriginal;
                        String tansactionAmountOffice = DateUtils.getStringDouble(firmBargain.getTransactionAmount());
                        if (createTransactionMode == 0) { //作为买方创建订单
                            if (isPlatformOrder) {
                                safeOriginal = CombinationSecretKey.getSignOriginalData(mUserPhoneNumber, "system",
                                        firmBargain.getTransactionType(), tansactionAmountOffice);
                            } else {
                                safeOriginal = CombinationSecretKey.getSignOriginalData(mUserPhoneNumber, firmBargain.getAppointMobile(),
                                        firmBargain.getTransactionType(), tansactionAmountOffice);
                            }
                        } else {
                            if (isPlatformOrder) {
                                safeOriginal = CombinationSecretKey.getSignOriginalData("system", mUserPhoneNumber,
                                        firmBargain.getTransactionType(), tansactionAmountOffice);
                            } else {
                                safeOriginal = CombinationSecretKey.getSignOriginalData(firmBargain.getAppointMobile(), mUserPhoneNumber,
                                        firmBargain.getTransactionType(), tansactionAmountOffice);
                            }
                        }
                        String safeStr = mVirtualCpk.SM2Sign(safeOriginal);
                        firmBargain.setSafeStr(safeStr);
                        byte[] deRandom = Base64.decode(userBalanceResult.random);
                        byte[] random = mVirtualCpk.SM2Decrypt(deRandom);  //解密对称秘钥
                        String painText = new Gson().toJson(firmBargain);
                        String code = mVirtualCpk.EncryptData(random, painText);
                        requestCreateOrderInfo(code);
                    } else {
                        if (!MainActivity.isVisitor) {
                            Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                        }
                        //关闭虚拟设备,然后重启虚拟cos
                        mVirtualCpk.closeVirtualCos();
                        mVirtualCpk = VirtualCpk.getInstance(mActivity);
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                    //关闭虚拟设备,然后重启虚拟cos
                    mVirtualCpk.closeVirtualCos();
                    mVirtualCpk = VirtualCpk.getInstance(mActivity);
                });
    }

    /**
     * 请求创建订单  向服务器发送 加密后的创建订单信息数据
     *
     * @param code 对称加密数据
     */
    private void requestCreateOrderInfo(String code) {
        String requestCode = CombinationSecretKey.assembleJsonCode(code);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), requestCode);
        GoldenOrder goldenOrder = RetrofitUtils.create(GoldenOrder.class);
        goldenOrder.createFirmBarginSecond(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    //关闭虚拟设备,然后重启虚拟cos
                    mVirtualCpk.closeVirtualCos();
                    mVirtualCpk = VirtualCpk.getInstance(mActivity);
                    if (userBalanceResult.success) {
                        Toast.makeText(getActivity(), ("创建订单成功，订单号为：") + userBalanceResult.firmId, Toast.LENGTH_LONG).show();
                        if (issueTradeOrderFragmentDialog != null)
                            issueTradeOrderFragmentDialog.dismiss();
                        if (isPlatformOrder) {
                            hangingPlatformTradeFragment.onRefresh();
                        } else {
                            hangingPersonalTradeFragment.onRefresh();
                        }
                    } else {
                        Toast.makeText(getActivity(), "创建订单失败，" + userBalanceResult.msg, Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                    //关闭虚拟设备,然后重启虚拟cos
                    closeVirtualAndRestart();
                });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 查询用户账号状态信息
     */
    private void queryUserBalance() {
        PhoneNumber phoneNumber = new PhoneNumber(mUserPhoneNumber);
        String keyId = CombinationSecretKey.getSecretKey("userBalance.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(phoneNumber));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserBalance queryUserBalance = RetrofitUtils.create(QueryUserBalance.class);
        queryUserBalance.queryUserBalance(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userBalanceResult -> {
                    if (userBalanceResult.success) {
                        mUserBalanceData = userBalanceResult.data;
                        showUserBalance();
                    } else {
                        if (!MainActivity.isVisitor){
                            Toast.makeText(getActivity(), "查询账号状态结果失败!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 查询 金本币价格
     */
    private void queryAuthPrice() {
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(""));

        QueryAuthPrice queryAuthPrice = RetrofitUtils.create(QueryAuthPrice.class);
        queryAuthPrice.queryAuthPrice(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authPriceResult -> {
                    if (authPriceResult.success) {
                        mAuthPriceData = authPriceResult.data;
                        SharedPreferences.Editor editor = mUserSharedPre.edit();
                        editor.putString(ConstantUtils.GOLD_PRICE, "" + authPriceResult.data.officePrice);
                        editor.putString(ConstantUtils.GOLD_AVERAGE_PRICE, "" + authPriceResult.data.officePrice);
                        editor.apply();
                        showAuthPrice();
                    } else {
                        Toast.makeText(getActivity(), "查询金价失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 查询挂盘交易信息
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
    private void queryTransactionInfo(String userId, int page, int rows, int status, int transactionType, int transactionMode, String sortType, int sortInt) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.userId = userId;
        orderDetails.page = page;
        orderDetails.rows = rows;
        orderDetails.status = status;
        orderDetails.transactionType = transactionType;
        if (transactionMode == 1) {
            orderDetails.sellerId = "none";
        } else if (transactionMode == 2) {
            orderDetails.buyerId = "none";
        }
        if (sortType != null) {
            orderDetails.sorte = sortType;
            orderDetails.order = sortInt;
        } else {
            orderDetails.order = -1;
        }
        String keyId = CombinationSecretKey.getSecretKey("orderDetails.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(orderDetails));
        if (MainActivity.isVisitor) {
            hufuCode = "";
        }
        HuFuCode huFuCode = new HuFuCode(hufuCode);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(huFuCode));

        QueryTransactionOrder queryTransactionOrder = RetrofitUtils.create(QueryTransactionOrder.class);
        queryTransactionOrder.queryTransactionOrder(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryAllCertsResult -> {
                    mTransactionTypeSwitch.setVisibility(View.VISIBLE);
                    mTransSwipeRL.setRefreshing(false);
                    if (queryAllCertsResult.success) {
                        showDivider = true;
                        showRecyclerView(queryAllCertsResult.data);
                    } else {
                        if (!MainActivity.isVisitor) {
                            Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                        }
                    }
                }, error -> {
                    mTransSwipeRL.setRefreshing(false);
                    mTransactionTypeSwitch.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private TransactionItemDecoration mTransactionItem;

    private void showRecyclerView(OrderDetailsResult.Data datas) {
        divider.setVisibility(View.VISIBLE);
        if (mTransactionItem == null) mTransactionItem = new TransactionItemDecoration();
        transactionInfoRe.removeItemDecoration(mTransactionItem);
        transactionInfoRe.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionInfoRe.addItemDecoration(mTransactionItem);
        if (isPlatformOrder) {
            //加载平台信息
            transactionInfoRe.setAdapter(new TransactionAdapter(datas));
            transactionInfoRe.setHasFixedSize(true);
        } else {
            //加载私人信息
            transactionInfoRe.setAdapter(new PersonalTranAdatper(datas.getList(), mActivity));
            transactionInfoRe.setHasFixedSize(true);
        }
        totalData = datas.getTotal();
        totalPage = totalData / rows;
        int lastPageDataItem = totalData % rows;
        if (lastPageDataItem != 0) totalPage = totalPage + 1;
        infoPageText.setText("共" + totalPage + "页  " + "共" + totalData + "条记录");
        if (totalPage == 0) dividePageSpinner.setVisibility(View.GONE);
        else dividePageSpinner.setVisibility(View.VISIBLE);
        if (firstShowFragment) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                    getPageData(totalPage));
            dividePageSpinner.setAdapter(adapter);
            dividePageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    pageCurrently = i + 1;
                    if (firstShowFragment) {
                        firstShowFragment = false;
                        return;
                    }
                    if (isPlatformOrder) {
                        //查询平台订单信息
                        queryTransactionInfo(null, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
                    } else {
                        //查询私人订单信息
                        queryTransactionInfo(mUserPhoneNumber, pageCurrently, rows, status, transactionType, transactionMode, sortType, sortInt);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }

    }


    @SuppressWarnings("deprecation")
    private class TransactionItemDecoration extends RecyclerView.ItemDecoration {
        Paint dividerPaint;
        int dividerHeight;

        public TransactionItemDecoration() {
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

    private List<String> getPageData(int totalPage) {
        List<String> dataList = new ArrayList<String>();
        for (int i = 1; i < totalPage + 1; i++) {
            dataList.add("" + i);
        }
        return dataList;
    }

    //"未发放奖励额度：" + DateUtils.getStringFloat(mUserBalanceData.userInfo.totalReward - mUserBalanceData.userInfo.rewardAmount)
    private void showUserBalance() {
        String[] groupData = {"账号状态"};
        String[][] childData = {{"账户余额：" + mUserBalanceData.userInfo.balance + "金本币", "可用信用额度：" + mUserBalanceData.userInfo.creditAmount + "金本币",
                "积分账户：" + mUserBalanceData.userInfo.freezeAmount + "积分", "已发放奖励额度：" + mUserBalanceData.userInfo.rewardAmount + "金本币"
        }};
        SharedPreferences.Editor editor = mUserSharedPre.edit();
        editor.putString(ConstantUtils.USER_GOLD_NUM, String.valueOf(mUserBalanceData.userInfo.balance));
        editor.putString(ConstantUtils.USER_INTEGRAL_NUM, String.valueOf(mUserBalanceData.userInfo.freezeAmount));
        editor.apply();
    }

    private void showAuthPrice() {
        String[] groupData = {"金价排行"};
        String[][] childData = {{"国际金价：  " + mAuthPriceData.internatPrice + "元/金本币",
                "官网金价：  " + mAuthPriceData.officePrice + "元/金本币",
                "平均金价：   " + mAuthPriceData.averagePrice + "元/金本币"
        }};

        interGoldPriceTv.setText(String.format("%.2f",mAuthPriceData.internatPrice));
        officeGoldPriceTv.setText(String.format("%.2f",mAuthPriceData.officePrice));
        averageGoldPriceTv.setText(String.format("%.2f",mAuthPriceData.averagePrice));
        priceGold = "" + mAuthPriceData.averagePrice;
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionHolder> {
        OrderDetailsResult.Data datas;

        TransactionAdapter(OrderDetailsResult.Data datas) {
            this.datas = datas;
        }


        @Override
        public TransactionAdapter.TransactionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TransactionHolder(LayoutInflater.from(getActivity()).inflate(R.layout.item_transaction_info, parent, false));
        }

        @Override
        public void onBindViewHolder(TransactionAdapter.TransactionHolder holder, int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showTransactionOrderInfo(datas.getList().get(position));
                }
            });
            holder.orderNumber.setText("订单号" + datas.getList().get(position).firmId);
            String sellerId = datas.getList().get(position).sellerId;
            String buyerId = datas.getList().get(position).buyerId;
            if ("".equals(buyerId) || buyerId == null) {
                holder.initiator.setText("发起人" + DateUtils.getStringHidePhone(sellerId));
                holder.transactionType.setText("卖出");
            } else {
                holder.initiator.setText("发起人" + DateUtils.getStringHidePhone(buyerId));
                holder.transactionType.setText("买入");
            }
            holder.goldCloud.setText(DateUtils.getStringFloat(datas.getList().get(position).transactionAmount));
            holder.price.setText(DateUtils.getStringFloat(datas.getList().get(position).customPrice));
            holder.startTime.setText(datas.getList().get(position).createTime);
            holder.tadingNotes.setText(datas.getList().get(position).noteInformation);

        }

        @Override
        public int getItemCount() {
            return datas.getList().size();
        }

        class TransactionHolder extends RecyclerView.ViewHolder {

            private final TextView transactionType;
            private final TextView orderNumber;
            private final TextView initiator;
            private final TextView goldCloud;
            private final TextView price;
            private final TextView startTime;
            private final TextView tadingNotes;

            public TransactionHolder(View itemView) {
                super(itemView);
                transactionType = itemView.findViewById(R.id.transaction_type_txt);
                orderNumber = itemView.findViewById(R.id.order_number_txt);
                initiator = itemView.findViewById(R.id.initiator_txt);
                goldCloud = itemView.findViewById(R.id.gold_cloud_txt);
                price = itemView.findViewById(R.id.price_txt);
                startTime = itemView.findViewById(R.id.start_time_txt);
                tadingNotes = itemView.findViewById(R.id.tading_notes_txt);
            }
        }


    }

    private void showTransactionOrderInfo(TransactionOrderInfo transactionInfo) {
        final AlertDialog dialogPlusBuilder = new AlertDialog.Builder(mActivity).create();
        View view = View.inflate(mActivity, R.layout.dialog_transaction_order_info, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (view.getId() == R.id.revoke_tran_btn) {
                    if (isRevoke) {
                        RevokeOrderQuest mReOrder = createRevokeOrderInfo(transactionInfo, tranMode);
                        showPaymentPassword(mReOrder);
                        dialogPlusBuilder.dismiss();
                    }
                } else if (view.getId() == R.id.trade_tran_btn) {
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
                }

            }
        });
        dialogPlusBuilder.setView(view);
        dialogPlusBuilder.show();
        writeDataDialog(transactionInfo);

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
        int transactionType; //交易类型  0 点对点交易  1 平台挂盘交易
        String appiontMobile;
        if (isPlatformOrder) {
            transactionType = 1;
            appiontMobile = "system";
        } else {
            transactionType = 0;
            appiontMobile = mUserPhoneNumber;
        }
        if (transactionInfo.status == 0) { //交易状态  0 作为卖方  1 作为买方
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

    private int tranMode = -1;   //交易类型  0 买入 1 卖出  以订单信息作为参照物
    private boolean isRevoke = true;
    private int confirmtranMode = -1;   //交易类型  0 买入 1 卖出  以自己作为参照物

    private void writeDataDialog(TransactionOrderInfo transactionInfo) {
        TextView tranIssueTv = mActivity.findViewById(R.id.tran_issue_tv);
        tranIssueTv.setText("今日平均金价：" + priceGold + "元/金本币");
        TextView goldNumTv = mActivity.findViewById(R.id.gold_number_tv);
        goldNumTv.setText("订单金本币数：" + transactionInfo.transactionAmount);
        TextView goldPriceTv = mActivity.findViewById(R.id.inter_gold_price_tv);
        goldPriceTv.setText("金本币单价：" + transactionInfo.customPrice);
        TextView orderPriceTv = mActivity.findViewById(R.id.order_price_tv);
        BigDecimal tranAmountBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.transactionAmount));
        BigDecimal goldPriceBig = new BigDecimal(DateUtils.getStringFloat(transactionInfo.customPrice));
        orderPriceTv.setText("交易金额：" + tranAmountBig.multiply(goldPriceBig).floatValue());
        TextView orderNumTv = mActivity.findViewById(R.id.order_num_tv);
        orderNumTv.setText("" + transactionInfo.firmId);
        TextView orderCreateTimeTv = mActivity.findViewById(R.id.order_create_time_tv);
        orderCreateTimeTv.setText("订单创建时间：" + transactionInfo.createTime);
        TextView tranNoteTV = mActivity.findViewById(R.id.tran_note_tv);
        tranNoteTV.setText("交易备注:" + transactionInfo.noteInformation);
        TextView orderCreateTv = mActivity.findViewById(R.id.order_create_tv);
        TextView tranModeTv = mActivity.findViewById(R.id.tran_mode_tv);
        Button transactionBtn = mActivity.findViewById(R.id.trade_tran_btn);
        Button transactionRevoke = mActivity.findViewById(R.id.revoke_tran_btn);
        if (transactionInfo.sellerId == null || "".equals(transactionInfo.sellerId)) {
            orderCreateTv.setText("订单发起人：" + DateUtils.getStringHidePhone(transactionInfo.buyerId));
            tranModeTv.setText("交易类型：买入");
            tranMode = 0;
            confirmtranMode = 1;
        } else {
            orderCreateTv.setText("订单发起人：" + DateUtils.getStringHidePhone(transactionInfo.sellerId));
            tranModeTv.setText("交易类型：卖出");
            tranMode = 1;
            confirmtranMode = 0;
        }
        TextView recipientTv = mActivity.findViewById(R.id.dialog_recipient_tv);
        if (isPlatformOrder) {
            recipientTv.setVisibility(View.GONE);
        } else {
            recipientTv.setVisibility(View.VISIBLE);
            if (transactionInfo.initiatorId.equals(transactionInfo.sellerId)) {
                orderCreateTv.setText("订单发起人：" + DateUtils.getStringHidePhone(transactionInfo.initiatorId));
                recipientTv.setText("订单接受人：" + DateUtils.getStringHidePhone(transactionInfo.buyerId));
            } else {
                recipientTv.setText("订单接受人：" + DateUtils.getStringHidePhone(transactionInfo.sellerId));
                orderCreateTv.setText("订单发起人：" + DateUtils.getStringHidePhone(transactionInfo.initiatorId));
            }
        }

        if (mUserPhoneNumber.equals(transactionInfo.initiatorId)) {
            if (isPlatformOrder) {
                transactionBtn.setVisibility(View.GONE);
                transactionRevoke.setVisibility(View.VISIBLE);
            } else {
                transactionBtn.setVisibility(View.GONE);
                transactionRevoke.setVisibility(View.VISIBLE);
            }
        } else {
            if (isPlatformOrder) {
                transactionBtn.setVisibility(View.VISIBLE);
                transactionRevoke.setVisibility(View.GONE);
            } else {
                transactionBtn.setVisibility(View.VISIBLE);
                transactionRevoke.setVisibility(View.VISIBLE);
            }
        }

    }

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
                        mTransSwipeRL.setRefreshing(true);  //刷新界面
                        refreshTransactionAndUserInfo();//刷新数据
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
                        mTransSwipeRL.setRefreshing(true);  //刷新界面
                        refreshTransactionAndUserInfo();//刷新数据
                        Toast.makeText(getActivity(), "确认订单成功！", Toast.LENGTH_LONG).show();
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
}
