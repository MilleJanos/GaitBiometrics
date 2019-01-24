package com.example.jancsi_pc.playingwithsensors.ListUserStats

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.example.jancsi_pc.playingwithsensors.R

/**
 *  This is a class that is meant to draw a horizontal line divider for a RecyclerView with the
 *  characteristics given in the line_divider.xml drawable
 *
 *  @param context the application context
 */
class ItemDividerDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val mDivider: Drawable = ContextCompat.getDrawable(context, R.drawable.line_divider)!!

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
}