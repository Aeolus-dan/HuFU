package com.hufuinfo.hufudigitalgoldenchain.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.apiinterface.QueryUserInfo;
import com.hufuinfo.hufudigitalgoldenchain.bean.HuFuCode;
import com.hufuinfo.hufudigitalgoldenchain.bean.RechargeRecord;
import com.hufuinfo.hufudigitalgoldenchain.bean.RechargeRecordResult;
import com.hufuinfo.hufudigitalgoldenchain.utils.CombinationSecretKey;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.RetrofitUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.VirtualCpk;
import com.hufuinfo.hufudigitalgoldenchain.widget.SoftRadioButton;
import com.hufuinfo.hufudigitalgoldenchain.widget.SoftRadioGroup;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class TotalBillInfoFragment extends Fragment implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_PARAM1 = "BILL_TYPE";
    /**
     * "全部账单"  0
     * "金本币账户账单"  1
     * "积分账户账单", 2
     * "信用额度账单",  3
     */
    private int billType;

    private Activity mActivity;

    private SwipeRefreshLayout totalBillInofSrl;
    private RecyclerView totalBillInofRv;
    private SoftRadioButton totalBillAmountSrb;
    private SoftRadioButton totalBillTimeSrb;
    private SoftRadioGroup totalTypeSrg;

    private String antiFake;
    private VirtualCpk mVirtualCpk;
    private SharedPreferences mUserSharedPre;
    private String userMobile;

    private RechargeRecordAdapter mRechargeRecordAdapter;

    private List<RechargeRecordResult.UserVoucherCenter> userVoucherCenterLists = new ArrayList<>();

    private int pageCurrently = 1;
    //每页的行数
    private int rows = 10;

    /**
     * 0：时间倒序，1：时间正序，2交易金额倒序，3：交易金额正序
     */
    private int sortInt = 0;

    private View notDataView;
    private View errorView;

    public TotalBillInfoFragment() {
        // Required empty public constructor
    }

    public static TotalBillInfoFragment newInstance(int billType) {
        TotalBillInfoFragment fragment = new TotalBillInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, billType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            billType = getArguments().getInt(ARG_PARAM1);
        }

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
        View view = inflater.inflate(R.layout.fragment_total_bill_info, container, false);
        initView(view);
        onLoadFirstData(billType, sortInt);
        return view;
    }

    private void initView(View view) {

        totalBillAmountSrb = view.findViewById(R.id.trade_amount_srb);
        totalBillTimeSrb = view.findViewById(R.id.trade_time_srb);
        totalTypeSrg = view.findViewById(R.id.total_bill_srg);
        totalBillTimeSrb.setChecked(true, false);
        totalTypeSrg.setOnCheckedChangeListener(new SoftRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SoftRadioGroup group, int checkedId, boolean orientation) {
                if (checkedId == R.id.trade_amount_srb) {
                    if (orientation) {
                        sortInt = 3;
                    } else {
                        sortInt = 2;
                    }
                } else if (checkedId == R.id.trade_time_srb) {
                    if (orientation) {
                        sortInt = 1;
                    } else {
                        sortInt = 0;
                    }
                }
                onRefresh();
            }
        });
        totalBillInofSrl = view.findViewById(R.id.total_bill_info_srl);
        totalBillInofSrl.setOnRefreshListener(this::onRefresh);
        totalBillInofRv = view.findViewById(R.id.total_bill_info_rv);
        totalBillInofRv.setLayoutManager(new LinearLayoutManager(mActivity));
        mRechargeRecordAdapter = new RechargeRecordAdapter(userVoucherCenterLists);
        totalBillInofRv.setAdapter(mRechargeRecordAdapter);
        //   mRechargeRecordAdapter.setEmptyView(getView());
        mRechargeRecordAdapter.setOnLoadMoreListener(this::onLoadMoreRequested, totalBillInofRv);
        totalBillInofRv.addItemDecoration(new RechargeRecordItem());
        totalBillInofRv.setHasFixedSize(true);

        notDataView = getLayoutInflater().inflate(R.layout.empty_view, (ViewGroup) totalBillInofRv.getParent(), false);
        notDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
            }
        });
        errorView = getLayoutInflater().inflate(R.layout.error_view, (ViewGroup) totalBillInofRv.getParent(), false);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefresh();
            }
        });
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
     * 查询 全部账单
     *
     * @param mobile    手机号码
     * @param page      页面
     * @param rows      行数
     * @param vouchSort 排序  0：时间倒序，1：时间正序，2交易金额倒序，3：交易金额正序
     */
    private void queryAllRecordInfo(String mobile, int page, int rows, int vouchSort,
                                    Observer<List<RechargeRecordResult.UserVoucherCenter>> observer) {
        RechargeRecord rechargeRecord = new RechargeRecord(mobile, page, rows);
        rechargeRecord.setVouchSort(vouchSort);

        String keyId = CombinationSecretKey.getSecretKey("rechargeRecord.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(rechargeRecord));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        queryUserInfo.queryAllRecordInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryRechargeResult -> {
                    totalBillInofSrl.setRefreshing(false);
                    if (queryRechargeResult.success) {
                        observer.onNext(queryRechargeResult.data.getUserVoucherCenter());
                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    totalBillInofSrl.setRefreshing(false);
                    mRechargeRecordAdapter.setEmptyView(errorView);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 查询人民币账户记录
     *
     * @param mobile
     * @param page
     * @param rows
     */
    private void queryCnyRecordInfo(String mobile, int page, int rows, int vouchSort,
                                    Observer<List<RechargeRecordResult.UserVoucherCenter>> observer) {
        RechargeRecord rechargeRecord = new RechargeRecord(mobile, page, rows);
        rechargeRecord.setVouchSort(vouchSort);
        String keyId = CombinationSecretKey.getSecretKey("rechargeRecord.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(rechargeRecord));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        queryUserInfo.queryCnyRecordInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryRechargeResult -> {
                    totalBillInofSrl.setRefreshing(false);
                    if (queryRechargeResult.success) {
                        observer.onNext(queryRechargeResult.data.getUserVoucherCenter());
                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    totalBillInofSrl.setRefreshing(false);
                    mRechargeRecordAdapter.setEmptyView(errorView);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 查询信用额度账单记录
     *
     * @param mobile
     * @param page
     * @param rows
     */
    private void queryCreRecordInfo(String mobile, int page, int rows, int vouchSort,
                                    Observer<List<RechargeRecordResult.UserVoucherCenter>> observer) {
        RechargeRecord rechargeRecord = new RechargeRecord(mobile, page, rows);
        rechargeRecord.setVouchSort(vouchSort);
        String keyId = CombinationSecretKey.getSecretKey("rechargeRecord.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(rechargeRecord));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        queryUserInfo.queryCreRecordInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryRechargeResult -> {
                    totalBillInofSrl.setRefreshing(false);
                    if (queryRechargeResult.success) {
                        observer.onNext(queryRechargeResult.data.getUserVoucherCenter());

                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    totalBillInofSrl.setRefreshing(false);
                    mRechargeRecordAdapter.setEmptyView(errorView);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 查询金本币账户账单记录
     *
     * @param mobile
     * @param page
     * @param rows
     */
    private void queryBalRecordInfo(String mobile, int page, int rows, int vouchSort,
                                    Observer<List<RechargeRecordResult.UserVoucherCenter>> observer) {
        RechargeRecord rechargeRecord = new RechargeRecord(mobile, page, rows);
        rechargeRecord.setVouchSort(vouchSort);

        String keyId = CombinationSecretKey.getSecretKey("rechargeRecord.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(rechargeRecord));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        queryUserInfo.queryBalRecordInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryRechargeResult -> {
                    totalBillInofSrl.setRefreshing(false);
                    if (queryRechargeResult.success) {
                        totalBillInofSrl.setRefreshing(false);
                        observer.onNext(queryRechargeResult.data.getUserVoucherCenter());
                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    totalBillInofSrl.setRefreshing(false);
                    mRechargeRecordAdapter.setEmptyView(errorView);
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRefresh() {
        pageCurrently = 1;
        mRechargeRecordAdapter.setEmptyView(R.layout.loading_view, (ViewGroup) totalBillInofRv.getParent());
        Observer<List<RechargeRecordResult.UserVoucherCenter>> observer = new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                if (value == null) return;
                if (!mRechargeRecordAdapter.isLoading()) {
                }
                mRechargeRecordAdapter.getData().clear();
                mRechargeRecordAdapter.setNewData(value);
                if (value.size() < 10) {
                    mRechargeRecordAdapter.loadMoreEnd();
                }
                mRechargeRecordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
            }
        };
        switch (billType) {
            case 0:
                queryAllRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 1:
                queryBalRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 2:
                queryCnyRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 3:
                queryCreRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
        }
    }

    @Override
    public void onLoadMoreRequested() {
        pageCurrently++;
        totalBillInofSrl.setRefreshing(true);
        Observer<List<RechargeRecordResult.UserVoucherCenter>> observer = new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                if ((value != null && value.isEmpty())) {
                    mRechargeRecordAdapter.addData(value);
                    mRechargeRecordAdapter.loadMoreEnd();
                } else {
                    // Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                    mRechargeRecordAdapter.addData(value);
                    mRechargeRecordAdapter.loadMoreComplete();
                }
                mRechargeRecordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };

        switch (billType) {
            case 0:
                queryAllRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 1:
                queryBalRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 2:
                queryCnyRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 3:
                queryCreRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
        }
    }


    public void onLoadFirstData(int billType, int sortInt) {
        pageCurrently = 1;

        Observer<List<RechargeRecordResult.UserVoucherCenter>> observer = new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                if (value == null) return;
                mRechargeRecordAdapter.addData(value);
                if (value.size() < 10) {
                    mRechargeRecordAdapter.loadMoreEnd();
                }

                mRechargeRecordAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
        switch (billType) {
            case 0:
                queryAllRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 1:
                queryBalRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 2:
                queryCnyRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
            case 3:
                queryCreRecordInfo(userMobile, pageCurrently, rows, sortInt, observer);
                break;
        }
    }

    private class RechargeRecordAdapter extends BaseQuickAdapter<RechargeRecordResult.UserVoucherCenter, RechargeRecordAdapter.RechargeRecordHolder> {

        public RechargeRecordAdapter(List<RechargeRecordResult.UserVoucherCenter> datas) {
            super(R.layout.item_all_record_info, datas);
        }

        private String getRechargeMode(int status) {
            String rechargeMode = "";
            switch (status) {

                case 10:
                    rechargeMode = "充值发放奖励额度";
                    break;
                case 11:
                    rechargeMode = "积分账户（充值）";
                    break;
                case 12:
                    rechargeMode = "积分账户（充值）";
                    break;
                case 13:
                    rechargeMode = "信用额度（充值）";
                    break;
                case 20:
                    rechargeMode = "发放奖励（交易）";
                    break;
                case 21:
                    rechargeMode = "金本币账户（交易）";
                    break;
                case 22:
                    rechargeMode = "积分账户（交易）";
                    break;
                case 23:
                    rechargeMode = "信用额度（交易）";
                    break;
                case 30:
                    rechargeMode = "发放奖励（转化）";
                    break;
                case 31:
                    rechargeMode = "金本币账户（取消交易）";
                    break;
                case 32:
                    rechargeMode = "积分账户（取消交易）";
                    break;
                case 33:
                    rechargeMode = "信用额度（取消交易）";
                    break;
                case 41:
                    rechargeMode = "金本币账户（提现）";
                    break;
                case 42:
                    rechargeMode = "积分账户（提现）";
                    break;
                case 51:
                    rechargeMode = "本金折损（提现）";
                    break;
                case 52:
                    rechargeMode = "积分账户（利息日结）";
                    break;
                case 61:
                    rechargeMode = "手续费（提现）";
                    break;
                case 71:
                    rechargeMode = "金本币账户（转化）";
                    break;
                case 72:
                    rechargeMode = "积分账户（转化）";
                    break;
                case 81:
                    rechargeMode = "积分账户提现手续费支出";
                    break;
            }
            return rechargeMode;
        }




        @Override
        protected void convert(RechargeRecordHolder helper, RechargeRecordResult.UserVoucherCenter item) {

            String transactionType = String.valueOf(item.voucherAccount);
            helper.accountTv.setText(transactionType);
            helper.tranNum.setText(String.valueOf(item.goldNo));
            helper.originalNumTv.setText(String.valueOf(item.beforeBalance));
            int status = item.status;
            helper.rechargeModeTv.setText("类型 " + getRechargeMode(status));
            helper.rechargeTimeTv.setText(item.voucherTime);
        }

        class RechargeRecordHolder extends BaseViewHolder {

            private final TextView accountTv;
            private final TextView tranNum;
            private final TextView originalNumTv;
            private final TextView rechargeModeTv;
            private final TextView rechargeTimeTv;

            public RechargeRecordHolder(View itemView) {
                super(itemView);
                accountTv = itemView.findViewById(R.id.account_tv);
                tranNum = itemView.findViewById(R.id.tran_num_tv);
                originalNumTv = itemView.findViewById(R.id.original_money_tv);
                rechargeModeTv = itemView.findViewById(R.id.recharge_mode_tv);
                rechargeTimeTv = itemView.findViewById(R.id.recharge_time_tv);
            }
        }
    }


    @SuppressWarnings("deprecation")
    private class RechargeRecordItem extends RecyclerView.ItemDecoration {
        Paint dividerPaint;
        int dividerHeight;

        public RechargeRecordItem() {
            //paint
            dividerPaint = new Paint();
            dividerPaint.setColor(getResources().getColor(R.color.dividerAllRecordItemColor));
            dividerHeight = getResources().getDimensionPixelSize(R.dimen.divider_all_record);
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
}
