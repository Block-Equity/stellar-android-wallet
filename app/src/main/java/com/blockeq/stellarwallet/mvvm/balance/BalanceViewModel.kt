package com.blockeq.stellarwallet.mvvm.balance

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.models.*
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.trading.OffersRepository
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.OfferResponse

class BalanceViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val liveData : MutableLiveData<BalanceAvailability> = MutableLiveData()

    private var accountResponse : AccountResponse? = null
    private var offerList : ArrayList<OfferResponse>? = null
    init {
        AccountRepository.loadAccount().observeForever {
            accountResponse = it
            notifyIfNeeded()
        }
        OffersRepository.loadOffers().observeForever {
            offerList = it
            notifyIfNeeded()
        }
    }

    private fun notifyIfNeeded(){
        accountResponse?.let { offerList?.let { that ->
            liveData.postValue(BalanceAvailability(it, that)) }
        }
    }

    fun loadBalance(): LiveData<BalanceAvailability> {
        notifyIfNeeded()
        return liveData
    }
}
