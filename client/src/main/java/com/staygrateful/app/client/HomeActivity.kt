package com.staygrateful.app.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.staygrateful.app.client.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerBroadcast()

        bindView()

        bindEvent()
    }

    private fun bindView() {
        binding.inputPort.setText(TcpClientService.PORT.toString())
    }

    private fun bindEvent() {
        binding.btnSend.setOnClickListener {
            sendMessageToServer()
        }
        binding.btnConnectServer.setOnClickListener {
            startServerServices()
        }
    }

    private fun sendMessageToServer() {
        val log = binding.tvLog.text.toString()
        val text = binding.inputMsg.text.toString()
        binding.tvLog.text = "$log\nClient : $text".trim()
        TcpClientService.write(this, text)
        binding.inputMsg.setText("")
    }

    private fun startServerServices() {
        val ip = binding.inputIpAddress.text.toString()
        val port = binding.inputPort.text.toString().toInt()
        TcpClientService.start(
            this.applicationContext, ip, port
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }

    private fun registerBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mServiceReceiver, IntentFilter(TcpClientService.SERVICE_NAME)
        )
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            mServiceReceiver
        )
    }

    private var mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("Service receive message!")
            if (intent != null) {
                val code = intent.getIntExtra(TcpClientService.KEY_CODE_STATE, 0)
                val data = intent.getSerializableExtra(TcpClientService.KEY_VALUE_STATE)
                if (code == TcpClientService.STATE_CODE_READ) {
                    val text = binding.tvLog.text.toString()
                    binding.tvLog.text = "$text\nServer : $data".trim()
                    /*Toast.makeText(this@HomeActivity,
                        "Read : $data", Toast.LENGTH_SHORT).show()*/
                }
            }
        }
    }
}