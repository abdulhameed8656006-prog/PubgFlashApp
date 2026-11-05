package com.abdulhameed.pubgflash

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
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
        window.statusBarColor = Style.BG_TOP
        window.navigationBarColor = Style.BG_BOTTOM

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.background = Style.background()
        root.setPadding(Style.dp(this, 24), 0, Style.dp(this, 24), 0)

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.ic_launcher)
        root.addView(logo, LinearLayout.LayoutParams(Style.dp(this, 130), Style.dp(this, 130)))
        val pulse = ScaleAnimation(
            1f, 1.12f, 1f, 1.12f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        pulse.duration = 600
        pulse.repeatMode = Animation.REVERSE
        pulse.repeatCount = Animation.INFINITE
        logo.startAnimation(pulse)

        val t = TextView(this)
        t.text = "PUBG Flash is out!"
        t.textSize = 32f
        t.typeface = Typeface.DEFAULT_BOLD
        t.setTextColor(Style.GOLD_TOP)
        t.gravity = Gravity.CENTER
        t.setPadding(0, Style.dp(this, 24), 0, 0)
        root.addView(t)

        val sub = TextView(this)
        sub.text = "Flash Alarm"
        sub.textSize = 15f
        sub.setTextColor(Style.TEXT_DIM)
        sub.gravity = Gravity.CENTER
        root.addView(sub)

        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, Style.dp(this, 28), 0, 0)
        root.addView(
            Style.button(this, "Stop alarm", true) {
                Store.ack(this)
                finish()
            }, lp
        )
        val lp2 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp2.setMargins(0, Style.dp(this, 12), 0, 0)
        root.addView(
            Style.button(this, "Open Google Play", false) {
                Store.ack(this)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Store.PLAY_URL)))
                finish()
            }, lp2
        )

        setContentView(root)
        tts = TextToSpeech(this, this)
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
