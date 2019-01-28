package com.blockeq.stellarwallet.mvvm.effects

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Handler
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.WalletState
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.NetworkUtils
import com.blockeq.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import org.jetbrains.anko.doAsync
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val applicationContext : Context = application.applicationContext
    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance()
    private var walletViewState: MutableLiveData<WalletViewState> = MutableLiveData()
    private var accountResponse: AccountResponse? = null
    private var effectsListResponse: ArrayList<EffectResponse>? = null
    private var state: WalletState = WalletState.UPDATING
    private var pollingStarted = false
    private var handler = Handler()
    private var runnableCode : Runnable? = null

    init {
        loadAccount(false)

        effectsRepository.loadList(false).observeForever { it ->
            var toNotify = true
            effectsListResponse = it
            if (state == WalletState.ACTIVE) {
                Timber.d("already active, toNotify false")
                // it was already ACTIVE, let's do not notify again
                toNotify = false
            } else if (it != null && accountResponse != null) {
                state = WalletState.ACTIVE
            }
            if (toNotify) {
                notifyViewState()
            }
        }
    }

    fun forceRefresh() {
        state = WalletState.UPDATING
        doAsync {
            loadAccount(true, false)
            effectsRepository.forceRefresh()
        }
    }

    fun walletViewState(forceRefresh: Boolean ): MutableLiveData<WalletViewState> {
        // it does not need to refresh since polling will try to get an active account
        if (forceRefresh) {
            forceRefresh()
        }
        return walletViewState
    }

    private fun loadAccount(notify: Boolean, isNotWaitingForActive : Boolean = true) {
        var toNotify = notify
        AccountRepository.loadAccount().observeForever {
            if (it != null) {
                when (it.httpCode) {
                    200 -> {
                        accountResponse = it.accountResponse
                        if (state == WalletState.ACTIVE) {
                            // it was already ACTIVE, let's do not notify again
                            toNotify = false
                        } else if (it.accountResponse != null && effectsListResponse != null) {
                            state = WalletState.ACTIVE
                        } else if(!isNotWaitingForActive) {
                            EffectsRepository.getInstance().loadList(true)
                        }
                    }
                    404 -> {
                        accountResponse = null
                        state = WalletState.NOT_FUNDED
                        if (!pollingStarted) {
                            startPolling()
                        }
                    }
                    else -> {
                        state = WalletState.ERROR
                        if (!pollingStarted) {
                            startPolling()
                        }
                    }
                }

                if (toNotify) {
                    if (isNotWaitingForActive || state == WalletState.ACTIVE) {
                        notifyViewState()
                    }
                }
            }
        }
    }

    private fun notifyViewState() {
        val accountId = WalletApplication.wallet.getStellarAccountId()!!
        when(state) {
            WalletState.ACTIVE -> {
                val availableBalance = getAvailableBalance()
                val totalAvailableBalance = getTotalAssetBalance()
                walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.ACTIVE, accountId, getActiveAssetCode(), availableBalance, totalAvailableBalance, effectsListResponse))
            }
            WalletState.ERROR -> {
                walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.ERROR, accountId, getActiveAssetCode(), null, null, null))
            }
            WalletState.NOT_FUNDED -> {
                val availableBalance = AvailableBalance("XLM", DEFAULT_ACCOUNT_BALANCE)
                val totalAvailableBalance = TotalBalance(state, "Lumens", "XLM", DEFAULT_ACCOUNT_BALANCE)
                walletViewState.postValue(WalletViewState(WalletViewState.AccountStatus.UNFUNDED, accountId, getActiveAssetCode(), availableBalance, totalAvailableBalance, null))
            } else -> {
                // nothing
            }
        }
    }

    private fun getActiveAssetCode() : String {
        return WalletApplication.userSession.currAssetCode
    }

    private fun getActiveAssetName() : String {
        return WalletApplication.userSession.currAssetName
    }

    private fun getAvailableBalance() : AvailableBalance {
        val balance = truncateDecimalPlaces(WalletApplication.wallet.getAvailableBalance())
        return AvailableBalance(getActiveAssetCode(), balance)
    }

    private fun getTotalAssetBalance(): TotalBalance {
        val currAsset = WalletApplication.userSession.currAssetCode
        val assetBalance = truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))
        return TotalBalance(WalletState.ACTIVE, getActiveAssetName(), getActiveAssetCode(), assetBalance)
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
            if (state != WalletState.ACTIVE) {
                pollingStarted = true
                Timber.d("starting polling")
                runnableCode = object : Runnable {
                    override fun run() {
                        Timber.d("starting pulling cycle")
                        when {
                            state == WalletState.ACTIVE -> { Timber.d("polling cancelled") ; return }
                            NetworkUtils(applicationContext).isNetworkAvailable() -> loadAccount(true, false)
                        }
                        handler.postDelayed(this, 3000)
                    }
                }
                handler.post(runnableCode)
            }
        }
    }
}
