package ie.equalit.ouinet_examples.android_kotlin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import org.cleaninsights.sdk.CleanInsights

class ExampleApp: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        @JvmStatic
        val cleanInsights: CleanInsights by lazy {
            CleanInsights(
                context!!.assets.open("cleaninsights.json").reader().readText(),
                context!!.filesDir)
        }
    }

    override fun onCreate() {
        super.onCreate()

        context = this
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        // Since there's no good callback before the app gets killed, we work around this problem
        // by persisting, when the OS gets annoyed about memory consumption, which is an indicator,
        // that we're soon going to get killed.
        cleanInsights.persist()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        // Since there's no good callback before the app gets killed, we work around this problem
        // by persisting, when the OS gets annoyed about memory consumption, which is an indicator,
        // that we're soon going to get killed.
        cleanInsights.persist()
    }

    override fun onTerminate() {
        super.onTerminate()

        // This only works in emulators, but nevertheless, for completeness sakes.
        cleanInsights.persist()
    }
}
