package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.BalanceState
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
    private var state: BalanceState = BalanceState.UPDATING

    init {
        loadAccount(false)
    }

    private fun loadAccount(notify: Boolean){
        AccountRepository.loadAccount().observeForever {
            if(it != null) {
                when(it.httpCode) {
                    200 -> {
                        accountResponse = it.accountResponse
                        state = BalanceState.ACTIVE
                    }
                    404 -> {
                        state = BalanceState.NOT_FUNDED
                    } else -> {
                        state = BalanceState.ERROR
                    }
                }

                if (notify) {
                    notifyViewState()
                }
            }
        }
    }

    fun forceRefresh() {
        doAsync {
            if (accountResponse == null) {
                loadAccount(true)
            }
            effectsRepository.loadList().observeForever { it ->
                effectsListResponse = it
                notifyViewState()
            }
        }
    }

    fun walletViewState() :  MutableLiveData<WalletViewState> {
        notifyViewState()
        forceRefresh()
        return walletViewState
    }

    private fun notifyViewState() {
        if (accountResponse != null && effectsListResponse != null) {
            val availableBalance = getAvailableBalance()
            val totalAvailableBalance = getTotalAssetBalance()
            //TODO fix the mutable null issue here
           walletViewState.postValue(WalletViewState(WalletViewState.WalletStatus.ACTIVE, getAssetCode(), availableBalance, totalAvailableBalance, effectsListResponse))
        } else if(state == BalanceState.ERROR) {
            walletViewState.postValue(WalletViewState(WalletViewState.WalletStatus.ERROR, getAssetCode(), null, null, null))
        } else if(state == BalanceState.NOT_FUNDED){
            val availableBalance = AvailableBalance("XLM", DEFAULT_ACCOUNT_BALANCE)
            val totalAvailableBalance = TotalBalance(state, "Lumens", "XLM", DEFAULT_ACCOUNT_BALANCE)
            walletViewState.postValue(WalletViewState(WalletViewState.WalletStatus.UNFUNDED, getAssetCode(), availableBalance, totalAvailableBalance, null))

        }
    }

    private fun getAssetCode() : String {
        return WalletApplication.userSession.currAssetCode
    }

    private fun getAssetName() : String {
        return WalletApplication.userSession.currAssetName
    }

    private fun getAvailableBalance() : AvailableBalance {
        val balance = truncateDecimalPlaces(WalletApplication.wallet.getAvailableBalance())
        return AvailableBalance(getAssetCode(), balance)
    }

    private fun getTotalAssetBalance(): TotalBalance {
        val currAsset = WalletApplication.userSession.currAssetCode
        val assetBalance = truncateDecimalPlaces(AccountUtils.getTotalBalance(currAsset))
        return TotalBalance(BalanceState.ACTIVE, getAssetName(), getAssetCode(), assetBalance)
    }
}
