package ie.equalit.ouinet_examples.android_kotlin

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import ie.equalit.ouinet_examples.android_kotlin.components.HttpClient
import ie.equalit.ouinet_examples.android_kotlin.components.Ouinet
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetSocketAddress
import java.net.Proxy

data class OuinetUiState(
    val state: String = "Created",
    var currentUrl: String = "https://ouinet.work",
    val groupsCount: Int = 0,
    val cacheSize: Int = 0,
)

class OuinetViewModel(private val client: Ouinet?, private val http: HttpClient?) : ViewModel() {

  private val mutUiState = MutableStateFlow(OuinetUiState())
  val uiState: StateFlow<OuinetUiState> = mutUiState.asStateFlow()

  init {
    Executors.newFixedThreadPool(1).execute { this.updateState() }
  }

  fun start(context: Context) {
    val toast = Toast.makeText(context, "Starting Ouinet service", Toast.LENGTH_SHORT)
    toast.show()
    client!!.background.startup()
  }

  fun restart(context: Context) {
    val toast = Toast.makeText(context, "Restarting Ouinet service", Toast.LENGTH_SHORT)
    toast.show()
    client!!.background.stop {
      client.background.start()
    }
  }

  fun stop(context: Context) {
    val toast = Toast.makeText(context, "Stopping Ouinet service", Toast.LENGTH_SHORT)
    toast.show()
    client!!.background.stop()
  }

    fun getUrl(context: Context) {
        val endpoint = client!!.background.getProxyEndpoint()
        Log.d(TAG, "Endpoint = ${endpoint?.getAddress()}:${endpoint?.getPort()}")
        val ouinetDir = client.config.ouinetDirectory
        Log.d(TAG, "Ouinet dir = $ouinetDir")
        if (endpoint != null) {
            Log.d(TAG, "Endpoint is not null")
            val ouinetService = Proxy(Proxy.Type.HTTP,
                InetSocketAddress(endpoint.getAddress(), endpoint.getPort()))
            http!!.setClient(ouinetService, ouinetDir)
        }
        val toast = Toast.makeText(context, "Requesting URL: ${uiState.value.currentUrl}", Toast.LENGTH_SHORT)
        toast.show()
        http?.getURL(uiState.value.currentUrl)
    }

    fun clearCache(context: Context) {
        val endpoint = client!!.background.getFrontendEndpoint()
        val toast = Toast.makeText(context, "Clearing cache", Toast.LENGTH_SHORT)
        toast.show()
        http?.getURL("http://${endpoint!!}/?purge_cache=do")
    }

  private fun updateState() {
    while (true) {
      try {
        Thread.sleep(1000)
      } catch (e: InterruptedException) {
        e.printStackTrace()
      }
      val state = client!!.background.getState()
      mutUiState.update { it.copy(state = state) }
    }
  }

    companion object {
        const val TAG = "OuinetViewModel"
    }
}
