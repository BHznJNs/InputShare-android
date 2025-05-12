package com.bhznjns.inputsharereporter

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.net.LocalServerSocket
import android.net.LocalSocket
import com.bhznjns.inputsharereporter.utils.I18n // Assuming I18n is available
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

const val SERVER_EVENT_KEEPALIVE = 0x00
const val SERVER_EVENT_TOGGLE    = 0x01

const val ABSTRACT_SOCKET_NAME = "inputsharereporter"

const val KEEPALIVE_INTERVAL_MS = 4000L
const val RETRY_INTERVAL_MS = 1000L

class ReporterServer : Service() {
    private val executor = Executors.newFixedThreadPool(2)
    private val uiHandler = Handler(Looper.getMainLooper())
    private var serverSocket: LocalServerSocket? = null
    private var clientSocket: LocalSocket? = null
    private var outputStream: OutputStream? = null
    private val isRunning = AtomicBoolean(false)

    private fun startServer() {
        if (!isRunning.compareAndSet(false, true)) {
            Log.i("ReporterServer", "Server is already running.")
            return
        }

        executor.execute {
            try {
                // Create LocalServerSocket bound to the abstract namespace
                serverSocket = LocalServerSocket(ABSTRACT_SOCKET_NAME)
                Log.i("ReporterServer", "Server started on abstract address: localabstract:$ABSTRACT_SOCKET_NAME")

                // Accept client connection (this is a blocking call)
                clientSocket = serverSocket?.accept()
                Log.i("ReporterServer", "Client connected")
                outputStream = clientSocket?.outputStream

                // Post UI update to main thread
                uiHandler.post { Toast.makeText(this, I18n.choose(listOf(
                    "PC client connected.",
                    "电脑端已连接。",
                )), Toast.LENGTH_SHORT).show() }

                startHeartbeat()
            } catch (e: IOException) {
                // Catch IOException specifically for socket operations
                Log.e("ReporterServer", "Server starting or connection error: ${e.message}")
                // Only retry if the service is still intended to be running
                if (isRunning.get()) {
                    stopServer(true) // Retry connection
                }
            } catch (e: Exception) {
                // Catch other potential exceptions
                Log.e("ReporterServer", "Unexpected server error: ${e.message}")
                if (isRunning.get()) {
                    stopServer(true) // Retry connection
                }
            }
        }
    }

    private fun sendEvent(event: Int): Boolean {
        val data = byteArrayOf(event.toByte())
        return sendBytes(data)
    }

    private fun sendBytes(data: ByteArray): Boolean {
        if (outputStream == null || clientSocket == null || !clientSocket!!.isConnected) {
            Log.e("ReporterServer", "Client is not connected or socket is closed.")
            // If sending fails, assume connection is lost and stop/retry server
            if (isRunning.get()) {
                stopServer(true)
            }
            return false
        }

        try {
            outputStream!!.write(data)
            outputStream!!.flush()
        } catch (e: IOException) {
            // Catch IOException for write/flush errors, indicating connection issue
            Log.e("ReporterServer", "Server sending data error: ${e.message}")
            if (isRunning.get()) {
                stopServer(true) // Retry connection on send error
            }
            return false
        } catch (e: Exception) {
            // Catch other potential exceptions
            Log.e("ReporterServer", "Unexpected sending data error: ${e.message}")
            if (isRunning.get()) {
                stopServer(true) // Retry connection on send error
            }
            return false
        }
        return true
    }

    private fun startHeartbeat() {
        executor.execute {
            while (isRunning.get() && clientSocket != null && clientSocket!!.isConnected) {
                val data = byteArrayOf(SERVER_EVENT_KEEPALIVE.toByte())
                if (!sendBytes(data)) break
                try {
                    Thread.sleep(KEEPALIVE_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    // Thread interrupted, likely during server stop
                    Log.d("ReporterServer", "Heartbeat thread interrupted.")
                    Thread.currentThread().interrupt() // Restore interrupt flag
                    break
                }
            }
            Log.d("ReporterServer", "Heartbeat loop finished.")
        }
    }

    private fun stopServer(retry: Boolean) {
        // Use compareAndSet to ensure only one thread stops the server
        if (!isRunning.compareAndSet(true, false)) {
            Log.w("ReporterServer", "Server is already stopping or stopped.")
            return
        }

        Log.i("ReporterServer", "Attempting to stop server...")
        try {
            // Closing LocalSocket will likely cause read/write operations on it to throw IOException
            clientSocket?.close()
            serverSocket?.close()
            Log.i("ReporterServer", "Server socket resources closed.")
        } catch (e: IOException) {
            Log.e("ReporterServer", "Error closing server resources: ${e.message}")
        } catch (e: Exception) {
            Log.e("ReporterServer", "Unexpected error during server stop: ${e.message}")
        } finally {
            clientSocket = null
            serverSocket = null
            outputStream = null
        }

        // Shutdown executor gracefully
        executor.shutdownNow()
        try {
            // Wait a bit for tasks to terminate
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                Log.w("ReporterServer", "Executor did not terminate in time.")
            }
        } catch (e: InterruptedException) {
            Log.e("ReporterServer", "Interrupted while waiting for executor termination.")
            Thread.currentThread().interrupt() // Restore interrupt flag
        }

        if (!retry) Log.i("ReporterServer", "Server fully stopped (no retry).")
        // when need to reconnect, show the disconnected message
        uiHandler.post { Toast.makeText(this, I18n.choose(listOf(
            "PC client disconnected.",
            "电脑端已断开连接。",
        )), Toast.LENGTH_SHORT).show() }

        uiHandler.postDelayed({
            Log.i("ReporterServer", "Attempting to restart server after delay...")
            startServer()
        }, RETRY_INTERVAL_MS)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ReporterServer", "Service onCreate")
        startServer()
    }

    override fun onDestroy() {
        Log.d("ReporterServer", "Service onDestroy")
        stopServer(false)
        super.onDestroy()
    }

    /* Codes for bind to this service */
    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun sendEvent(event: Int) {
            executor.execute {
                this@ReporterServer.sendEvent(event)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("ReporterServer", "Service onBind")
        // Ensure server is running when bound, in case it was stopped unexpectedly
        if (!isRunning.get()) startServer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("ReporterServer", "Service onUnbind")
        stopServer(false) // Stop server when all clients unbind
        stopSelf()
        return super.onUnbind(intent)
    }
}