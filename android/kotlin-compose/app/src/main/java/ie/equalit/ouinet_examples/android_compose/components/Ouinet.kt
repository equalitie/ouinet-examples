package ie.equalit.ouinet_examples.android_compose.components

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Process as androidProcess
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet_examples.android_compose.BuildConfig
import ie.equalit.ouinet_examples.android_compose.R
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.listDirectoryEntries

class Ouinet (
    private val context : Context
) {

    val dnsProtocols = setOf("https", "plain")
    lateinit var config: Config
    lateinit var background : OuinetBackground

    fun setup (cacheType: String) {
        //startI2PD(context)
        val builder = Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorTlsCert(BuildConfig.INJECTOR_TLS_CERT)
            .setTlsCaCertStoreDir(context.resources.getString(R.string.cacert_file_path))
            .setCacheType(cacheType)
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

        if (cacheType == "ouisync") {
            builder.setOuisyncPageIndex(BuildConfig.OUISYNC_PAGE_INDEX )
        }

        if (cacheType == "bep3-http-over-i2p") {
            val i2pdExe = findI2PDExe(context).toString()
            builder.setI2pBep3Tracker(BuildConfig.I2P_BEP3_TRACKER)
                //.setEnableI2pServiceExt("127.0.0.1:7656")
                .setEnableI2pServiceExe(i2pdExe)
                //.setEnableI2pServiceLib(true)
                .addRequestField("X-Ouinet-Route: PublicInjector Bep3HTTPOverI2P")
                .setDisableOriginAccess(true)
        }
        config = builder.build()
        background = OuinetBackground.Builder(context)
            .setOuinetConfig(config)
            .build()
    }
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

    fun exitServiceProcess() {
        context.getSystemService(ACTIVITY_SERVICE).let { am ->
            (am as ActivityManager).runningAppProcesses?.let { processes ->
                for (process in processes) {
                    if (process.processName.contains("ouinetService")){
                        androidProcess.killProcess(process.pid)
                    }
                }
            }
        }
    }
}