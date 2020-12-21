package com.hufuinfo.hufudigitalgoldenchain.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.bean.OrderDetailsResult;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;

import java.util.List;

public class PlatformTradeFinishAdapter extends BaseQuickAdapter<TransactionOrderInfo, PlatformTradeFinishAdapter.PlatformTradeFinishHolder> {

    private String userAccount;

    public PlatformTradeFinishAdapter(List<TransactionOrderInfo> datas, String userAccount) {
        super(R.layout.platform_trade_finish_item, datas);
        this.userAccount = userAccount;
    }


    @Override
    protected void convert(PlatformTradeFinishHolder helper, TransactionOrderInfo item) {
        helper.orderNumber.setText(item.firmId);
        String sellerId = item.sellerId;
        String buyerId = item.buyerId;
        String initiatorId = item.initiatorId;
        helper.initiator.setText("发起人 " + DateUtils.getStringHidePhone(initiatorId));
        if (initiatorId.equals(sellerId)) {
            helper.traderTv.setText("交易人 " + DateUtils.getStringHidePhone(buyerId));

        } else {
            helper.traderTv.setText("交易人 " + DateUtils.getStringHidePhone(sellerId));

        }
        if (userAccount.equals(sellerId)) {
            helper.transactionType.setText("卖出");

        } else {
            helper.transactionType.setText("买入");
        }
        helper.goldCloud.setText(String.format("%.2f",item.transactionAmount));
        helper.price.setText(String.format("%.2f",item.customPrice));
        helper.startTime.setText(item.transactionDate);
    }


    class PlatformTradeFinishHolder extends BaseViewHolder {

        private final TextView transactionType;
        private final TextView orderNumber;
        private final TextView initiator;
        private final TextView goldCloud;
        private final TextView price;
        private final TextView startTime;
        private final TextView traderTv;

        public PlatformTradeFinishHolder(View itemView) {
            super(itemView);

            transactionType = itemView.findViewById(R.id.transaction_type_txt);
            orderNumber = itemView.findViewById(R.id.order_number_txt);
            initiator = itemView.findViewById(R.id.initiator_txt);
            traderTv = itemView.findViewById(R.id.trader_txt);
            goldCloud = itemView.findViewById(R.id.gold_cloud_txt);
            price = itemView.findViewById(R.id.price_txt);
            startTime = itemView.findViewById(R.id.start_time_txt);
        }
    }
}
