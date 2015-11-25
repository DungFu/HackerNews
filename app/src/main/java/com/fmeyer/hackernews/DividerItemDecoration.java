package com.fmeyer.hackernews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public DividerItemDecoration(Context context) {
        mDivider = context.getResources().getDrawable(R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int right = parent.getWidth();
        int dividerHeight = mDivider.getIntrinsicHeight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            View nextChild = parent.getChildAt(i + 1);

            RecyclerView.LayoutParams layoutParams1 =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            RecyclerView.LayoutParams layoutParams2 =
                    (RecyclerView.LayoutParams) nextChild.getLayoutParams();
            int left = 0;
            if (layoutParams1 != null && layoutParams2 != null) {
                left = Math.min(layoutParams1.leftMargin, layoutParams2.leftMargin);
            }

            int ty = (int) (child.getTranslationY() + 0.5f);
            int top = child.getBottom() + ty;
            int bottom = top + dividerHeight;

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
