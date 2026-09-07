package com.arslan.shizuwall

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import com.arslan.shizuwall.receivers.FirewallControlReceiver
import com.arslan.shizuwall.ui.MainActivity
import rikka.shizuku.Shizuku

class ShizuWallApp : Application() {

    private var startedActivities = 0
    private var rebootReapplySent = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) { startedActivities++ }
            override fun onActivityStopped(activity: Activity) { startedActivities-- }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        try {
            Shizuku.addBinderReceivedListenerSticky { reapplyAfterRebootIfNeeded() }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to register Shizuku binder listener", t)
        }
    }

    private fun reapplyAfterRebootIfNeeded() {
        if (rebootReapplySent || startedActivities > 0) return
        val prefs = try {
            getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE)
        } catch (t: Throwable) {
            return
        }
        if (prefs.getString(MainActivity.KEY_WORKING_MODE, WorkingMode.SHIZUKU.name) != WorkingMode.SHIZUKU.name) return
        if (!prefs.getBoolean(MainActivity.KEY_AUTO_ENABLE_ON_SHIZUKU_START, false)) return
        if (!prefs.getBoolean(MainActivity.KEY_FIREWALL_ENABLED, false)) return
        val savedElapsed = prefs.getLong(MainActivity.KEY_FIREWALL_SAVED_ELAPSED, -1L)
        if (savedElapsed <= 0L || SystemClock.elapsedRealtime() >= savedElapsed) return

        rebootReapplySent = true
        Log.d(TAG, "Shizuku started after reboot, re-enabling firewall")
        sendBroadcast(
            Intent(this, FirewallControlReceiver::class.java).apply {
                action = MainActivity.ACTION_FIREWALL_CONTROL
                putExtra(MainActivity.EXTRA_FIREWALL_ENABLED, true)
                putExtra(FirewallControlReceiver.EXTRA_AUTOMATION_EVENT, true)
            }
        )
    }

    companion object {
        private const val TAG = "ShizuWallApp"
    }
}
