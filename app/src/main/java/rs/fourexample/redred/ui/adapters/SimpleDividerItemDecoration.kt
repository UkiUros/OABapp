package rs.fourexample.redred.ui.adapters

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import rs.fourexample.redred.R

class SimpleDividerItemDecoration(context: Context, showDividerAfterLast: Boolean) :
    ItemDecoration() {
    private val mDivider: Drawable?
    private val sideMargin: Int
    private var showDividerAfterLast = true
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + sideMargin
        val right = parent.width - parent.paddingRight - sideMargin
        var childCount = parent.childCount
        if (!showDividerAfterLast) {
            childCount -= 1
        }
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.item_settings_line_divider)
        sideMargin = context.resources.getDimensionPixelSize(R.dimen.item_divider_margin)
        this.showDividerAfterLast = showDividerAfterLast
    }
}