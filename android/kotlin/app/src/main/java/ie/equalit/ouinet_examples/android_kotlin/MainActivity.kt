package ie.equalit.ouinet_examples.android_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet
import ie.equalit.ouinet_examples.android_kotlin.components.PermissionHandler
import ie.equalit.ouinet_examples.android_kotlin.components.PermissionHandler.Companion.PERMISSION_CODE_IGNORE_BATTERY_OPTIMIZATIONS
import okhttp3.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.net.URISyntaxException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.Executors
import javax.net.ssl.*
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private val ouinet by lazy { Ouinet(this) }
    lateinit var ouinetDir: String
    private val TAG = "OuinetTester"
    private val pHandler = PermissionHandler(this)
    private lateinit var mGroupsView : TextView
    private lateinit var mCacheView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGroupsView = findViewById<View>(R.id.groups) as TextView
        mGroupsView.text = String.format(getString(R.string.groups_text), 0)

        mCacheView = findViewById<View>(R.id.cache_size) as TextView
        mCacheView.text = String.format(getString(R.string.cache_text), 0)

        val restart = findViewById<Button>(R.id.restart)
        restart.setOnClickListener{
            ouinet.background.stop {
                ouinet.background.start()
            }
        }

        val clear = findViewById<Button>(R.id.clear)
        clear.setOnClickListener{ clearCache() }

        val get = findViewById<Button>(R.id.get)
        get.setOnClickListener{ getURL(get) }

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pHandler.requestPostNotificationPermission(this)
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pHandler.requestBatteryOptimizationsOff(this)
        }
         */

        ouinet.setOnNotificationTapped {
            beginShutdown(false)
        }
        ouinet.setOnConfirmTapped {
            beginShutdown(true)
        }
        ouinet.setBackground(this)
        ouinetDir = ouinet.config.ouinetDirectory
        Executors.newFixedThreadPool(1).execute(Runnable { this.updateOuinetState() })
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

                if (state == "Started") {
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

    fun clearCache() {
        val endpoint = ouinet.background.getFrontendEndpoint()
        val url = "http://" + endpoint!!.toString() + "/?purge_cache=do"

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
                }
                getGroups()
                getStatus()
            }
        })
    }

    fun getURL(view: View?) {
        val editText = findViewById<View>(R.id.url) as EditText
        val logViewer = findViewById<View>(R.id.log_viewer) as TextView
        val url = editText.text.toString()
        val toast = Toast.makeText(this, "Loading: $url", Toast.LENGTH_SHORT)
        toast.show()

        val client: OkHttpClient = getOuinetHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .header("X-Ouinet-Group", getDhtGroup(url))
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { logViewer.text = e.toString() }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseHeaders = response.headers
                var i = 0
                val size = responseHeaders.size
                while (i < size) {
                    println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                    i++
                }
                /* Consume the response body in an async thread */
                val thread = Thread {
                    try {
                        if (response.isSuccessful) {
                            println("Response ready")
                            val `in`: Reader? = response.body?.charStream()
                            val reader = BufferedReader(`in`)
                            var line: String? = reader.readLine()
                            while (line != null) {
                                println(line)
                                line = reader.readLine()
                            }
                            reader.close()
                            `in`?.close()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                thread.start()
                runOnUiThread {
                    logViewer.text = responseHeaders.toString()
                }
                getGroups()
                getStatus()
            }
        })
    }

    private fun getDhtGroup(url: String): String {
        var domain: String = ""
        try {
            domain = URI(url).schemeSpecificPart
            domain = domain.replace("^//".toRegex(), "")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
        return domain
    }

    private fun getOuinetHttpClient(): OkHttpClient {
        return try {
            val trustManagers: Array<TrustManager> = getOuinetTrustManager()

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(
                getSSLSocketFactory(trustManagers),
                (trustManagers[0] as X509TrustManager)
            )

            // Proxy to ouinet service
            val endpoint = ouinet.background.getProxyEndpoint()
            val ouinetService = Proxy(Proxy.Type.HTTP,
                InetSocketAddress(endpoint!!.getAddress(), endpoint!!.getPort()))
            builder.proxy(ouinetService)
            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    private fun getSSLSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, SecureRandom())
        return sslContext.socketFactory
    }

    @Throws(
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        CertificateException::class,
        IOException::class
    )
    private fun getOuinetTrustManager(): Array<TrustManager> {
        return arrayOf(OuinetTrustManager())
    }

    inner private class OuinetTrustManager : X509TrustManager {
        private var trustManager: X509TrustManager? = null
        private var ca: Certificate? = null

        init {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)
            for (tm in tmf.trustManagers) {
                if (tm is X509TrustManager) {
                    trustManager = tm
                    break
                }
            }
        }

        @get:Throws(
            KeyStoreException::class,
            CertificateException::class,
            NoSuchAlgorithmException::class,
            IOException::class
        )
        private val keyStore: KeyStore
            private get() {
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", certificateAuthority)
                return keyStore
            }

        @get:Throws(CertificateException::class)
        private val certificateAuthority: Certificate?
            private get() {
                var caInput: InputStream? = null
                try {
                    caInput = FileInputStream(ouinetDir + "/ssl-ca-cert.pem")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val cf = CertificateFactory.getInstance("X.509")
                ca = cf.generateCertificate(caInput)
                return ca
            }

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            for (cert in chain) {
                Log.d(TAG, "Server Cert Issuer: " + cert.issuerDN.name + " " + cert.subjectDN.name)
            }
            for (cert in trustManager!!.acceptedIssuers) {
                Log.d(TAG, "Client Trusted Issuer: " + cert.issuerDN.name)
            }
            trustManager!!.checkServerTrusted(chain, authType)
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf(ca as X509Certificate)
        }
    }
}