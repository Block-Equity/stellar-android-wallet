package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.WalletState
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.StringFormat.Companion.truncateDecimalPlaces
import org.jetbrains.anko.doAsync
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance()
    private var walletViewState: MutableLiveData<WalletViewState> = MutableLiveData()
    private var accountResponse: AccountResponse? = null
    private var effectsListResponse: ArrayList<EffectResponse>? = null
    private var state: WalletState = WalletState.UPDATING

    init {
        loadAccount(false)
    }

    fun forceRefresh() {
        state = WalletState.UPDATING
        doAsync {
            loadAccount(true)
            effectsRepository.loadList().observeForever { it ->
                var toNotify = true
                effectsListResponse = it
                if (state == WalletState.ACTIVE) {
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
    }

    fun walletViewState(): MutableLiveData<WalletViewState> {
        notifyViewState()
        forceRefresh()
        return walletViewState
    }

    private fun loadAccount(notify: Boolean){
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
                        }
                    }
                    404 -> {
                        accountResponse = null
                        state = WalletState.NOT_FUNDED
                    }
                    else -> {
                        state = WalletState.ERROR
                    }
                }

                if (toNotify) {
                    notifyViewState()
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
}
