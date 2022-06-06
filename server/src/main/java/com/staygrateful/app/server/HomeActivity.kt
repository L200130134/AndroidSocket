package com.staygrateful.app.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.staygrateful.app.server.databinding.ActivityHomeBinding
import com.staygrateful.app.server.extension.fromJson
import com.staygrateful.app.server.extension.showToast
import com.staygrateful.app.server.model.UserRasPi
import com.staygrateful.app.server.utils.ResourceUtils
import com.staygrateful.app.server.utils.UtilsNetwork

class HomeActivity : AppCompatActivity() {

    private var isConnected: Boolean = false
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerBroadcast()

        bindView()

        bindEvent()
    }

    override fun onResume() {
        super.onResume()
        bindInfoView(isConnected)
    }

    private fun bindView() {
        bindInfoView(isConnected)
    }

    private fun bindInfoView(connected: Boolean) {
        binding.tvInfo.text = String.format(
            "IP Address \t: %s\nPort \t: %s\nStatus \t: %s",
            UtilsNetwork.getLocalIpAddress(this),
            TcpServerService.PORT,
            if (connected) "Connected" else "Not Connected"
        )
    }

    private fun bindEvent() {
        binding.btnStart.setOnClickListener {
            startServerServices()
            binding.btnStart.visibility = View.GONE
        }
        binding.btnSend.setOnClickListener {
            sendMessageToClient()
        }
    }

    private fun sendMessageToClient() {
        val log = binding.tvLog.text.toString()
        val text = binding.inputMsg.text.toString()
        binding.tvLog.text = "$log\nServer : $text".trim()
        TcpServerService.write(this, text)
        binding.inputMsg.setText("")
    }

    private fun startServerServices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, TcpServerService::class.java))
        } else {
            startService(Intent(applicationContext, TcpServerService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }

    private fun registerBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mServiceReceiver, IntentFilter(TcpServerService.SERVICE_NAME)
        )
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            mServiceReceiver
        )
    }

    private var mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val code = intent.getIntExtra(TcpServerService.KEY_CODE_STATE, 0)
                val data = intent.getStringExtra(TcpServerService.KEY_VALUE_STATE)

                if (code == TcpServerService.STATE_CODE_CONNECTED) {
                    isConnected = true
                    bindInfoView(true)
                } else if (code == TcpServerService.STATE_CODE_READ) {
                    val user = data.fromJson(UserRasPi::class.java)
                    if (user != null) {
                        this@HomeActivity.showToast("Received user data : ${user.name}")
                        sendSampleJson()
                    }
                    val text = binding.tvLog.text.toString()
                    binding.tvLog.text = "$text\nClient : $data".trim()
                }
            }
        }
    }

    private fun sendSampleJson() {
        val jsonRaw = ResourceUtils.getString(this, R.raw.weighing_sample)
        TcpServerService.write(this@HomeActivity, jsonRaw)
    }
}