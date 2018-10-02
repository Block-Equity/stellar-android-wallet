package blockeq.com.stellarwallet

import android.arch.lifecycle.ProcessLifecycleOwner
import android.support.multidex.MultiDexApplication
import blockeq.com.stellarwallet.helpers.Constants
import blockeq.com.stellarwallet.helpers.LocalStore
import blockeq.com.stellarwallet.helpers.WalletLifecycleListener
import blockeq.com.stellarwallet.models.Session
import blockeq.com.stellarwallet.models.UserSession
import blockeq.com.stellarwallet.utils.AccountUtils
import com.google.gson.Gson
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security


class WalletApplication : MultiDexApplication() {

    private val lifecycleListener: WalletLifecycleListener by lazy {
        WalletLifecycleListener()
    }


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

        var userSession = UserSession()

        var appReturnedFromBackground = false

        fun applicationContext(): WalletApplication {
            return instance!!
        }
    }

    override fun onCreate() {
        super.onCreate()

        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider() as Provider?, 1)

        setupLifecycleListener()

        val sharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        localStore = LocalStore(sharedPreferences!!, Gson())
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle
                .addObserver(lifecycleListener)
    }
}
