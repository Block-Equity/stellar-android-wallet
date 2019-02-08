package com.blockeq.stellarwallet

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ProcessLifecycleOwner
import android.support.multidex.MultiDexApplication
import com.blockeq.stellarwallet.encryption.PRNGFixes
import com.blockeq.stellarwallet.helpers.LocalStoreImpl
import com.blockeq.stellarwallet.helpers.WalletLifecycleListener
import com.blockeq.stellarwallet.interfaces.WalletStore
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.mvvm.balance.BalanceRepository
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.blockeq.stellarwallet.mvvm.exchange.ExchangeRepository
import com.blockeq.stellarwallet.remote.Horizon
import com.blockeq.stellarwallet.remote.ServerType
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import org.bouncycastle.jce.provider.BouncyCastleProvider
import shadow.okhttp3.OkHttpClient
import timber.log.Timber
import java.security.Provider
import java.security.Security
import java.util.logging.Level
import java.util.logging.Logger

class WalletApplication : MultiDexApplication() {
    companion object {
        // Use LocalStoreImpl for SharedPreferences
        lateinit var wallet: WalletStore

        var userSession = object : UserSession {
            var impl = UserSessionImpl()

            override fun getSessionAsset(): SessionAsset { return impl.getSessionAsset() }
            override fun setSessionAsset(sessionAsset: SessionAsset) {
                impl.setSessionAsset(sessionAsset)
                assetSession.postValue(sessionAsset)
            }
            override fun getPin(): String? { return impl.getPin() }
            override fun setPin(pin: String?) {
                impl.setPin(pin)
                if(pin != null) {
                    BalanceRepository.init()
                }
            }
            override fun setMinimumBalance(minimumBalance: MinimumBalance) { impl.setMinimumBalance(minimumBalance) }
            override fun getMinimumBalance(): MinimumBalance? { return impl.getMinimumBalance() }
        }

        var assetSession : MutableLiveData<SessionAsset> = MutableLiveData()

        var appReturnedFromBackground = false
    }

    override fun onCreate() {
        super.onCreate()
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE

        //removing the default provider coming from Android SDK.
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider() as Provider?)

        PRNGFixes.apply()

        if (DebugPreferencesHelper(applicationContext).isTestNetServerEnabled) {
            Horizon.init(ServerType.TEST_NET)
        } else {
            Horizon.init(ServerType.PROD)
        }

        setupLifecycleListener()

        wallet = BlockEqWallet(LocalStoreImpl(applicationContext))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            Timber.plant(Timber.DebugTree())

            if (DebugPreferencesHelper(applicationContext).isLeakCanaryEnabled) {
                Timber.d("Enabling leak canary")
                if (LeakCanary.isInAnalyzerProcess(this)) {
                    // This process is dedicated to LeakCanary for heap analysis.
                    // You should not init your app in this process.
                    return
                }
                LeakCanary.install(this)
                // Normal app init code...
            } else {
                Timber.d("Leak canary is disabled")
            }
        }

        // exchange providers addresses are not very likely to change but let's refresh them during application startup.
        ExchangeRepository(this).getAllExchangeProviders(true)
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }

    private val lifecycleListener: WalletLifecycleListener by lazy {
        WalletLifecycleListener(applicationContext)
    }
}
