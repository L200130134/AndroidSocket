package com.staygrateful.app.client

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
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class TcpClientService : Service() {

    private val working = AtomicBoolean(true)
    private var socket: Socket? = null
    private var dataInputStream: DataInputStream? = null
    private var dataOutputStream: DataOutputStream? = null
    private val runnable = Runnable {
        try {
            val ip = InetAddress.getByName(IP)
            socket = Socket(ip, PORT)
            dataInputStream = DataInputStream(socket!!.getInputStream())
            dataOutputStream = DataOutputStream(socket!!.getOutputStream())
            while (working.get()) {
                try {
                    val msg = dataInputStream!!.readUTF()
                    Log.i(TAG, "Received: $msg")
                    sendMessageToUi(STATE_CODE_READ, msg)
                } catch (e: IOException) {
                    e.printStackTrace()
                    try {
                        dataInputStream!!.close()
                        dataOutputStream!!.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    try {
                        dataInputStream!!.close()
                        dataOutputStream!!.close()
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
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
            } else if (ACTION_CLOSE_SERVICE == action) {
                closeSocket()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun closeSocket() {
        working.set(false)
        try {
            dataOutputStream?.close()
        } catch (e: Exception) {
            e.localizedMessage
        }
        try {
            dataInputStream?.close()
        } catch (e: Exception) {
            e.localizedMessage
        }
        try {
            socket?.close()
        } catch (e: Exception) {
            e.localizedMessage
        }
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
        Thread{
            Log.e(TAG, "write: $string")
            try {
                dataOutputStream?.writeUTF(string)
            } catch (e: Exception) {
                e.localizedMessage
                Log.e(TAG, "write error : ${e.localizedMessage}")
            }
        }.start()
    }

    private fun sendMessageToUi(stateCode: Int, string: String?) {
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
            val channelName = "Tcp Client Background Service"
            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Tcp Client is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build()
            startForeground(2, notification)
        } else {
            startForeground(1, Notification())
        }
    }

    companion object {
        val TAG = TcpClientService::class.java.simpleName
        const val SERVICE_NAME: String = "service_tcp_server"
        const val KEY_CODE_STATE: String = "service_tcp_code"
        const val KEY_VALUE_STATE: String = "service_tcp_value"
        const val STATE_CODE_READ: Int = 1001
        const val STATE_CODE_CLOSE: Int = 1002
        private const val ACTION_WRITE_SERVICE = "ACTION_WRITE_SERVICE"
        private const val ACTION_CLOSE_SERVICE = "ACTION_CLOSE_SERVICE"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        var IP = ""
            private set
        var PORT = 9876
            private set

        fun write(context: Context, message: String) {
            try {
                val intent = Intent(context, TcpClientService::class.java)
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

        fun start(ctx: Context, ipAddress: String, port: Int) {
            IP = ipAddress
            PORT = port
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(Intent(ctx, TcpClientService::class.java))
            } else {
                ctx.startService(Intent(ctx, TcpClientService::class.java))
            }
        }

        fun close(ctx: Context) {
            try {
                val intent = Intent(ctx, TcpClientService::class.java)
                intent.action = ACTION_CLOSE_SERVICE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(intent)
                } else {
                    ctx.startService(intent)
                }
            } catch (e : Exception) {
                e.printStackTrace()
                Log.e(TAG, "try write:")
            }
        }
    }
}