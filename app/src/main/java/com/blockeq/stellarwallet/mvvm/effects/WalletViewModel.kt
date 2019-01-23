package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import org.jetbrains.anko.doAsync
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance()

    private var effectsList: MutableLiveData<ArrayList<EffectResponse>> = MutableLiveData()
    var account : LiveData<AccountResponse> = AccountRepository.loadAccount()

    fun getEffects() : LiveData<ArrayList<EffectResponse>> {
        forceRefresh()
        return effectsList
    }

    fun forceRefresh() {
        doAsync {
            effectsRepository.loadList().observeForever { t -> effectsList.postValue(t) }
        }
    }
}
