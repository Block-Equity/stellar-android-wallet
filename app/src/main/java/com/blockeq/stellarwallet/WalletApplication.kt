package com.blockeq.stellarwallet

import android.arch.lifecycle.ProcessLifecycleOwner
import android.support.multidex.MultiDexApplication
import com.blockeq.stellarwallet.encryption.PRNGFixes
import com.blockeq.stellarwallet.helpers.LocalStoreImpl
import com.blockeq.stellarwallet.helpers.WalletLifecycleListener
import com.blockeq.stellarwallet.interfaces.WalletStore
import com.blockeq.stellarwallet.models.UserSessionImpl
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.blockeq.stellarwallet.mvvm.exchange.ExchangeRepository
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Provider
import java.security.Security

class WalletApplication : MultiDexApplication() {
    companion object {
        // Use LocalStoreImpl for SharedPreferences
        lateinit var wallet: WalletStore

        var userSession = UserSessionImpl()

        var appReturnedFromBackground = false
    }

    override fun onCreate() {
        super.onCreate()

        //removing the default provider coming from Android SDK.
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider() as Provider?)

        PRNGFixes.apply()

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
