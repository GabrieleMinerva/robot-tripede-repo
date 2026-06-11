package com.robottripede.app.data.diagnostics

import android.content.Context
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

object DiagnosticStore {
    private const val PREFS_NAME = "robot_diagnostics"
    private const val KEY_LAST_CRASH = "last_crash"
    private const val KEY_EVENTS = "events"
    private const val MAX_EVENTS = 80

    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            recordEvent(appContext, "uncaught_exception", "${thread.name}: ${throwable.javaClass.simpleName}")
            saveCrash(appContext, thread.name, throwable)
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(10)
            }
        }
    }

    fun recordEvent(context: Context, name: String, detail: String = "") {
        val safeDetail = detail
            .replace("\r", " ")
            .replace("\n", " ")
            .take(240)
        val event = "${timestamp()} | $name | $safeDetail"
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val events = prefs.getString(KEY_EVENTS, "").orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .toMutableList()
        events += event
        val clipped = events.takeLast(MAX_EVENTS).joinToString("\n")
        prefs.edit().putString(KEY_EVENTS, clipped).apply()
    }

    fun saveCrash(context: Context, screen: String, throwable: Throwable) {
        val report = buildString {
            appendLine("Robot Tripede Crash Report")
            appendLine("Time: ${timestamp()}")
            appendLine("Screen: $screen")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine()
            appendLine("Exception:")
            appendLine("${throwable.javaClass.name}: ${throwable.message.orEmpty()}")
            appendLine()
            appendLine("Stacktrace:")
            appendLine(Log.getStackTraceString(throwable))
        }
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_CRASH, report)
            .apply()
    }

    fun lastCrash(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_CRASH, null)
            ?: "Nessun crash registrato localmente."
    }

    fun recentEvents(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EVENTS, null)
            ?.ifBlank { null }
            ?: "Nessun evento diagnostico registrato."
    }

    fun buildFullReport(context: Context): String {
        return buildString {
            appendLine("Robot Tripede Diagnostic Report")
            appendLine("Generated: ${timestamp()}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Package: ${context.packageName}")
            appendLine()
            appendLine("Last crash")
            appendLine("----------")
            appendLine(lastCrash(context))
            appendLine()
            appendLine("Recent events")
            appendLine("-------------")
            appendLine(recentEvents(context))
        }
    }

    fun clear(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private fun timestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
    }
}
