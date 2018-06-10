package com.youzan.mobile.proxy

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CONNECT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.start_proxy).setOnClickListener {
            val intent = VpnService.prepare(this)
            if (intent == null) {
                startProxy()
            } else {
                startActivityForResult(intent, REQUEST_CONNECT)
            }
        }
    }

    fun startProxy() {
        val intent = Intent(this, ProxyService::class.java)
        intent.putExtra(ProxyService.COMMAND, ProxyService.START_COMMAND)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CONNECT) {
            startProxy()
        }

    }
}
