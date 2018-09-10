package blockeq.com.stellarwallet

import android.app.Application
import blockeq.com.stellarwallet.helpers.LocalStore
import com.google.gson.Gson


class WalletApplication : Application() {

    init {
        instance = this
    }

    companion object {
        private val PRIVATE_MODE = 0
        private val PREF_NAME = "blockeq.com.stellarwallet.PREFERENCE_FILE_KEY"
        private var instance: WalletApplication? = null

        // Use LocalStore for SharedPreferences
        var localStore: LocalStore? = null

        fun applicationContext(): WalletApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        localStore = LocalStore(sharedPreferences!!, Gson())
    }

}