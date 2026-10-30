package ie.equalit.ouinet_examples.android_compose.components

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.Proxy
import java.net.URI
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class HttpClient {
   lateinit var client: OkHttpClient
    lateinit var caCertDir: String
    var enabled = false

    fun setClient(proxy: Proxy?, certDir: String?) {
        if (certDir != null) {
            caCertDir = certDir
            Log.d(TAG, "Set caCertDir = $caCertDir")
        }
        else {
            Log.d(TAG, "Did not set caCertDir")
        }

        val trustManagers: Array<TrustManager> = getOuinetTrustManager()

        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(
            getSSLSocketFactory(trustManagers),
            (trustManagers[0] as X509TrustManager)
        )
        // Proxy to ouinet service
        if (proxy != null) {
            builder.proxy(proxy)
        }
        client = builder.build()
        enabled = true
   }

    fun getURL(url: String, callback : (String, String) -> Unit) {
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
                val responseHeaders = response.headers
                var i = 0
                val size = responseHeaders.size
                while (i < size) {
                    println(responseHeaders.name(i) + ": " + responseHeaders.value(i))
                    i++
                }
                /* Consume the response body in an async thread */
                var body = ""
                val thread = Thread {
                    try {
                        if (response.isSuccessful) {
                            println("Response ready")
                            val `in`: Reader? = response.body?.charStream()
                            val reader = BufferedReader(`in`)
                            var line: String? = reader.readLine()
                            while (line != null) {
                                println(line)
                                body += line + "\n"
                                line = reader.readLine()
                            }
                            reader.close()
                            `in`?.close()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    callback.invoke(responseHeaders.toString(), body)
                }
                thread.start()
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

    @SuppressLint("CustomX509TrustManager")
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
                    caInput = FileInputStream("$caCertDir/ssl-ca-cert.pem")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val cf = CertificateFactory.getInstance("X.509")
                ca = cf.generateCertificate(caInput)
                return ca
            }

        @SuppressLint("TrustAllX509TrustManager")
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

    companion object {
        const val TAG = "HttpClient"
    }
}
