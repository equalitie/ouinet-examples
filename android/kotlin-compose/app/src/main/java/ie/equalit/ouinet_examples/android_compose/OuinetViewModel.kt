package ie.equalit.ouinet_examples.android_compose

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import ie.equalit.ouinet.OuinetEndpoint
import ie.equalit.ouinet_examples.android_compose.components.HttpClient
import ie.equalit.ouinet_examples.android_compose.components.Ouinet
import ie.equalit.ouinet_examples.android_compose.ext.bytesToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.Executors
import kotlin.system.exitProcess

data class OuinetUiState(
    val state: String = "Created",
    val proxyPort: Int = 0,
    val frontendPort: Int = 0,
    var currentUrl: String = "https://ouinet.work",
    val groupsCount: Int = 0,
    val cacheSize: String = "0",
    val response: String = "",
    val body: String = "",
    var pollingEnabled: Boolean = true,
)

class OuinetViewModel(private val ouinet: Ouinet?, private val http: HttpClient?) : ViewModel() {
    private val mutUiState = MutableStateFlow(OuinetUiState())
    val uiState: StateFlow<OuinetUiState> = mutUiState.asStateFlow()

    init {
        Log.d(TAG,"Initializing OuinetViewModel")
        Executors.newFixedThreadPool(1).execute { this.updateState() }
    }

    fun start(context: Context) {
        val toast = Toast.makeText(context, "Starting Ouinet service", Toast.LENGTH_SHORT)
        toast.show()
        ouinet!!.background.startup()
    }

    fun restart(context: Context) {
        val toast = Toast.makeText(context, "Restarting Ouinet service", Toast.LENGTH_SHORT)
        toast.show()
        ouinet!!.background.stop {
            ouinet.background.start()
        }
    }

    fun shutdown(context: Context) {
        val toast = Toast.makeText(context, "Shutting down Ouinet Tester", Toast.LENGTH_SHORT)
        toast.show()
        ouinet!!.background.shutdown(doClear = false)
        {
            /*  For some reason, exitProcess(0) fails to kill the ouinetService
             *  so use this shutdown method callback to force exit it */
            ouinet.exitServiceProcess()
            exitProcess(0)
        }
    }

    fun isStarted(): Boolean {
        return uiState.value.state == "Started"
    }

    fun isRequestEnabled(): Boolean {
        return isStarted()
    }

    fun isGetEnabled(): Boolean {
        return isStarted()
    }

    fun isClearEnabled(): Boolean {
        return isStarted()
    }

    fun isShutdownEnabled(): Boolean {
        return isStarted()
    }

    fun isPollingSwitchEnabled(): Boolean {
        return isStarted()
    }

    fun isResponseHeaderReady(): Boolean {
        return uiState.value.response != ""
    }

    fun isResponseBodyReady(): Boolean {
        return uiState.value.body != ""
    }

    fun fetchUrl(context: Context) {
        val toast = Toast.makeText(context, "Requesting URL: ${uiState.value.currentUrl}", Toast.LENGTH_SHORT)
        toast.show()
        http?.getURL(uiState.value.currentUrl) { response, body ->
            mutUiState.update { it.copy(response = response, body = body) }
        }
    }

    fun clearCache(context: Context) {
        val endpoint = ouinet!!.background.getFrontendEndpoint()
        val toast = Toast.makeText(context, "Clearing cache", Toast.LENGTH_SHORT)
        toast.show()
        http?.getURL("http://${endpoint!!}/?purge_cache=do") { response, body ->
            mutUiState.update { it.copy(response = response, body = body) }
        }
    }

    private fun updateState() {
      while (true) {
          try {
              Thread.sleep(1000)
          } catch (e: InterruptedException) {
              e.printStackTrace()
          }
          val state = ouinet!!.background.getState()
          mutUiState.update { it.copy(state = state) }
          val proxyEndpoint = ouinet.background.getProxyEndpoint()
          val frontEndpoint = ouinet.background.getFrontendEndpoint()
          if (state == "Started" && proxyEndpoint != null && frontEndpoint != null) {
              mutUiState.update { it.copy(
                  proxyPort = proxyEndpoint.getPort(),
                  frontendPort = frontEndpoint.getPort())
              }
              if (http?.enabled == false) {
                  val ouinetDir = ouinet.config.ouinetDirectory
                  val ouinetService = Proxy(Proxy.Type.HTTP,
                      InetSocketAddress(proxyEndpoint.getAddress(), proxyEndpoint.getPort()))
                  http.setClient(ouinetService, ouinetDir)
              }
              if (uiState.value.pollingEnabled) {
                  updateGroups(frontEndpoint)
                  updateCacheSize(frontEndpoint)
              }
          }
        }
    }
    private fun updateGroups(endpoint: OuinetEndpoint) {
        http!!.getURL("http://${endpoint.getAddress()}:${endpoint.getPort()}/groups.txt") { _, body ->
            val groups = body.reader().readLines()
            mutUiState.update { it.copy(groupsCount = groups.count()) }
        }
    }

    private fun updateCacheSize(endpoint: OuinetEndpoint) {
        http!!.getURL("http://${endpoint.getAddress()}:${endpoint.getPort()}/api/status") { _, body ->
            val statusArr = body.split(",")
            for (entry in statusArr) {
                if (entry.contains("local_cache_size")) {
                    val cacheSize = entry.substring(entry.indexOf(":") + 1)
                    val byteString = cacheSize.toInt().bytesToString()
                    mutUiState.update { it.copy(cacheSize = byteString) }
                }
            }
        }
    }
    companion object {
        const val TAG = "OuinetViewModel"
    }
}
