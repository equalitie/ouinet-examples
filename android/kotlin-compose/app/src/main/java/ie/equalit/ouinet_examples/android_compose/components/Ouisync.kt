package ie.equalit.ouinet_examples.android_compose.components

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.equalitie.ouisync.session.Session
import org.equalitie.ouisync.session.create
import java.io.File

/**
 * Single repo, single file implementation of Ouisync
 */
class Ouisync (
    context: Context
) {
    var session: Session? = null
    private var sessionError by mutableStateOf<String?>(null)
    private var protocolVersion: Long by mutableLongStateOf(0)
    private val rootDir: File? = context.filesDir
    private var configDir: String = "$rootDir/ouinet/ouisync"

    /**
     * Starts Ouisync service and creates a new session.
     * Binds to network interface and adds cache server from settings.
     */
    suspend fun createSession() {
        try {
            session = Session.create(configDir)
            sessionError = null
            session?.let {
                it.bindNetwork(listOf("quic/0.0.0.0:0", "quic/[::]:0"))
                it.addUserProvidedPeers(listOf("quic/51.79.21.142:20209"))
                protocolVersion = it.getCurrentProtocolVersion()
            }
            Log.i(TAG, "Session.create successful")
        } catch (e: Exception) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        } catch (e: java.lang.Error) {
            Log.e(TAG, "Session.create failed", e)
            sessionError = e.toString()
        }
    }

    companion object {
        private const val TAG = "Ouisync"
    }
}