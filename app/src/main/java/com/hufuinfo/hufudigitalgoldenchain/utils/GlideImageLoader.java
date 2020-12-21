package com.hufuinfo.hufudigitalgoldenchain.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.youth.banner.loader.ImageLoader;

public class GlideImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        //Glide 加载图片简单用法
        RequestOptions options = new RequestOptions()
                .fitCenter()
                .centerCrop();
        Glide
                .with(context)
                .load(path)
                .apply(options)
                .into(imageView);

        //Picasso 加载图片简单用法
        // Picasso.with(context).load(path).into(imageView);

        //用fresco加载图片简单用法，记得要写下面的createImageView方法
      /*  Uri uri = Uri.parse((String) path);
        imageView.setImageURI(uri);*/
    }

}
