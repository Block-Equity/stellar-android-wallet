package blockeq.com.stellarwallet

import android.support.multidex.MultiDexApplication
import blockeq.com.stellarwallet.helpers.LocalStore
import blockeq.com.stellarwallet.models.Session
import com.google.gson.Gson


class WalletApplication : MultiDexApplication() {

    init {
        instance = this
    }

    companion object {
        private val PRIVATE_MODE = 0
        private val PREF_NAME = "blockeq.com.stellarwallet.PREFERENCE_FILE_KEY"
        private var instance: WalletApplication? = null

        // Use LocalStore for SharedPreferences
        var localStore: LocalStore? = null

        var session : Session? = null

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