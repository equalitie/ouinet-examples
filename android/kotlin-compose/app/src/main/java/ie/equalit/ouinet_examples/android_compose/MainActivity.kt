package ie.equalit.ouinet_examples.android_compose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import ie.equalit.ouinet_examples.android_compose.components.HttpClient
import ie.equalit.ouinet_examples.android_compose.components.Ouinet
import ie.equalit.ouinet_examples.android_compose.components.PermissionHandler
import ie.equalit.ouinet_examples.android_compose.components.PermissionHandler.Companion.PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS

class MainActivity : AppCompatActivity() {
    private val ouinet by lazy { Ouinet(this) }
    private val httpClient  = HttpClient()
    private val pHandler = PermissionHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pHandler.requestPostNotificationPermission(this)
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pHandler.requestBatteryOptimizationsOff(this)
        }
         */
        Log.d(TAG, "Launching MainActivity")
        ouinet.setBackground(this)
        val viewModel = OuinetViewModel(ouinet, httpClient)
        setContent { MaterialTheme { OuinetView(viewModel) } }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS) {
            pHandler.isIgnoringBatteryOptimizations()
        }
    }

    companion object {
        private const val TAG = "OuinetTester"
    }
}