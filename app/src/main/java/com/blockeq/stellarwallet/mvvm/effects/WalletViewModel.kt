package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants.Companion.DEFAULT_ACCOUNT_BALANCE
import com.blockeq.stellarwallet.models.AvailableBalance
import com.blockeq.stellarwallet.models.TotalBalance
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.utils.AccountUtils
import org.jetbrains.anko.doAsync
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance()

    private var effectsList: MutableLiveData<ArrayList<EffectResponse>> = MutableLiveData()
    private var walletViewState: MutableLiveData<WalletViewState> = MutableLiveData()

    var account : MutableLiveData<AccountResponse> =  MutableLiveData()

    private var accountResponse: AccountResponse? = null
    private var effectsListResponse: ArrayList<EffectResponse>? = null

    init {
        AccountRepository.loadAccount().observeForever {
            account.postValue(it)
            accountResponse = it
        }
    }

    fun getEffects() : LiveData<ArrayList<EffectResponse>> {
        forceRefresh()
        return effectsList
    }

    fun forceRefresh() {
        doAsync {
            effectsRepository.loadList().observeForever { it ->
                effectsList.postValue(it)
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
        } else {
            val availableBalance = AvailableBalance("XLM", DEFAULT_ACCOUNT_BALANCE)
            val totalAvailableBalance = TotalBalance("Lumens", "XLM", DEFAULT_ACCOUNT_BALANCE)
            walletViewState.postValue(WalletViewState(WalletViewState.WalletStatus.UNKNOWN, getAssetCode(), availableBalance, totalAvailableBalance, null))

        }
    }

    private fun getAssetCode() : String {
        val code =  WalletApplication.userSession.currAssetCode
        return  code
    }

    private fun getAssetName() : String {
        return WalletApplication.userSession.currAssetName
    }

    private fun getAvailableBalance() : AvailableBalance {
        val balance = WalletApplication.wallet.getAvailableBalance()
        return AvailableBalance(getAssetCode(), balance)
    }

    private fun getTotalAssetBalance(): TotalBalance {
        val currAsset = WalletApplication.userSession.currAssetCode
        val assetBalance = AccountUtils.getTotalBalance(currAsset)
        return TotalBalance(getAssetName(), getAssetCode(), assetBalance)
    }
}
