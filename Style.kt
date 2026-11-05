package com.abdulhameed.pubgflash

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button

// Dark neon theme: black/deep-purple background, cyan + purple neon accents.
// GOLD_TOP / GOLD_BOTTOM are the two accent colors (cyan -> purple); the names are kept
// so the other files do not need to change.
object Style {
    val BG_TOP = 0xFF05060F.toInt()
    val BG_BOTTOM = 0xFF1B0B3B.toInt()
    val GOLD_TOP = 0xFF00E5FF.toInt()
    val GOLD_BOTTOM = 0xFFB44CFF.toInt()
    val CARD = 0x1AFFFFFF.toInt()
    val CARD_BORDER = 0x6600E5FF.toInt()
    val TEXT_DIM = 0xB3FFFFFF.toInt()
    val NAVY = 0xFF05060F.toInt()

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun background(): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(BG_TOP, BG_BOTTOM))

    fun card(radius: Float): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(CARD)
        g.setStroke(2, CARD_BORDER)
        g.cornerRadius = radius
        return g
    }

    fun goldFill(radius: Float): GradientDrawable {
        val g = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(GOLD_TOP, GOLD_BOTTOM))
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
