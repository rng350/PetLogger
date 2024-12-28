package com.hfad.petlogger.screens.sections.recyclerviews.decorators

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PhotoItemSpacingDecoration(
    private val spanCount: Int = 3,
    private val spacing: Int = (2 * Resources.getSystem().displayMetrics.density).toInt()
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position >= spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = 0
    }
}