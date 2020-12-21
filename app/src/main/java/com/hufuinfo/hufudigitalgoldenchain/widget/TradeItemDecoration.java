package com.hufuinfo.hufudigitalgoldenchain.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hufuinfo.hufudigitalgoldenchain.R;

public class TradeItemDecoration extends RecyclerView.ItemDecoration {
    Paint dividerPaint;
    int dividerHeight;

    public TradeItemDecoration(Context mContext) {
        //paint
        dividerPaint = new Paint();
        dividerPaint.setColor(mContext.getResources().getColor(R.color.dividertradeDecoration));
        dividerHeight = mContext.getResources().getDimensionPixelSize(R.dimen.divider_trade_item);
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
