package blockeq.com.stellarwallet

import android.arch.lifecycle.ProcessLifecycleOwner
import android.support.multidex.MultiDexApplication
import blockeq.com.stellarwallet.helpers.LocalStore
import blockeq.com.stellarwallet.helpers.WalletLifecycleListener
import blockeq.com.stellarwallet.models.UserSession
import com.facebook.stetho.Stetho
import com.google.gson.Gson
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security


class WalletApplication : MultiDexApplication() {

    private val lifecycleListener: WalletLifecycleListener by lazy {
        WalletLifecycleListener()
    }

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREF_NAME = "blockeq.com.stellarwallet.PREFERENCE_FILE_KEY"

        // Use LocalStore for SharedPreferences
        lateinit var localStore: LocalStore

        var userSession = UserSession()

        var appReturnedFromBackground = false
    }

    override fun onCreate() {
        super.onCreate()

        //removing the default provider coming from Android SDK.
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider() as Provider?)

        setupLifecycleListener()

        val sharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        localStore = LocalStore(sharedPreferences, Gson())

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle
                .addObserver(lifecycleListener)
    }
}
