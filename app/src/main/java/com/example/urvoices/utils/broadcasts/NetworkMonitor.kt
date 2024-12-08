package com.example.urvoices.utils.broadcasts

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class NetworkMonitor(private val context: Context) {

	private val connectivityManager =
		context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

	fun registerCallback(onNetworkChange: (isConnected: Boolean) -> Unit) {
		val networkCallback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				// Connected
				onNetworkChange(true)
			}

			override fun onLost(network: Network) {
				// Lost connection
				onNetworkChange(false)
			}
		}

		connectivityManager.registerDefaultNetworkCallback(networkCallback)
	}

	fun unregisterCallback() {
		connectivityManager.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
	}

	fun isNetworkConnected(): Boolean {
		val activeNetwork = connectivityManager.activeNetwork ?: return false
		val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
		return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
	}
}