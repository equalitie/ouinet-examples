package ie.equalit.ouinet_examples.android_kotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ie.equalit.ouinet_examples.android_kotlin.ExampleApp.Companion.cleanInsights
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet
import okhttp3.*
import org.cleaninsights.sdk.CleanInsights
import org.cleaninsights.sdk.ConsentRequestUi
import org.cleaninsights.sdk.Feature
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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

class MainActivity : AppCompatActivity() {
    private val ouinet by lazy { Ouinet(this) }
    lateinit var ouinetDir: String
    private val TAG = "OuinetTester"
    private val start = System.currentTimeMillis()
    private var firstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val get = findViewById<Button>(R.id.get)
        get.setOnClickListener{ getURL(get) }

        ouinet.setOnNotificationTapped {
            ouinet.background.shutdown(false)
        }
        ouinet.setOnConfirmTapped {
            ouinet.background.shutdown(true)
        }
        ouinet.setBackground(this)
        ouinetDir = ouinet.config.ouinetDirectory
        ouinet.background.startup()
        val consents= findViewById<Button>(R.id.consents)
        consents.setOnClickListener{
            startActivity(Intent(this, ConsentsActivity::class.java))
        }
        Executors.newFixedThreadPool(1).execute(Runnable { this.updateOuinetState() })
    }

    override fun onResume() {
        super.onResume()
        if (firstTime) {
            val ui = ConsentRequestUi(this)

            cleanInsights.requestConsent("test", ui) { granted ->
                if (!granted) return@requestConsent

                cleanInsights.requestConsent(Feature.Lang, ui) {
                    cleanInsights.requestConsent(Feature.Ua, ui)
                }

                val time = (System.currentTimeMillis() - start) / 1000.0

                cleanInsights.measureEvent("app-state", "startup-success", "test", "time-needed", time)
                cleanInsights.measureVisit(listOf("Main"), "test")
            }

            firstTime = false
        }
        else {
            cleanInsights.measureVisit(listOf("Main"), "test")
        }

        cleanInsights.testServer {
            if (it != null) {
                Log.e("Server Test", "Exception!")
                it.printStackTrace()
            } else {
                Log.i("Server Test", "No exception - works!")
            }
        }
    }

    private fun updateOuinetState() {
        val ouinetState = findViewById<View>(R.id.status) as TextView
        while (true) {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val state = ouinet.background.getState()
            runOnUiThread { ouinetState.text = "State: $state" }
        }
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
                response.body.use { _ ->
                    val responseHeaders = response.headers
                    var i = 0
                    val size = responseHeaders.size
                    while (i < size) {
                        println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                        i++
                    }
                    runOnUiThread { logViewer.text = responseHeaders.toString() }
                }
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
            val ouinetService = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8077))
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