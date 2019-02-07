package com.blockeq.stellarwallet.mvvm.effects

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Handler
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import com.blockeq.stellarwallet.interfaces.BalanceAvailability
import com.blockeq.stellarwallet.interfaces.StellarAccount
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.balance.BalanceRepository
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import com.blockeq.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import org.jetbrains.anko.doAsync
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber

class WalletViewModelPolling(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val applicationContext : Context = application.applicationContext
    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance()
    private var walletViewState: MutableLiveData<WalletViewState> = MutableLiveData()
    private var stellarAccount: StellarAccount? = null
    private var effectsListResponse: ArrayList<EffectResponse>? = null
    private var state: WalletState = WalletState.UPDATING
    private var pollingStarted = false
    private var handler = Handler()
    private var runnableCode : Runnable? = null
    private var sessionAsset : SessionAsset = DefaultAsset()

    private var balance : BalanceAvailability? = null
    init {
        WalletApplication.assetSession.observeForever {
            if (it != null) {
                sessionAsset = it
                notifyViewState()
            }
        }

        effectsRepository.loadList(false).observeForever {
            Timber.d("effects repository, observer triggered")
            synchronized(this) {
                if (it != null) {
                    effectsListResponse = it
                    if (stellarAccount != null) {
                        state = WalletState.ACTIVE
                        Timber.d("notifying new effects {${it.size}")
                        notifyViewState()
                    }
                }
            }
        }

        BalanceRepository.loadBalance().observeForever {
            balance = it
            if (it != null && effectsListResponse != null) {
                Timber.d("new balance received")
                state = WalletState.ACTIVE
                notifyViewState()
            }
            Timber.d("new balance --> Refreshing Effects")
            EffectsRepository.getInstance().forceRefresh()
        }

        val livData = AccountRepository.loadAccountEvent()
        Timber.d("$livData")
        livData.observeForever {
            if (it != null) {
                when (it.httpCode) {
                    200 -> {
                        Timber.d("Equals = ${stellarAccount?.basicHashCode()==it.stellarAccount.basicHashCode()} ${stellarAccount?.basicHashCode()} vs ${it.stellarAccount.basicHashCode()}")
                        val immutableAccount = stellarAccount
                        if (immutableAccount == null
                                || immutableAccount.basicHashCode() != it.stellarAccount.basicHashCode()
                                || state != WalletState.ACTIVE) {
                            stellarAccount = it.stellarAccount
                            effectsRepository.forceRefresh()
                        } else {
                            //let's ignore this response
                            Timber.d("ignoring account response")
                        }
                    }
                    404 -> {
                        stellarAccount = it.stellarAccount
                        state = WalletState.NOT_FUNDED
                        notifyViewState()
                        if (!pollingStarted) {
                            startPolling()
                        }
                    }
                    else -> {
                        // disabling the ui of ERROR since without pull to refresh makes no sense
                        // state = WalletState.ERROR
                        if (!pollingStarted) {
                            startPolling()
                        }
                    }
                }
            }
        }
    }

    fun forceRefresh() {
        state = WalletState.UPDATING
        doAsync {
            AccountRepository.refresh()
            effectsRepository.forceRefresh()
        }
    }

    fun walletViewState(forceRefresh: Boolean): MutableLiveData<WalletViewState> {
        // it does not need to refresh since polling will try to get an active account
        if (forceRefresh) {
            forceRefresh()
        }
        return walletViewState
    }

    private fun notifyViewState() {
        Timber.d("Notifying state {$state}")
            when(state) {
                WalletState.ACTIVE -> {
                    balance?.let {
                        val availableBalance = getAvailableBalance(it)
                        val totalAvailableBalance = getTotalAssetBalance()
                        walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.ACTIVE, it.getAccountId(),
                                sessionAsset.assetCode, availableBalance, totalAvailableBalance, effectsListResponse))
                    }
                }
                // WalletState.ERROR -> {
                //      walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.ERROR, accountId,  sessionAsset.assetCode, null, null, null))
                // }
                WalletState.NOT_FUNDED -> {
                    val availableBalance = AvailableBalance("XLM", null, DEFAULT_ACCOUNT_BALANCE)
                    val totalAvailableBalance = TotalBalance(state, "Lumens", "XLM", DEFAULT_ACCOUNT_BALANCE)
                    val accountId = WalletApplication.wallet.getStellarAccountId()?:""
                    walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.UNFUNDED, accountId, sessionAsset.assetCode, availableBalance, totalAvailableBalance, null))
                }
                else -> {
                    // nothing
                }
            }
    }

    private fun getAvailableBalance(balance : BalanceAvailability) : AvailableBalance {
        val totalAvailable : String?
        if (sessionAsset.assetCode == "native") {
            totalAvailable = truncateDecimalPlaces(balance.getNativeAssetAvailability().totalAvailable.toString())
        } else {
            totalAvailable = truncateDecimalPlaces(balance.getAssetAvailability(sessionAsset.assetCode, sessionAsset.assetIssuer).totalAvailable.toString())
        }
        return AvailableBalance(sessionAsset.assetCode, sessionAsset.assetIssuer, totalAvailable)
    }

    private fun getTotalAssetBalance(): TotalBalance {
        val currAsset = sessionAsset.assetCode

        val assetBalance = truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))
        return TotalBalance(WalletState.ACTIVE, sessionAsset.assetName, sessionAsset.assetCode, assetBalance)
    }


    fun moveToForeGround() {
        startPolling()
    }

    fun moveToBackground() {
        Timber.d("disabling polling")
        synchronized(this) {
            handler.removeCallbacks(runnableCode)
            pollingStarted = false
        }
    }

    private fun startPolling() {
        if (pollingStarted) return
        synchronized(this) {
                pollingStarted = true
                Timber.d("starting polling")
                runnableCode = object : Runnable {
                    override fun run() {
                        Timber.d("starting pulling cycle")
                        when {
                            NetworkUtils(applicationContext).isNetworkAvailable() -> {
                                AccountRepository.refresh()
                            }
                        }
                        handler.postDelayed(this, 4000)
                    }
                }
                handler.post(runnableCode)
            }
    }
}
