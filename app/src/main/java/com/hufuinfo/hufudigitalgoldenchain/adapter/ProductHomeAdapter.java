package com.hufuinfo.hufudigitalgoldenchain.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.bean.ProductInfo;

import java.util.List;

public class ProductHomeAdapter extends RecyclerView.Adapter<ProductHomeAdapter.ProductHoldView> {

    private Context mContext;
    private List<ProductInfo> mProductInfos;

    public ProductHomeAdapter(Context context, List<ProductInfo> productInfos) {
        this.mContext = context;
        this.mProductInfos = productInfos;
    }


    @NonNull
    @Override
    public ProductHoldView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ProductHoldView(LayoutInflater.from(mContext).
                inflate(R.layout.item_proudct_home, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHoldView productHoldView, int i) {
        productHoldView.productTv.setText(mProductInfos.get(i).getProductName());
        RequestOptions mOptions = new RequestOptions().fitCenter();
        Glide.with(mContext)
                .load(mProductInfos.get(i).getImageUrl())
                .apply(mOptions)
                .into(productHoldView.productIv);
    }

    @Override
    public int getItemCount() {
        return mProductInfos.size();
    }

    public class ProductHoldView extends RecyclerView.ViewHolder {
        private final TextView productTv;
        private final ImageView productIv;

        public ProductHoldView(View itemView) {
            super(itemView);
            productTv = itemView.findViewById(R.id.product_tv);
            productIv = itemView.findViewById(R.id.product_image_view);
        }
    }
}
