package ie.equalit.ouinet_examples.android_kotlin.components

import android.content.Context
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet_examples.android_kotlin.BuildConfig
import ie.equalit.ouinet_examples.android_kotlin.R

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
            //.setBtBootstrapExtras(getBtBootstrapExtras())
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

    /*
    private fun getBtBootstrapExtras() : Set<String>? {
        var countryIsoCode = ""
        val locationUtils = CenoLocationUtils(context.application)
        countryIsoCode = locationUtils.currentCountry

        // Attempt getting country-specific `BT_BOOTSTRAP_EXTRAS` entry from BuildConfig,
        // fall back to empty BT bootstrap extras otherwise.
        var btbsxsStr= ""
        if (countryIsoCode.isNotEmpty()) {
            // Country code found, try getting bootstrap extras resource for this country
            for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) {
                if (countryIsoCode == entry[0]) {
                    btbsxsStr = entry[1]
                }
            }
        }

        if (btbsxsStr != "") {
            // Bootstrap extras resource found
            val btbsxs: HashSet<String> = HashSet()
            for (x in btbsxsStr.split(" ").toTypedArray()) {
                if (x.isNotEmpty()) {
                    btbsxs.add(x)
                }
            }
            if (btbsxs.size > 0) {
                return btbsxs
            }
        }
        // else no bootstrap extras included, leave null
        return null
    }
     */
}