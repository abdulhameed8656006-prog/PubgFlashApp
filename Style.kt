package com.abdulhameed.pubgflash

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button

object Style {
    val BG_TOP = 0xFF0B5CFF.toInt()
    val BG_BOTTOM = 0xFF062678.toInt()
    val GOLD_TOP = 0xFFFFF176.toInt()
    val GOLD_BOTTOM = 0xFFFFB300.toInt()
    val CARD = 0x33FFFFFF.toInt()
    val TEXT_DIM = 0xB3FFFFFF.toInt()
    val NAVY = 0xFF0A2A7A.toInt()

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun background(): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(BG_TOP, BG_BOTTOM))

    fun card(radius: Float): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(CARD)
        g.cornerRadius = radius
        return g
    }

    fun goldFill(radius: Float): GradientDrawable {
        val g = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(GOLD_TOP, GOLD_BOTTOM))
        g.cornerRadius = radius
        return g
    }

    fun button(c: Context, text: String, primary: Boolean, onClick: () -> Unit): Button {
        val b = Button(c)
        b.text = text
        b.isAllCaps = false
        b.textSize = 17f
        b.typeface = Typeface.DEFAULT_BOLD
        b.stateListAnimator = null
        b.minHeight = dp(c, 54)
        val r = dp(c, 16).toFloat()
        if (primary) {
            b.setTextColor(NAVY)
            b.background = goldFill(r)
        } else {
            b.setTextColor(Color.WHITE)
            b.background = card(r)
        }
        b.setOnClickListener { onClick() }
        return b
    }
}
