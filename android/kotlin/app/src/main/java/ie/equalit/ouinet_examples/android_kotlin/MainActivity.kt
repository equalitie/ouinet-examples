package ie.equalit.ouinet_examples.android_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import ie.equalit.ouinet_examples.android_kotlin.components.HttpClient
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet
import ie.equalit.ouinet_examples.android_kotlin.components.PermissionHandler
import ie.equalit.ouinet_examples.android_kotlin.components.PermissionHandler.Companion.PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private val ouinet by lazy { Ouinet(this) }
    private val httpClient  = HttpClient()
    private val TAG = "OuinetTester"
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

        ouinet.setBackground(this)
        val viewModel = OuinetViewModel(ouinet, httpClient)
        setContent { MaterialTheme { OuinetView(viewModel) } }
    }

    fun startOuinet(view: View?) {
        val toast = Toast.makeText(this, "Starting Ouinet service", Toast.LENGTH_SHORT)
        ouinet.background.startup()
        toast.show()
    }

    private fun exitOuinetServiceProcess() {
        getSystemService(Context.ACTIVITY_SERVICE).let { am ->
            (am as ActivityManager).runningAppProcesses?.let { processes ->
                for (process in processes) {
                    if (process.processName.contains("ouinetService")){
                        Process.killProcess(process.pid)
                    }
                }
            }
        }
    }

    private fun beginShutdown(doClear : Boolean) {
        ouinet.background.shutdown(doClear)
        {
            if(doClear) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.clearApplicationUserData()
            }
            /*  For some reason, exitProcess(0) fails to kill the ouinetService
             *  so use this shutdown method callback to force exit it
             *  eventually exitOuinetServiceProcess method can be moved to ouinet AAR */
            exitOuinetServiceProcess()
            exitProcess(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS) {
            pHandler.isIgnoringBatteryOptimizations()
        }
    }

    /*
    private fun updateOuinetState() {
        val ouinetState = findViewById<View>(R.id.status) as TextView
        val ouinetEndpoints = findViewById<View>(R.id.endpoints) as TextView
        val buttonGet = findViewById<View>(R.id.get) as Button
        val buttonStart = findViewById<View>(R.id.start) as Button
        val urlInput = findViewById<View>(R.id.url) as EditText

        while (true) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val state = ouinet.background.getState()
            runOnUiThread {
                ouinetState.text = "State: $state"

                if (state == "Degraded" || state == "Started") {
                    buttonGet.isVisible = true
                    buttonStart.isVisible = false
                    ouinetEndpoints.isVisible = true
                    urlInput.isVisible = true
                } else {
                    buttonGet.isVisible = false
                    buttonStart.isVisible = true
                    ouinetEndpoints.isVisible = false
                    val proxy_endpoint = ouinet.background.getProxyEndpoint()
                    val frontend_endpoint = ouinet.background.getFrontendEndpoint()
                    if (proxy_endpoint != null && frontend_endpoint != null)
                        ouinetEndpoints.text = "P: " + proxy_endpoint.toString() + " / F: " + frontend_endpoint.toString()
                    urlInput.isVisible = false
                }
            }
        }
    }
    */

    private fun log2(n: Int): Double {
        return ln(n.toDouble()) / ln(2.0)
    }

    private fun bytesToString(b: Int): String {
        // originally from <https://stackoverflow.com/a/42408230>
        // ported from extension JS code to kotlin
        if (b == 0) {
            return "0 B"
        }
        val i = floor(log2(b) / 10).toInt()
        val v = b / 1024.0.pow(i)
        val u = "KMGTPEZY"[i - 1] + "iB";
        return String.format("%.2f %s", v, u)
    }

    /*
    fun getGroups() {
        val endpoint = ouinet.background.getFrontendEndpoint()
        val url = "http://" + endpoint!!.toString() + "/groups.txt"

        val client: OkHttpClient = getOuinetHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .header("X-Ouinet-Group", getDhtGroup(url))
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                //runOnUiThread { logViewer.text = e.toString() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body.use { body ->
                    val responseHeaders = response.headers
                    var i = 0
                    val size = responseHeaders.size
                    while (i < size) {
                        println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                        i++
                    }
                    body?.let {
                        val groups = it.string().reader().readLines()
                        Log.d(TAG, "Count of sites cached: ${groups.count()}")
                        Log.d(TAG, "Sites cached: $groups")
                        runOnUiThread {
                            mGroupsView.text = String.format(
                                getString(R.string.groups_text),
                                groups.count()
                            )
                        }
                    }
                }
            }
        })
    }

    fun getStatus() {
        val endpoint = ouinet.background.getFrontendEndpoint()
        val url = "http://" + endpoint!!.toString() + "/api/status"

        val client: OkHttpClient = getOuinetHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .header("X-Ouinet-Group", getDhtGroup(url))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body.use { body ->
                    val responseHeaders = response.headers
                    var i = 0
                    val size = responseHeaders.size
                    while (i < size) {
                        println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                        i++
                    }
                    body?.let {
                        val status = it.string()
                        Log.d(TAG, "Ouinet status: $status")
                        val statusArr = status.split(",")
                        for (entry in statusArr) {
                            if (entry.contains("local_cache_size")){
                                val cacheSize = entry.substring(entry.indexOf(":") + 1)
                                val byteString = bytesToString(cacheSize.toInt())
                                Log.d(TAG, "Ouinet cache size: $cacheSize")
                                runOnUiThread {
                                    mCacheView.text = String.format(
                                        getString(R.string.cache_text),
                                        byteString
                                    )
                                }
                            }
                        }
                    }
                }
            }
        })
    }
    */
}