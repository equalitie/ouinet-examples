package ie.equalit.ouinet_examples.android_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/*
 * Adapted from https://github.com/rklare89/AndroidVPNExample.git
 */

@SuppressLint("VpnServicePolicy")
class OuinetVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var monitorThread: Thread? = null //Separate thread to update the domain list
    private val flaggedDomains = HashSet<String?>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting VPN Service")

        initializeFlaggedDomains() //Initializes flagged domain list for the service to check against

        //Builds and sets parameters for the VPN.  This particular VPN is set up to act as a sort of network sniffer so it passes network traffic to Google's DNS server
        val builder = Builder()
        builder.setSession("VpnMonitor")
            .addAddress(
                "172.16.0.2",
                30
            ) // Should be safe range.  It will avoid the normal 192.xxx addresses
            .addDnsServer("8.8.8.8") // Google DNS
            .addRoute("8.8.8.8", 32) // Route DNS only

        try {
            val socket = DatagramSocket()
            protect(socket)
            Log.d(TAG, "Socket protected for DNS")
            socket.close()
        } catch (e: Exception) {
            Log.e(TAG, "Socket protect failed: " + e.message)
        }

        try {
            vpnInterface = builder.establish()
            if (vpnInterface != null) {
                Log.d(TAG, "VPN Established - FD: " + vpnInterface?.fd)
                monitorThread = Thread { this.monitorTraffic() }
                monitorThread?.start() ?: Log.e(TAG, "Monitor thread is null, failed to start")
            } else {
                Log.d(TAG, "VPN failed to establish")
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPN start failed: " + e.message)
        }

        return START_STICKY
    }

    private fun initializeFlaggedDomains() {
        flaggedDomains.clear()
        flaggedDomains.add("NotApprovedDomain.com")
        flaggedDomains.add("ceno.app")
        Log.d(TAG, "Flagged domains initialized: $flaggedDomains")
    }


    //This section filters and logs if a flagged domain is accessed.
    private fun monitorTraffic() {
        Log.d(TAG, "Monitoring started")
        try {
            val inChannel: FileChannel =
                FileInputStream(vpnInterface?.fileDescriptor).channel
            val outChannel: FileChannel =
                FileOutputStream(vpnInterface?.fileDescriptor).channel
            val buffer = ByteBuffer.allocate(2048)

            while (!Thread.interrupted()) {
                buffer.clear()
                val bytesRead = inChannel.read(buffer)
                if (bytesRead > 0) {
                    buffer.flip()
                    val packet = ByteArray(bytesRead)
                    buffer.get(packet)
                    val hex: String = bytesToHex(packet)
                    Log.d(TAG, "Raw packet: $hex")

                    val destIp =
                        ((packet[16].toInt() and 0xFF) shl 24) or ((packet[17].toInt() and 0xFF) shl 16) or
                                ((packet[18].toInt() and 0xFF) shl 8) or (packet[19].toInt() and 0xFF)
                    val srcIp =
                        ((packet[12].toInt() and 0xFF) shl 24) or ((packet[13].toInt() and 0xFF) shl 16) or
                                ((packet[14].toInt() and 0xFF) shl 8) or (packet[15].toInt() and 0xFF)
                    var domain: String? = null
                    if (packet[9].toInt() == 17 && destIp == 0x08080808) { // UDP query to 8.8.8.8
                        domain = extractDomain(packet)
                    } else if (packet[9].toInt() == 17 && srcIp == 0x08080808) { // Response from 8.8.8.8
                        Log.d(TAG, "DNS response from 8.8.8.8")
                    }

                    if (domain != null) {
                        Log.d(TAG, "Domain: $domain")
                        synchronized(flaggedDomains) {
                            if (flaggedDomains.contains(domain)) {
                                Log.d(
                                    TAG,
                                    "Flagged Domain Accessed at: " + System.currentTimeMillis() + ": " + domain
                                )
                            }
                        }
                    }

                    buffer.rewind()
                    outChannel.write(buffer)
                    Log.d(TAG, "Bytes written: $bytesRead")
                } else if (bytesRead == -1) {
                    Log.d(TAG, "Input closed")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Monitoring error: " + e.message)
        }
    }

    private fun extractDomain(packet: ByteArray): String? {
        try {
            if (packet.size < 28 || packet[9].toInt() != 17) return null
            val dataStart = (packet[0].toInt() and 0x0F) * 4 + 8
            if (packet.size < dataStart + 12) return null

            val qdCount =
                ((packet[dataStart + 4].toInt() and 0xFF) shl 8) or (packet[dataStart + 5].toInt() and 0xFF)
            if (qdCount < 1) return null

            var pos = dataStart + 12
            val domain = StringBuilder()
            while (pos < packet.size && packet[pos].toInt() != 0) {
                val len = packet[pos].toInt() and 0xFF
                pos++
                if (pos + len > packet.size) return null
                domain.append(String(packet, pos, len, charset("UTF-8")))
                    .append(".")
                pos += len
            }
            if (domain.isNotEmpty()) {
                domain.setLength(domain.length - 1)
                return domain.toString()
                    .lowercase(Locale.getDefault())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Parse error: " + e.message)
        }
        return null
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x ", b))
        }
        return sb.toString()
    }


    //This section cleans up the VPN on closing.
    override fun onDestroy() {
        monitorThread?.interrupt()
        vpnInterface?.let {
            try {
                it.close()
            } catch (e: Exception) {
                Log.e(TAG, "VPN close error: " + e.message)
            }
        }
        Log.d(TAG, "VPN Stopped")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "OuinetVpnService"
    }
}

