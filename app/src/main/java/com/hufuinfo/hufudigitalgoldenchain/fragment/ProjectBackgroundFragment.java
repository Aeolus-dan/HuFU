package com.hufuinfo.hufudigitalgoldenchain.fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hufuinfo.hufudigitalgoldenchain.R;
import com.hufuinfo.hufudigitalgoldenchain.utils.BitmapUtils;
import com.hufuinfo.hufudigitalgoldenchain.utils.DisplayUtil;

public class ProjectBackgroundFragment extends Fragment {


    public ProjectBackgroundFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_background, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
      //  Bitmap background3 = dealWithBitmap(R.drawable.background3);
        ImageView background3Vi = view.findViewById(R.id.project_background3_Iv);
        //background3Vi.setImageBitmap(background3);
    }

    private Bitmap dealWithBitmap(int ResourceId) {
        int width = DisplayUtil.getScreenWidth(getActivity());
        int height = 256;
        Drawable drawable =getActivity().getResources().getDrawable(R.drawable.background3);//ContextCompat.getDrawable(getActivity(), R.drawable.background3);
        Bitmap sourceBitmap = BitmapUtils.drawableToBitmap(drawable);
        //BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.background3);
        return BitmapUtils.scaleMatrix(sourceBitmap, width, height);
        // return ThumbnailUtils.extractThumbnail(sourceBitmap, width, height);
    }
}
