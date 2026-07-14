package ie.equalit.ouinet_examples.android_compose.components

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Process
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet_examples.android_compose.BuildConfig
import ie.equalit.ouinet_examples.android_compose.R

class Ouinet (
    private val context : Context
) {

    val dnsProtocols = setOf("https", "plain")
    val config: Config by lazy {
        Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorCredentials(BuildConfig.INJECTOR_CREDENTIALS)
            .setInjectorTlsCert(BuildConfig.INJECTOR_TLS_CERT)
            .setTlsCaCertStorePath(context.resources.getString(R.string.cacert_file_path))
            .setCacheType(context.resources.getString(R.string.cache_type))
            .setLogLevel(Config.LogLevel.DEBUG)
            //.setDisableBridgeAnnouncement(true)
            //.setDisableOriginAccess(true)
            //.setBtBootstrapExtras(setOf("198.51.100.99"))
            .setListenOnTcp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.PROXY_PORT)
            .setFrontEndEp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.FRONTEND_PORT)
            .setDnsProtocols(dnsProtocols)
            .build()
    }

    lateinit var background : OuinetBackground
    fun setBackground (ctx: Context) {
        background = OuinetBackground.Builder(ctx)
            .setOuinetConfig(config)
            .build()
    }

    fun exitServiceProcess() {
        context.getSystemService(ACTIVITY_SERVICE).let { am ->
            (am as ActivityManager).runningAppProcesses?.let { processes ->
                for (process in processes) {
                    if (process.processName.contains("ouinetService")){
                        Process.killProcess(process.pid)
                    }
                }
            }
        }
    }
}