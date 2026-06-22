package ie.equalit.ouinet_examples.android_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException


@SuppressLint("VpnServicePolicy")
class ProxyVpnService : VpnService() {
    private var tunInterface: ParcelFileDescriptor? = null
    @Volatile private var running = false
    private var serverIp: String = ""
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serverIp = intent?.getStringExtra("server_ip") ?: ""
        if (serverIp.isEmpty()) stopSelf()

        //startForegroundNotification()
        startVPN()
        return START_STICKY
    }
    private fun startVPN() {
        val builder = Builder()
        builder.setSession("ThinkSlow VPN")
        builder.addAddress("10.8.0.2", 24)
        builder.addDnsServer("8.8.8.8")
        builder.addRoute("0.0.0.0", 0)
        tunInterface = builder.establish()
        running = true
        Thread { vpnLoop() }.start()
    }
    private fun vpnLoop() {
        val fd = tunInterface?.fileDescriptor ?: return
        val tunIn = FileInputStream(fd)
        val tunOut = FileOutputStream(fd)
        val socket = Socket()
        protect(socket)
        val server = InetSocketAddress(serverIp, 5555)
        val buffer = ByteArray(32767)
        //val packet = DatagramPacket(buffer, buffer.size)

        // Create PrintWriter object for sending messages to server.
        val outSock = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
        //Create BufferedReader object for receiving messages from server.
        val inSock = BufferedReader(InputStreamReader(socket.getInputStream()))

        while (running) {
            val len = tunIn.read(buffer)
            //if (len > 0) socket.send(DatagramPacket(buffer, len, server))
            // read tun into socket
            // reach socket out to tun
            try {
                //socket.receive(packet)
                //tunOut.write(buffer, 0, packet.length)
            } catch (_: SocketTimeoutException) {

            }
        }
    }
    override fun onDestroy() {
        running = false
        tunInterface?.close()
        //stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
