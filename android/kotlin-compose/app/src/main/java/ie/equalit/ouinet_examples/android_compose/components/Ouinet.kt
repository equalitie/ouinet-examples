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
        var builder = Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorTlsCert(BuildConfig.INJECTOR_TLS_CERT)
            .setTlsCaCertStoreDir(context.resources.getString(R.string.cacert_file_path))
            .setCacheType(BuildConfig.CACHE_TYPE)
            .setLogLevel(Config.LogLevel.DEBUG)
            .setListenOnTcp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.PROXY_PORT)
            .setFrontEndEp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.FRONTEND_PORT)
            .setDnsProtocols(dnsProtocols)
            .setDropSavedOpts(true)
            //.setDisableBridgeAnnouncement(true)
            //.setBtBootstrapExtras(getBtBootstrapExtras())

        if (BuildConfig.INJECTOR_EP != "" &&
            BuildConfig.INJECTOR_CREDENTIALS != "") {
            builder.setInjectorEp(BuildConfig.INJECTOR_EP)
                .setInjectorCredentials(BuildConfig.INJECTOR_CREDENTIALS)
        }

        if (BuildConfig.CACHE_TYPE == "ouisync" &&
            BuildConfig.OUISYNC_PAGE_INDEX != ""
        ) {
            builder.setOuisyncPageIndex(BuildConfig.OUISYNC_PAGE_INDEX )
        }

        if (BuildConfig.CACHE_TYPE == "bep3-http-over-i2p") {
            builder.setI2pBep3Tracker(BuildConfig.I2P_BEP3_TRACKER)
                //.setEnableI2pServiceExt("127.0.0.1:7656")
                //.setEnableI2pServiceExe(findI2PDExe(context).toString())
                .setEnableI2pServiceLib(true)
                .addRequestField("X-Ouinet-Route: PublicInjector Bep3HTTPOverI2P")
                .setDisableOriginAccess(true)
        }
        builder.build()
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