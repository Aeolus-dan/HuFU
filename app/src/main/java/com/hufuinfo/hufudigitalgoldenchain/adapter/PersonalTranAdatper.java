package com.hufuinfo.hufudigitalgoldenchain.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.bean.TransactionOrderInfo;
import com.hufuinfo.hufudigitalgoldenchain.utils.ConstantUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DateUtils;

import java.util.List;

public class PersonalTranAdatper extends BaseQuickAdapter<TransactionOrderInfo, PersonalTranAdatper.PersonalHolder> {
    private String mUserAccount;

    public PersonalTranAdatper(List<TransactionOrderInfo> datas, Context context) {
        super(R.layout.item_platform_transaction_info, datas);
        this.mContext = context;
        mUserAccount = context.getSharedPreferences(ConstantUtils.USER_INFO_STORAGE, Context.MODE_PRIVATE).getString(ConstantUtils.USER_ACCOUNT, null);
    }

    @Override
    protected void convert(PersonalHolder helper, TransactionOrderInfo item) {
        helper.orderNumber.setText("订单号 " + item.firmId);
        String sellerId = item.sellerId;
        String buyerId = item.buyerId;
        String initiatorAccount = item.initiatorId;
        helper.initiator.setText("发起人 " + DateUtils.getStringHidePhone(initiatorAccount));
        if (initiatorAccount.equals(buyerId)) {
            helper.transactionType.setText("买入");
            helper.traderTv.setText("接收人 " + DateUtils.getStringHidePhone(sellerId));
        } else {
            helper.transactionType.setText("卖出");
            helper.traderTv.setText("接收人 " + DateUtils.getStringHidePhone(buyerId));
        }

        helper.goldCloud.setText(DateUtils.getStringFloat(item.transactionAmount));
        helper.price.setText(DateUtils.getStringFloat(item.customPrice));
        if (item.status == 0)
            helper.startTime.setText(item.createTime);
        else
            helper.startTime.setText(item.transactionDate);
        helper.tadingNotes.setText(item.noteInformation);

    }

    class PersonalHolder extends BaseViewHolder {

        private final TextView transactionType;
        private final TextView orderNumber;
        private final TextView initiator;
        private final TextView traderTv;
        private final TextView goldCloud;
        private final TextView price;
        private final TextView startTime;
        private final TextView tadingNotes;

        PersonalHolder(View itemView) {
            super(itemView);
            transactionType = itemView.findViewById(R.id.transaction_type_txt);
            orderNumber = itemView.findViewById(R.id.order_number_txt);
            initiator = itemView.findViewById(R.id.initiator_txt);
            traderTv = itemView.findViewById(R.id.trader_txt);
            goldCloud = itemView.findViewById(R.id.gold_cloud_txt);
            price = itemView.findViewById(R.id.price_txt);
            startTime = itemView.findViewById(R.id.start_time_txt);
            tadingNotes = itemView.findViewById(R.id.tading_notes_txt);
        }
    }

}
