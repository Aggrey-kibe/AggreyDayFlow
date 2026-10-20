package com.aggreykibe.dayflow.data

import android.content.Context
import android.content.SharedPreferences
import android.os.UserManager
import java.io.File

/**
 * Saves data in "device-protected" storage. Unlike normal app storage, Android lets the app read it
 * right after a restart, before the person has typed their PIN. That is what allows alarms to be restored
 * and to ring after an overnight restart. It is still private to this app. Data saved by earlier versions
 * of the app in normal storage is moved across the first time the phone is unlocked.
 */
object DeviceStorage {
    fun prefs(context: Context, name: String): SharedPreferences {
        val app = context.applicationContext
        val device = app.createDeviceProtectedStorageContext()
        migrateOldData(app, device, name)
        return device.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    private fun migrateOldData(app: Context, device: Context, name: String) {
        val unlocked = app.getSystemService(UserManager::class.java)?.isUserUnlocked ?: true
        if (!unlocked) return // normal storage cannot be read yet; try again after unlock
        val source = File(app.dataDir, "shared_prefs/$name.xml")
        val target = File(device.dataDir, "shared_prefs/$name.xml")
        if (source.exists() && !target.exists()) device.moveSharedPreferencesFrom(app, name)
    }
}
