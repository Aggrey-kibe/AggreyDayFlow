package com.aggreykibe.dayflow.permissions

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Tecno, Infinix and itel phones (made by Transsion, running HiOS / XOS) add their own battery manager on top of Android.
 * It can stop apps when the screen locks. Android offers no standard way to change that, so the best the app can do is
 * explain and open the right screen. The screen names below are best-effort: they differ between HiOS versions and
 * have not been tested on a real Tecno phone, so each one is tried safely and the app's own settings page is the fallback.
 */
object OemSettings {
    fun isTranssion(): Boolean {
        val maker = (Build.MANUFACTURER + " " + Build.BRAND).lowercase()
        return listOf("tecno", "infinix", "itel", "transsion").any { it in maker }
    }

    /** Opens the Autostart screen if this phone has it; otherwise the phone manager app; otherwise the app's settings page. */
    fun openAutostart(context: Context) {
        val candidates = listOf(
            ComponentName("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
            ComponentName("com.cyin.himgr", "com.cyin.himgr.autostart.AutoStartActivity"),
            ComponentName("com.itel.autobootmanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"),
        )
        for (component in candidates) {
            if (launch(context, Intent().setComponent(component))) return
        }
        for (pkg in listOf("com.transsion.phonemaster", "com.cyin.himgr")) {
            val launcher = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launcher != null && launch(context, launcher)) return
        }
        openAppDetails(context)
    }

    fun openAppDetails(context: Context) {
        launch(
            context,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")),
        )
    }

    private fun launch(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }
}
