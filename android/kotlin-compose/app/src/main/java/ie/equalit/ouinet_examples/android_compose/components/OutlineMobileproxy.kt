package ie.equalit.ouinet_examples.android_compose.components

import android.content.Context
import mobileproxy.Mobileproxy
import mobileproxy.Proxy
import mobileproxy.StreamDialer
import java.io.BufferedReader
import java.net.InetSocketAddress
import kotlin.io.readText

class OutlineMobileproxy(private val context : Context) {
    lateinit var dialer: StreamDialer
    lateinit var proxy: Proxy
    lateinit var sockAddr: InetSocketAddress

    fun setConfig() {
        // Setup and start Outline mobile proxy
        val testDomains = Mobileproxy.newListFromLines("ouinet.work\ni.ytimg.com")
        val strategiesConfig: String = context.assets.open("config.yaml")
            .bufferedReader()
            .use(BufferedReader::readText)
        dialer = Mobileproxy.newSmartStreamDialer(testDomains, strategiesConfig, Mobileproxy.newStderrLogWriter())
    }

    fun start() {
        proxy = Mobileproxy.runProxy("localhost:8080", dialer)
    }

    fun stop() {
        proxy.stop(0)
    }
}