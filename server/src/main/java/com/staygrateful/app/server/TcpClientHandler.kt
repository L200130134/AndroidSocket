package com.staygrateful.app.server

import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class TcpClientHandler(
    private val dataInputStream: DataInputStream,
    private val dataOutputStream: DataOutputStream,
    private val onResult: ((String) -> Unit)? = null
) : Thread() {
    override fun run() {
        while (true) {
            try {
                if(dataInputStream.available() > 0){
                    val msg = dataInputStream.readUTF()
                    Log.i(TAG, "Received: $msg")
                    onResult?.let { it(msg) }
                } /*else {
                    Log.e(TAG, "run: data not available")
                }*/
            } catch (e: IOException) {
                Log.e(TAG, "run: error 1 -> ${e.localizedMessage}")
                e.printStackTrace()
                try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    Log.e(TAG, "run: error 1.2 -> ${ex.localizedMessage}")
                    ex.printStackTrace()
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "run: error 2 -> ${e.localizedMessage}")
                e.printStackTrace()
                try {
                    dataInputStream.close()
                    dataOutputStream.close()
                } catch (ex: IOException) {
                    Log.e(TAG, "run: error 2.2 -> ${ex.localizedMessage}")
                    ex.printStackTrace()
                }
            }
        }
    }

    fun writeUTF(string: String) {
        Thread {
            dataOutputStream.writeUTF(string)
        }.start()
    }

    companion object {
        private val TAG = TcpServerService::class.java.simpleName
    }

}