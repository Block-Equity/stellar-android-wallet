package blockeq.com.stellarwallet.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils(private val context: Context) {

    // TODO: Use RxAndroid and improve the overall architecture of network calls
    // Moving this to Horizon.kt
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}