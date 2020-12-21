package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RechargeRecordFragment extends Fragment implements BaseQuickAdapter.RequestLoadMoreListener {
    private final static String THIS_FILE = "RechargeRecordFragment";
    private Activity mActivity;

    private RecyclerView transactionInfoRe;
    private String userMobile;

    // 分页参数
    //每页的行数
    private int rows = 10;
    //当前页面
    private int pageCurrently = 1;


    private RechargeRecordAdapter rechargeRecordAdapter;
    private List<RechargeRecordResult.UserVoucherCenter> userVoucherCenters = new ArrayList<>();

    private SwipeRefreshLayout rechargeSwipeRefreshL;

    private VirtualCpk mVirtualCpk;
    private SharedPreferences mUserSharedPre;
    private String antiFake;

    public RechargeRecordFragment() {
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
        View view = inflater.inflate(R.layout.fragment_recharge_record, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        transactionInfoRe.setLayoutManager(new LinearLayoutManager(getActivity()));
        rechargeRecordAdapter = new RechargeRecordAdapter(R.layout.item_recharge_info, userVoucherCenters);
        rechargeRecordAdapter.setOnLoadMoreListener(this, transactionInfoRe);
        transactionInfoRe.setAdapter(rechargeRecordAdapter);
        transactionInfoRe.addItemDecoration(new RechargeRecordItem());
        transactionInfoRe.setHasFixedSize(true);
        onLoadFirstData();
    }

    private void initView(View view) {
        transactionInfoRe = view.findViewById(R.id.transaction_order_info_re);
        rechargeSwipeRefreshL = view.findViewById(R.id.recharge_swipe_refresh);
        rechargeSwipeRefreshL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCurrently = 1;
                queryRechargeRecordInfo(userMobile, pageCurrently, rows, new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                        if (value == null) return;
                        if (!rechargeRecordAdapter.isLoading()) {
                        }
                        rechargeRecordAdapter.getData().clear();
                        rechargeRecordAdapter.setNewData(value);
                        if (value.size() < 10) {
                            rechargeRecordAdapter.loadMoreEnd();
                        }
                        rechargeRecordAdapter.notifyDataSetChanged();
                        rechargeSwipeRefreshL.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
        view.findViewById(R.id.recharge_record_return_tv).setOnClickListener(listener ->
                mActivity.onBackPressed());
    }

    private void queryRechargeRecordInfo(String mobile, int page, int rows, Observer<List<RechargeRecordResult.UserVoucherCenter>> observer) {
        RechargeRecord rechargeRecord = new RechargeRecord(mobile, page, rows);
        rechargeRecord.setVouchSort(1);

        String keyId = CombinationSecretKey.getSecretKey("rechargeRecord.do");
        String hufuCode = mVirtualCpk.EncryptData(keyId.getBytes(), new Gson().toJson(rechargeRecord));
        HuFuCode mHuFoCode = new HuFuCode(hufuCode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new Gson().toJson(mHuFoCode));

        QueryUserInfo queryUserInfo = RetrofitUtils.create(QueryUserInfo.class);
        queryUserInfo.queryUserRechargeInfo(antiFake, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(queryRechargeResult -> {
                    if (queryRechargeResult.success) {
                        observer.onNext(queryRechargeResult.data.getUserVoucherCenter());
                    } else {
                        Toast.makeText(getActivity(), "查询结果失败", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    Toast.makeText(getActivity(), R.string.net_error + error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onLoadMoreRequested() {
        pageCurrently++;
        queryRechargeRecordInfo(userMobile, pageCurrently, 10, new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                if ((value != null && value.isEmpty())) {
                    Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                    rechargeRecordAdapter.addData(value);
                    rechargeRecordAdapter.loadMoreEnd();
                } else {
                    // Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
                    rechargeRecordAdapter.addData(value);
                    rechargeRecordAdapter.loadMoreComplete();
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


    public void onLoadFirstData() {
        pageCurrently = 1;
        rechargeSwipeRefreshL.setRefreshing(true);
        queryRechargeRecordInfo(userMobile, pageCurrently, rows, new Observer<List<RechargeRecordResult.UserVoucherCenter>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<RechargeRecordResult.UserVoucherCenter> value) {
                rechargeSwipeRefreshL.setRefreshing(false);
                if (value == null) return;
                rechargeRecordAdapter.addData(value);
                if (value.size() < 10) {
                    rechargeRecordAdapter.loadMoreEnd();
                }

                rechargeRecordAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private class RechargeRecordAdapter extends BaseQuickAdapter<RechargeRecordResult.UserVoucherCenter, RechargeRecordAdapter.RechargeRecordHolder> {
        RechargeRecordAdapter(int layoutResId, List<RechargeRecordResult.UserVoucherCenter> datas) {
            super(layoutResId, datas);
        }

        @Override
        protected void convert(RechargeRecordHolder helper, RechargeRecordResult.UserVoucherCenter item) {
            String transactionType = String.valueOf(item.voucherAccount);
            helper.accountTv.setText(transactionType);
            helper.goldNumberTv.setText(String.valueOf(item.goldNo));
            int status = item.status;
            helper.rechargeModeTv.setText(getRechargeMode(status));
            helper.rechargeTimeTv.setText(item.voucherTime);
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

        class RechargeRecordHolder extends BaseViewHolder {

            private final TextView accountTv;
            private final TextView goldNumberTv;
            private final TextView rechargeModeTv;
            private final TextView rechargeTimeTv;

            public RechargeRecordHolder(View itemView) {
                super(itemView);
                accountTv = itemView.findViewById(R.id.account_tv);
                goldNumberTv = itemView.findViewById(R.id.gold_cloud_tv);
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
}
