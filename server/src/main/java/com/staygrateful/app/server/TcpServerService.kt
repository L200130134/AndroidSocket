package com.staygrateful.app.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class TcpServerService : Service() {

    private var mTcpClientHandler: TcpClientHandler? = null
    private var serverSocket: ServerSocket? = null
    private val working = AtomicBoolean(true)
    private val runnable = Runnable {
        var socket: Socket? = null
        try {
            serverSocket = ServerSocket(PORT)
            Log.i(TAG, "Server started port: $PORT")
            while (working.get()) {
                if (serverSocket != null) {
                    socket = serverSocket!!.accept()
                    Log.i(TAG, "New client: $socket")
                    sendMessageToUi(STATE_CODE_CONNECTED)
                    val dataInputStream = DataInputStream(socket.getInputStream())
                    val dataOutputStream = DataOutputStream(socket.getOutputStream())

                    // Use threads for each client to communicate with them simultaneously
                    mTcpClientHandler = TcpClientHandler(dataInputStream, dataOutputStream) {
                        sendMessageToUi(STATE_CODE_READ, it)
                    }
                    mTcpClientHandler?.start()
                } else {
                    Log.e(TAG, "Couldn't create ServerSocket!")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                socket?.close()
            } catch (ex: IOException) {
                Log.e(TAG, "Error 1 : " + e.localizedMessage)
                ex.printStackTrace()
            }
            Log.e(TAG, "Error 2 : " + e.localizedMessage)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (ACTION_WRITE_SERVICE == action) {
                val writerMessage = intent.getStringExtra(KEY_MESSAGE)
                if (writerMessage != null) {
                    write(writerMessage)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        startMeForeground()
        Thread(runnable).start()
    }

    override fun onDestroy() {
        working.set(false)
    }

    private fun write(string: String) {
        try {
            mTcpClientHandler?.writeUTF(string)
        } catch (e: Exception) {
            e.localizedMessage
            Log.e(TAG, "write: $string")
        }
    }

    private fun sendMessageToUi(stateCode: Int, string: String? = null) {
        val intent = Intent(SERVICE_NAME)
        intent.putExtra(KEY_CODE_STATE, stateCode)
        if (string != null) {
            intent.putExtra(KEY_VALUE_STATE, string)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun startMeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val NOTIFICATION_CHANNEL_ID = packageName
            val channelName = "Tcp Server Background Service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Tcp Server is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build()
            startForeground(2, notification)
        } else {
            startForeground(1, Notification())
        }
    }

    companion object {
        val TAG = TcpServerService::class.java.simpleName
        const val PORT = 9876
        const val SERVICE_NAME: String = "service_tcp_server"
        const val KEY_CODE_STATE: String = "service_tcp_code"
        const val KEY_VALUE_STATE: String = "service_tcp_value"
        const val STATE_CODE_READ: Int = 1001
        const val STATE_CODE_CONNECTED: Int = 1002
        private const val ACTION_WRITE_SERVICE = "ACTION_WRITE_SERVICE"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"

        fun write(context: Context, message: String) {
            try {
                val intent = Intent(context, TcpServerService::class.java)
                intent.action = ACTION_WRITE_SERVICE
                intent.putExtra(KEY_MESSAGE, message)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(TAG, "try write:")
            }
        }
    }
}