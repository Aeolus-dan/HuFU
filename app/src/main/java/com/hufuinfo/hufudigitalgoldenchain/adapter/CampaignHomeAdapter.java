package com.hufuinfo.hufudigitalgoldenchain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.bean.CampaignInfo;

import java.util.List;

public class CampaignHomeAdapter extends RecyclerView.Adapter<CampaignHomeAdapter.CampainHomeHolder> {
    private Context mContext;
    private List<CampaignInfo> campaignInfos;

    public CampaignHomeAdapter(Context mContext, List<CampaignInfo> campaignInfos) {
        this.mContext = mContext;
        this.campaignInfos = campaignInfos;
    }

    @NonNull
    @Override
    public CampainHomeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new CampainHomeHolder(LayoutInflater.from(mContext).inflate(R.layout.item_campaign_info, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CampainHomeHolder campainHomeHolder, int i) {
        campainHomeHolder.titleTv.setText(campaignInfos.get(i).getTitle());
        campainHomeHolder.timeTv.setText(campaignInfos.get(i).getTime());
    }

    @Override
    public int getItemCount() {
        return campaignInfos.size();
    }

    public class CampainHomeHolder extends RecyclerView.ViewHolder {
        private final TextView titleTv;
        private final TextView timeTv;

        public CampainHomeHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.title_tv);
            timeTv = itemView.findViewById(R.id.time_tv);
        }
    }
}
