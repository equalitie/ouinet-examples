package ie.equalit.ouinet_examples.android_kotlin.components

import android.content.Context
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet_examples.android_kotlin.BuildConfig
import ie.equalit.ouinet_examples.android_kotlin.R

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.createDirectories

class Ouinet(
    private val context: Context
) {
    companion object {
        fun findI2PDExe(ctx: Context): Path {
            require(!ctx.applicationInfo.nativeLibraryDir.isNullOrEmpty())
            val libDir: Path
            try {
                libDir = Paths.get(ctx.applicationInfo.nativeLibraryDir)
            } catch (exception: Exception) {
                throw Exception("native lib dir is not parseable into a path", exception)
            }
            val exepath = libDir.resolve("libexeci2pd.so")

            if (!Files.exists(libDir)) {
                throw IllegalArgumentException("the dir $libDir does not exist")
            } else if (!Files.exists(exepath)) {
                val ls = libDir.listDirectoryEntries().joinToString("\n")
                throw IllegalArgumentException("the file $exepath does not exist, what exists in libDir is [$ls]")
            }
            return exepath
        }

        fun startI2PD(ctx: Context): Process {
            val exepath = findI2PDExe(ctx)
            val libDir = exepath.getParent()

            val datadir = Paths.get(ctx.cacheDir.toString(), "i2pd")
            if (!Files.exists(datadir)) {
                datadir.createDirectories()
            }

            val processB = ProcessBuilder(exepath.toString(), "--datadir=$datadir")
            processB.directory(File(libDir.toString()))
            processB.redirectErrorStream(true)

            val process = processB.start()
            return process
        }

    }

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
            builder.setOuisyncPageIndex("https://ouisync.net/r#AwEgz-AdVVaLplUYM90raG36ybZVcZc6OzQmDouF1-R4NU8gB8Ztn86vM6ydVMPpjLluBzwQlWlSSrdyfvKTZy9I9XA?name=page-index")
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

    lateinit var background: OuinetBackground
    fun setBackground(ctx: Context) {
        //startI2PD(ctx)
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
