package com.youzan.mobile.proxy

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.util.Log
import java.util.*
import android.net.VpnService as BaseVpnService

class ProxyService: BaseVpnService() {


    private val VPN_MTU = 1500

    private val PRIVATE_VLAN = "172.19.0.%s"

    companion object {
        const val START_COMMAND = 1
        const val STOP_COMMAND = 2

        const val COMMAND = "command"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            return Service.START_STICKY
        }

        val command = intent.getIntExtra(COMMAND, -1)
        if (command == -1) {
            return Service.START_STICKY
        }

        when (command) {
            START_COMMAND -> startProxy()
            STOP_COMMAND -> stopProxy()
        }

        return Service.START_STICKY
    }

    fun startProxy() {

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = this.Builder()
                .setConfigureIntent(pendingIntent)
//                .setSession(profile.formattedName)
                .setMtu(VPN_MTU)
                .addAddress(PRIVATE_VLAN.format(Locale.ENGLISH, "1"), 24)
        val conn = builder.establish() ?: return

        val connectFD = conn.fd

        Log.i("admin", "connect fd = ${connectFD}")
    }

    fun stopProxy() {

    }

}