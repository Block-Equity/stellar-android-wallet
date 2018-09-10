package blockeq.com.stellarwallet

import android.app.Application
import android.content.SharedPreferences
import blockeq.com.stellarwallet.helpers.LocalStore
import com.google.gson.Gson


class WalletApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private val PRIVATE_MODE = 0
        private val PREF_NAME = "WalletStorage"
        private var sharedPreferences: SharedPreferences? = null
        private var localStore: LocalStore? = null

        private var instance: WalletApplication? = null

        fun applicationContext(): WalletApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = WalletApplication.instance!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        localStore = LocalStore(sharedPreferences!!, Gson())
    }

}