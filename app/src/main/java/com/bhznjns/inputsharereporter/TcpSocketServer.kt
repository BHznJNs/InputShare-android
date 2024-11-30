package com.bhznjns.inputsharereporter

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.bhznjns.inputsharereporter.utils.I18n
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors


const val SERVER_EVENT_KEEPALIVE = 0x00
const val SERVER_EVENT_TOGGLE    = 0x01
const val SERVER_EVENT_PAUSE     = 0x02
const val SERVER_EVENT_RESUME    = 0x03

const val SERVER_PORT = 61625
const val KEEPALIVE_INTERVAL_MS = 4000L
const val RETRY_INTERVAL_MS = 1000L

interface ServiceCallback {
    fun getEdgeTogglingEnabled(): Boolean
}

class TcpSocketServer: Service() {
    private val executor = Executors.newFixedThreadPool(2)
    private val uiHandler = Handler(Looper.getMainLooper())
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var outputStream: OutputStream? = null
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        startServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer(false)
    }

    private fun startServer() {
        isRunning = true

        executor.execute {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                Log.i("Server", "Server started on port: $SERVER_PORT")
                clientSocket = serverSocket?.accept()
                Log.i("Server", "Client connected")
                outputStream = clientSocket?.getOutputStream()

                uiHandler.post { Toast.makeText(this, I18n.choose(listOf(
                    "PC client connected.",
                    "电脑端已连接。",
                )), Toast.LENGTH_SHORT).show() }

                // send current edge-toggling state
                val enabled = this.callback?.getEdgeTogglingEnabled()
                val event = if (enabled == true || enabled == null) SERVER_EVENT_RESUME else SERVER_EVENT_PAUSE
                sendEvent(event)

                startHeartbeat()
            } catch (e: Exception) {
                Log.e("Server", "Server starting error: ${e.message}")
                stopServer(true)
            }
        }
    }

    private fun sendEvent(event: Int): Boolean {
        val data = byteArrayOf(event.toByte())
        return sendBytes(data)
    }

    private fun sendBytes(data: ByteArray): Boolean {
        if (outputStream == null || clientSocket == null) {
            Log.e("Server", "Client is not connected.")
            return false
        }

        try {
            outputStream!!.write(data)
            outputStream!!.flush()
        } catch (e: Exception) {
            Log.e("Server", "Server sending data error: ${e.message}")
            stopServer(true)
            return false
        }
        return true
    }

    private fun startHeartbeat() {
        executor.execute {
            while (isRunning) {
                val data = byteArrayOf(SERVER_EVENT_KEEPALIVE.toByte())
                if (!sendBytes(data)) break
                Thread.sleep(KEEPALIVE_INTERVAL_MS)
            }
        }
    }

    private fun stopServer(retry: Boolean) {
        isRunning = false
        try {
            clientSocket?.close()
            serverSocket?.close()
            Log.i("Server", "Server stopped")

            uiHandler.post { Toast.makeText(this, I18n.choose(listOf(
                "PC client disconnected.",
                "电脑端已断开连接。",
            )), Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            Log.e("Server", "Server stopping error: ${e.message}")
        } finally {
            clientSocket = null
            serverSocket = null
            outputStream = null
        }

        if (retry) {
            Thread.sleep(RETRY_INTERVAL_MS)
            startServer()
        }
    }

    private val binder = LocalBinder()
    private var callback: ServiceCallback? = null

    inner class LocalBinder : Binder() {
        fun sendEvent(event: Int) {
            executor.execute {
                this@TcpSocketServer.sendEvent(event)
            }
        }
        fun registerCallback(callback: ServiceCallback) {
            this@TcpSocketServer.callback = callback
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
