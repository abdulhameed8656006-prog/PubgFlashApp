package com.abdulhameed.pubgflash

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Store {
    const val INFO_URL = "https://abdulhameed8656006-prog.github.io/PubgFlash/launch.json"
    const val PLAY_URL = "https://play.google.com/store/apps/details?id=com.tencent.igfit"
    const val DEFAULT_DATE = "2026-11-13T00:00:00"

    fun prefs(c: Context): SharedPreferences =
        c.getSharedPreferences("flash", Context.MODE_PRIVATE)

    fun parse(s: String): Long {
        return try {
            val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            f.timeZone = TimeZone.getDefault()
            f.parse(s)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun launchMillis(c: Context): Long {
        val t = parse(prefs(c).getString("date", DEFAULT_DATE) ?: DEFAULT_DATE)
        return if (t > 0L) t else parse(DEFAULT_DATE)
    }

    fun isAcked(c: Context): Boolean =
        prefs(c).getLong("ack", -1L) == launchMillis(c)

    fun ack(c: Context) {
        prefs(c).edit().putLong("ack", launchMillis(c)).apply()
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.cancel(Notifier.ID_LAUNCH)
        nm.cancel(Notifier.ID_ALARM)
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(Scheduler.pending(c, Scheduler.REQ_ALARM, "alarm"))
        am.cancel(Scheduler.pending(c, Scheduler.REQ_LAUNCH, "launch"))
    }
}

object Net {
    fun refresh(c: Context): Boolean {
        return try {
            val conn = URL(Store.INFO_URL + "?t=" + System.currentTimeMillis())
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val j = JSONObject(text)
            Store.prefs(c).edit()
                .putString("date", j.optString("launchDate", Store.DEFAULT_DATE))
                .putBoolean("confirmed", j.optBoolean("confirmed", false))
                .putString("headline", j.optString("headline", ""))
                .putString("notes", j.optString("notes", ""))
                .putString("updated", j.optString("updated", ""))
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}

object Scheduler {
    const val REQ_LAUNCH = 1
    const val REQ_ALARM = 2
    const val REQ_REFRESH = 3
    private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun pending(c: Context, req: Int, action: String): PendingIntent {
        val i = Intent(c, AlarmReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(c, req, i, FLAGS)
    }

    fun alarmMillis(c: Context): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = Store.launchMillis(c)
        cal.set(Calendar.HOUR_OF_DAY, 14)
        cal.set(Calendar.MINUTE, 30)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun setAt(am: AlarmManager, time: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        }
    }

    fun scheduleAll(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val launchPi = pending(c, REQ_LAUNCH, "launch")
        val alarmPi = pending(c, REQ_ALARM, "alarm")
        am.cancel(launchPi)
        am.cancel(alarmPi)
        if (Store.isAcked(c)) return
        val now = System.currentTimeMillis()
        val launch = Store.launchMillis(c)
        val alarm = alarmMillis(c)
        if (launch > now) setAt(am, launch, launchPi)
        if (alarm > now) setAt(am, alarm, alarmPi)
    }

    fun scheduleRefresh(c: Context) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            c, REQ_REFRESH, Intent(c, RefreshReceiver::class.java), FLAGS
        )
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR,
            AlarmManager.INTERVAL_HALF_DAY,
            pi
        )
    }
}

object Notifier {
    const val CH_LAUNCH = "launch"
    const val CH_ALARM = "alarm"
    const val ID_LAUNCH = 10
    const val ID_ALARM = 11
    private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun channels(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        val launch = NotificationChannel(CH_LAUNCH, "Launch alert", NotificationManager.IMPORTANCE_HIGH)
        val aa = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val alarm = NotificationChannel(CH_ALARM, "Launch alarm", NotificationManager.IMPORTANCE_HIGH)
        alarm.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), aa)
        alarm.enableVibration(true)
        alarm.vibrationPattern = longArrayOf(0, 600, 300, 600)
        nm.createNotificationChannel(launch)
        nm.createNotificationChannel(alarm)
    }

    fun launch(c: Context) {
        val open = Intent(c, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("ack", true)
        val pi = PendingIntent.getActivity(c, 20, open, FLAGS)
        val n = Notification.Builder(c, CH_LAUNCH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("PUBG Flash is out!")
            .setContentText("Tap to open. If you don't, an alarm rings at 2:30 PM.")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        c.getSystemService(NotificationManager::class.java).notify(ID_LAUNCH, n)
    }

    fun alarm(c: Context) {
        channels(c)
        val open = Intent(c, AlarmActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(c, 21, open, FLAGS)
        val stop = PendingIntent.getBroadcast(c, 22, Intent(c, StopReceiver::class.java), FLAGS)
        val n = Notification.Builder(c, CH_ALARM)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("PUBG Flash is out!")
            .setContentText("Tap to open, or press Stop alarm.")
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(0, "Stop alarm", stop).build())
            .build()
        n.flags = n.flags or Notification.FLAG_INSISTENT
        c.getSystemService(NotificationManager::class.java).notify(ID_ALARM, n)
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (Store.isAcked(c)) return
        Notifier.channels(c)
        when (i.action) {
            "launch" -> Notifier.launch(c)
            "alarm" -> Notifier.alarm(c)
        }
    }
}

class StopReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Store.ack(c)
    }
}

class RefreshReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val result = goAsync()
        Thread {
            try {
                Net.refresh(c)
                Scheduler.scheduleAll(c)
            } finally {
                result.finish()
            }
        }.start()
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Scheduler.scheduleAll(c)
        Scheduler.scheduleRefresh(c)
    }
}
