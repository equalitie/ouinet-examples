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
        Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorCredentials(BuildConfig.INJECTOR_CREDENTIALS)
            .setInjectorTlsCert(BuildConfig.INJECTOR_TLS_CERT)
            .setTlsCaCertStoreDir(context.resources.getString(R.string.cacert_file_path))
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

    lateinit var background: OuinetBackground
    fun setBackground(ctx: Context) {
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
