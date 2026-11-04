package com.abdulhameed.pubgflash

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val h = Handler(Looper.getMainLooper())
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
        buildUi()
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

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(14).toFloat()
        return g
    }

    private fun box(parent: LinearLayout, label: String): TextView {
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.gravity = Gravity.CENTER
        col.setPadding(0, dp(16), 0, dp(16))
        col.background = rounded(0xFF17171F.toInt())
        val num = TextView(this)
        num.textSize = 30f
        num.typeface = Typeface.DEFAULT_BOLD
        num.setTextColor(0xFFFFB400.toInt())
        num.gravity = Gravity.CENTER
        num.text = "--"
        val lbl = TextView(this)
        lbl.textSize = 11f
        lbl.setTextColor(0xFF9A9AA6.toInt())
        lbl.gravity = Gravity.CENTER
        lbl.text = label.uppercase()
        col.addView(num)
        col.addView(lbl)
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp.setMargins(dp(4), 0, dp(4), 0)
        parent.addView(col, lp)
        return num
    }

    private fun button(text: String, bg: Int, fg: Int, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.isAllCaps = false
        b.textSize = 16f
        b.setTextColor(fg)
        b.background = rounded(bg)
        b.setOnClickListener { onClick() }
        return b
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(0xFF0B0B0F.toInt())
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(48), dp(18), dp(28))
        scroll.addView(root)

        val title = TextView(this)
        title.text = "\u26A1 PUBG Flash Launch"
        title.textSize = 24f
        title.typeface = Typeface.DEFAULT_BOLD
        title.setTextColor(0xFFF5F5F7.toInt())
        title.gravity = Gravity.CENTER
        root.addView(title)

        tvWhen = TextView(this)
        tvWhen.textSize = 14f
        tvWhen.setTextColor(0xFF9A9AA6.toInt())
        tvWhen.gravity = Gravity.CENTER
        tvWhen.setPadding(0, dp(4), 0, dp(22))
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
        tvStatus.setTextColor(0xFFF5F5F7.toInt())
        tvStatus.gravity = Gravity.CENTER
        tvStatus.setPadding(0, dp(18), 0, dp(8))
        root.addView(tvStatus)

        tvInfo = TextView(this)
        tvInfo.textSize = 14f
        tvInfo.setTextColor(0xFF9A9AA6.toInt())
        tvInfo.setPadding(dp(14), dp(14), dp(14), dp(14))
        tvInfo.background = rounded(0xFF17171F.toInt())
        val infoLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        infoLp.setMargins(0, dp(10), 0, dp(14))
        root.addView(tvInfo, infoLp)

        val btnLp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        btnLp.setMargins(0, dp(8), 0, 0)
        root.addView(
            button("Open on Google Play", 0xFFFFB400.toInt(), 0xFF111111.toInt()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Store.PLAY_URL)))
            }, btnLp
        )
        root.addView(
            button("Test the alarm now", 0xFF17171F.toInt(), 0xFFF5F5F7.toInt()) {
                Notifier.alarm(this)
            }, btnLp
        )
        setContentView(scroll)
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
