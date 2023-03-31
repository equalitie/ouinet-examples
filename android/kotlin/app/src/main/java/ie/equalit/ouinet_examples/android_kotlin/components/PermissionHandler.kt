package ie.equalit.ouinet_examples.android_kotlin.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

/* CENO: Handles checking which permissions have been granted,
 * adapted from https://pub.dev/packages/flutter_background
 */
class PermissionHandler(private val context: Context) {
    companion object {
        const val PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS = 5672353
    }

    /*
    fun isWakeLockPermissionGranted(): Boolean
    {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        };
    }
    */

    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val powerManager = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            // Before Android 12 (S), the battery optimization isn't needed -> Always "ignoring"
            true
        }
    }

    @SuppressLint("BatteryLife")
    fun requestBatteryOptimizationsOff(activity: Activity) : Boolean {
        var result = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Before Android 12 (S) the battery optimization isn't needed for our use case -> Always "ignoring"
            result = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val powerManager = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            when {
                powerManager.isIgnoringBatteryOptimizations(context.packageName) -> {
                    result = false
                }
                context.checkSelfPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_DENIED -> {
                    result = false
                }
                else -> {
                    // Only return true if intent was sent to request permission
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:${context.packageName}")
                    activity.startActivityForResult(intent, PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS)
                    result = true
                }
            }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPostNotificationPermission(activity: AppCompatActivity) {
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                /* If POST_NOTIFICATION permission is granted, then ask to disable battery optimization as well */
                requestBatteryOptimizationsOff(activity)
            }
            /* else, POST_NOTIFICATION denied, so there is no reason to disable battery optimization */
        }.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
