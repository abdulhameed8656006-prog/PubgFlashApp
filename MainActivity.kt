package com.abdulhameed.pubgflash

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
    private lateinit var frame: FrameLayout
    private lateinit var tvWhen: TextView
    private lateinit var tvD: TextView
    private lateinit var tvH: TextView
    private lateinit var tvM: TextView
    private lateinit var tvS: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvInfo: TextView

    private val ticker = object : Runnable {
        override fun run() {
            update()
            h.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Style.BG_TOP
        window.navigationBarColor = Style.BG_BOTTOM
        buildUi()
        if (savedInstanceState == null && !intent.getBooleanExtra("ack", false)) {
            showSplash()
        }
        Notifier.channels(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        handleIntent(intent)
        Scheduler.scheduleAll(this)
        Scheduler.scheduleRefresh(this)
        showInfo(true)
        Thread {
            val ok = Net.refresh(this)
            Scheduler.scheduleAll(this)
            runOnUiThread {
                showInfo(ok)
                update()
            }
        }.start()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        h.post(ticker)
    }

    override fun onPause() {
        super.onPause()
        h.removeCallbacks(ticker)
    }

    private fun handleIntent(i: Intent?) {
        val tapped = i?.getBooleanExtra("ack", false) == true
        if (tapped || System.currentTimeMillis() >= Store.launchMillis(this)) {
            if (!Store.isAcked(this)) Store.ack(this)
        }
    }

    private fun dp(v: Int): Int = Style.dp(this, v)

    private fun box(parent: LinearLayout, label: String): TextView {
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        col.setPadding(0, dp(16), 0, dp(16))
        col.background = Style.card(dp(16).toFloat())
        val num = TextView(this)
        num.textSize = 30f
        num.typeface = Typeface.DEFAULT_BOLD
        num.setTextColor(Style.GOLD_TOP)
        num.gravity = Gravity.CENTER
        num.text = "--"
        val lbl = TextView(this)
        lbl.textSize = 11f
        lbl.setTextColor(Style.TEXT_DIM)
        lbl.gravity = Gravity.CENTER
        lbl.text = label.uppercase()
        col.addView(num)
        col.addView(lbl)
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp.setMargins(dp(4), 0, dp(4), 0)
        parent.addView(col, lp)
        return num
    }

    private fun buildUi() {
        frame = FrameLayout(this)
        frame.background = Style.background()

        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(28), dp(18), dp(28))
        scroll.addView(root)

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.ic_launcher)
        val logoLp = LinearLayout.LayoutParams(dp(92), dp(92))
        logoLp.gravity = Gravity.CENTER_HORIZONTAL
        root.addView(logo, logoLp)

        val title = TextView(this)
        title.text = "Flash Alarm"
        title.textSize = 28f
        title.typeface = Typeface.DEFAULT_BOLD
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setPadding(0, dp(10), 0, 0)
        root.addView(title)

        val sub = TextView(this)
        sub.text = "PUBG Flash launch countdown"
        sub.textSize = 14f
        sub.setTextColor(Style.TEXT_DIM)
        sub.gravity = Gravity.CENTER
        root.addView(sub)

        tvWhen = TextView(this)
        tvWhen.textSize = 14f
        tvWhen.setTextColor(Color.WHITE)
        tvWhen.gravity = Gravity.CENTER
        tvWhen.setPadding(0, dp(18), 0, dp(14))
        root.addView(tvWhen)

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        tvD = box(row, "Days")
        tvH = box(row, "Hours")
        tvM = box(row, "Minutes")
        tvS = box(row, "Seconds")
        root.addView(row)

        tvStatus = TextView(this)
        tvStatus.textSize = 15f
        tvStatus.setTextColor(Color.WHITE)
        tvStatus.gravity = Gravity.CENTER
        tvStatus.setPadding(0, dp(16), 0, dp(6))
        root.addView(tvStatus)

        tvInfo = TextView(this)
        tvInfo.textSize = 14f
        tvInfo.setTextColor(Style.TEXT_DIM)
        tvInfo.setPadding(dp(16), dp(14), dp(16), dp(14))
        tvInfo.background = Style.card(dp(16).toFloat())
        val infoLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        infoLp.setMargins(0, dp(10), 0, dp(14))
        root.addView(tvInfo, infoLp)

        val btnLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        btnLp.setMargins(0, dp(8), 0, 0)
        root.addView(
            Style.button(this, "Open on Google Play", true) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Store.PLAY_URL)))
            }, btnLp
        )
        root.addView(
            Style.button(this, "Test the alarm now", false) {
                Notifier.alarm(this)
            }, btnLp
        )

        frame.addView(
            scroll,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        setContentView(frame)
    }

    private fun showSplash() {
        val splash = LinearLayout(this)
        splash.orientation = LinearLayout.VERTICAL
        splash.gravity = Gravity.CENTER
        splash.background = Style.background()
        splash.isClickable = true

        val logo = ImageView(this)
        logo.setImageResource(R.drawable.ic_launcher)
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        splash.addView(logo, LinearLayout.LayoutParams(dp(160), dp(160)))

        val name = TextView(this)
        name.text = "Flash Alarm"
        name.textSize = 34f
        name.typeface = Typeface.DEFAULT_BOLD
        name.setTextColor(Color.WHITE)
        name.gravity = Gravity.CENTER
        name.setPadding(0, dp(22), 0, 0)
        name.alpha = 0f
        splash.addView(name)

        val tag = TextView(this)
        tag.text = "Never miss the PUBG Flash launch"
        tag.textSize = 15f
        tag.setTextColor(Style.GOLD_TOP)
        tag.gravity = Gravity.CENTER
        tag.setPadding(0, dp(6), 0, 0)
        tag.alpha = 0f
        splash.addView(tag)

        frame.addView(
            splash,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(700).setInterpolator(OvershootInterpolator()).start()
        name.animate().alpha(1f).setStartDelay(500).setDuration(500).start()
        tag.animate().alpha(1f).setStartDelay(800).setDuration(500).start()

        h.postDelayed({
            splash.animate().alpha(0f).setDuration(450).withEndAction {
                frame.removeView(splash)
            }.start()
        }, 2200)
    }

    private fun showInfo(ok: Boolean) {
        val p = Store.prefs(this)
        val head = p.getString("headline", "") ?: ""
        val notes = p.getString("notes", "") ?: ""
        val upd = p.getString("updated", "") ?: ""
        val sb = StringBuilder()
        if (!ok) sb.append("Offline, showing saved info.\n\n")
        if (head.isEmpty() && notes.isEmpty()) {
            sb.append("Checking the internet for the latest launch info...")
        } else {
            sb.append(head)
            if (notes.isNotEmpty()) sb.append("\n\n").append(notes)
            if (upd.isNotEmpty()) sb.append("\n\nLast updated: ").append(upd)
        }
        tvInfo.text = sb.toString()
    }

    private fun update() {
        val target = Store.launchMillis(this)
        val fmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val confirmed = Store.prefs(this).getBoolean("confirmed", false)
        tvWhen.text = "Launch: " + fmt.format(Date(target)) + (if (confirmed) "" else " (not confirmed yet)")
        val diff = target - System.currentTimeMillis()
        if (diff <= 0L) {
            tvD.text = "00"
            tvH.text = "00"
            tvM.text = "00"
            tvS.text = "00"
            tvStatus.text = "PUBG Flash launch time has arrived!"
            if (!Store.isAcked(this)) Store.ack(this)
        } else {
            val s = diff / 1000
            tvD.text = (s / 86400).toString()
            tvH.text = "%02d".format(s % 86400 / 3600)
            tvM.text = "%02d".format(s % 3600 / 60)
            tvS.text = "%02d".format(s % 60)
            tvStatus.text = "Time left until launch"
        }
    }
}
