package com.abdulhameed.pubgflash

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class AlarmActivity : Activity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val h = Handler(Looper.getMainLooper())

    private val speakLoop = object : Runnable {
        override fun run() {
            tts?.speak("PUBG Flash is out", TextToSpeech.QUEUE_FLUSH, null, "flash")
            h.postDelayed(this, 6000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        val density = resources.displayMetrics.density
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(0xFF1A0808.toInt())
        root.setPadding((24 * density).toInt(), 0, (24 * density).toInt(), 0)

        val t = TextView(this)
        t.text = "PUBG Flash is out!"
        t.textSize = 32f
        t.typeface = Typeface.DEFAULT_BOLD
        t.setTextColor(0xFFFF6B6B.toInt())
        t.gravity = Gravity.CENTER
        root.addView(t)

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (60 * density).toInt())
        lp.setMargins(0, (28 * density).toInt(), 0, 0)

        root.addView(makeButton("Stop alarm", 0xFFFF4D4D.toInt(), 0xFFFFFFFF.toInt(), density) {
            Store.ack(this)
            finish()
        }, lp)
        root.addView(makeButton("Open Google Play", 0xFFFFB400.toInt(), 0xFF111111.toInt(), density) {
            Store.ack(this)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Store.PLAY_URL)))
            finish()
        }, lp)
        setContentView(root)
        tts = TextToSpeech(this, this)
    }

    private fun makeButton(text: String, bg: Int, fg: Int, density: Float, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.textSize = 18f
        b.setTextColor(fg)
        val g = GradientDrawable()
        g.setColor(bg)
        g.cornerRadius = 14 * density
        b.background = g
        b.setOnClickListener { onClick() }
        return b
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            h.post(speakLoop)
        }
    }

    override fun onDestroy() {
        h.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
