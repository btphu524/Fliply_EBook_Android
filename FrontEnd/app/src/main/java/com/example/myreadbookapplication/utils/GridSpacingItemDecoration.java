package com.example.myreadbookapplication.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adds symmetrical spacing for grid items.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int innerSpacing;
    private final int edgeSpacing;
    private final int verticalSpacing;

    public GridSpacingItemDecoration(int spanCount, int innerSpacing, int edgeSpacing, int verticalSpacing) {
        this.spanCount = Math.max(1, spanCount);
        this.innerSpacing = innerSpacing;
        this.edgeSpacing = edgeSpacing;
        this.verticalSpacing = verticalSpacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        int column = position % spanCount;

        // Horizontal spacing
        if (column == 0) {
            outRect.left = edgeSpacing;
            outRect.right = innerSpacing / 2;
        } else if (column == spanCount - 1) {
            outRect.left = innerSpacing / 2;
            outRect.right = edgeSpacing;
        } else {
            outRect.left = innerSpacing / 2;
            outRect.right = innerSpacing / 2;
        }

        // Vertical spacing
        if (position < spanCount) {
            outRect.top = verticalSpacing;
        } else {
            outRect.top = verticalSpacing / 2;
        }
        outRect.bottom = verticalSpacing / 2;
    }
}

