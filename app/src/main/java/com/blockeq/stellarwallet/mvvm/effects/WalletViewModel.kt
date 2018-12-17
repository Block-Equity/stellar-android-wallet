package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.effects.remote.RemoteRepository
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*


class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance(RemoteRepository())
    private val accountRepository : AccountRepository = AccountRepository()

    var effectsList: LiveData<ArrayList<EffectResponse>> = effectsRepository.loadList()
    var account : LiveData<AccountResponse> = accountRepository.loadAccount()

}
