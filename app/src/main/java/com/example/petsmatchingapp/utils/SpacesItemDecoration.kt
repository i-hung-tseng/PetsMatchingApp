package com.example.petsmatchingapp.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private var space_right_left: Int?,private var space_top_bottom: Int?): RecyclerView.ItemDecoration() {


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        space_top_bottom?.let {
            outRect.top = it
            outRect.bottom = it
        }

        space_right_left?.let {
            outRect.right = it
            outRect.left = it
        }
    }
}