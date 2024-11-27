package com.bhznjns.inputsharereporter

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

const val SERVER_EVENT_KEEPALIVE = 0x00
const val SERVER_EVENT_TOGGLE = 0x01

const val SERVER_PORT = 61625
const val KEEPALIVE_INTERVAL_MS = 4000L
const val RETRY_INTERVAL_MS = 1000L

class TcpSocketServer: Service() {
    private val executor = Executors.newFixedThreadPool(2)
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

    fun startServer() {
        isRunning = true

        executor.execute {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                Log.i("Server", "Server started on port: $SERVER_PORT")
                clientSocket = serverSocket?.accept()
                Log.i("Server", "Client connected")

                outputStream = clientSocket?.getOutputStream()
                startHeartbeat()
            } catch (e: Exception) {
                Log.e("Server", "Server starting error: ${e.message}")
                stopServer(true)
            }
        }
    }

    fun sendBytes(data: ByteArray): Boolean {
        if (outputStream == null || clientSocket == null) {
            Log.e("Server", "Client is not connected.")
            return false
        }

        try {
            outputStream!!.write(data)
            outputStream!!.flush()
        } catch (e: Exception) {
            Log.d("Server", "Server sending data error: ${e.message}")
            stopServer(true)
            return false
        }
        return true
    }

    fun startHeartbeat() {
        executor.execute {
            while (isRunning) {
                val data = byteArrayOf(SERVER_EVENT_KEEPALIVE.toByte())
                if (!sendBytes(data)) break
                Thread.sleep(KEEPALIVE_INTERVAL_MS)
            }
        }
    }

    fun stopServer(retry: Boolean) {
        isRunning = false
        try {
            clientSocket?.close()
            serverSocket?.close()
            Log.i("Server", "Server stopped")
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

    inner class LocalBinder : Binder() {
        fun sendBytes(data: ByteArray) {
            executor.execute {
                this@TcpSocketServer.sendBytes(data)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
