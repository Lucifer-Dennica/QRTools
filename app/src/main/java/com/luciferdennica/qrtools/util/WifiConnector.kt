package com.luciferdennica.qrtools.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings

object WifiConnector {

    /**
     * Пытается подключиться к сети WiFi.
     * Android 10+ — через WifiNetworkSpecifier.
     * Android 9-  — открывает системный экран WiFi (ручное подключение).
     */
    fun connect(context: Context, data: WifiData, onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(data.ssid)
                if (data.security.uppercase() != "NOPASS" && data.password.isNotEmpty()) {
                    if (data.security.uppercase() == "WEP") {
                        specifierBuilder.setWepKey(data.password)
                    } else {
                        specifierBuilder.setWpa2Passphrase(data.password)
                    }
                }
                val specifier = specifierBuilder.build()

                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build()

                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val cb = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        cm.bindProcessToNetwork(network)
                        onResult(true)
                    }
                    override fun onUnavailable() { onResult(false) }
                }
                cm.requestNetwork(request, cb, 30_000)
            }.onFailure { onResult(false) }
        } else {
            // Android 9- — открыть настройки WiFi
            runCatching {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                onResult(true)
            }.onFailure { onResult(false) }
        }
    }
}
