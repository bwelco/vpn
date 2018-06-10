package com.youzan.mobile.proxy

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.io.File
import java.util.*
import android.net.VpnService as BaseVpnService

class ProxyService : BaseVpnService() {


    private val VPN_MTU = 1500

    private val PRIVATE_VLAN = "172.19.0.%s"
    val TUN2SOCKS = "libtun2socks.so"

    val processes = GuardedProcessPool()

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
        startForeground(1, getNotification())

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = this.Builder()
                .setConfigureIntent(pendingIntent)
//                .setSession(profile.formattedName)
                .setMtu(VPN_MTU)
                .addAddress(PRIVATE_VLAN.format(Locale.ENGLISH, "1"), 24)
        val conn = builder.establish() ?: return

        val connectFD = conn.fd

        Log.i("admin", "fd = ${connectFD}")
//        val cmd = arrayListOf(File(applicationInfo.nativeLibraryDir, TUN2SOCKS).absolutePath,
//                "--netif-ipaddr", PRIVATE_VLAN.format(Locale.ENGLISH, "2"),
//                "--netif-netmask", "255.255.255.0",
//                "--socks-server-addr", "172.17.20.10:8889",
//                "--tunfd", connectFD.toString(),
//                "--tunmtu", VPN_MTU.toString(),
//                "--sock-path", "sock_path",
//                "--loglevel", "3")
//
//        processes.start(cmd) { sendFd(connectFD) }
    }

    fun stopProxy() {
        Log.i("admin", "stop proxy")
        stopSelf()
    }

    private fun getNotification(): Notification? {
        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel()
                } else {
                    ""
                }

        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle("Env Proxy")
                .setContentText("desc")
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()

        return notification

    }

    private fun sendFd(fd: Int): Boolean {
        if (fd != -1) {
            var tries = 0
            while (tries < 10) {
                Thread.sleep(30L shl tries)
                if (JniHelper.sendFd(fd, File(App.INSTANCE.filesDir, "sock_path").absolutePath) != -1) return true
                tries += 1
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "my_service"
        val channelName = "My Background Service"
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

}