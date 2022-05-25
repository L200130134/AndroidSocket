package com.staygrateful.app.server.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.lang.Exception
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object UtilsNetwork {

    fun getLocalIpAddress(context: Context): String? {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE)
            if (wifiManager is WifiManager) {
                val wifiInfo = wifiManager.connectionInfo
                val ipInt = wifiInfo.ipAddress
                return InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
                ).hostAddress
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}