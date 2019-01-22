package com.blockeq.stellarwallet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast
import com.blockeq.stellarwallet.R

class NetworkUtils(private val context: Context) {

    // Moving this to Horizon.kt
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun displayNoNetwork() {
        Toast.makeText(context, context.getString(R.string.no_network), Toast.LENGTH_SHORT).show()
    }
}