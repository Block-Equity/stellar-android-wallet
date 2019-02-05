package com.blockeq.stellarwallet.mvvm.balance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.interfaces.BalanceAvailability
import com.blockeq.stellarwallet.models.BalanceAvailabilityImpl
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.trading.OffersRepository
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.OfferResponse

object BalanceRepository {
    private var liveData = MutableLiveData<BalanceAvailability>()
    private var accountResponse : AccountResponse? = null
    private var offerList : ArrayList<OfferResponse>? = null

    fun init(){
        AccountRepository.loadAccount().observeForever {
            accountResponse = it
            notifyIfNeeded()
        }
        OffersRepository.loadOffers().observeForever {
            offerList = it
            notifyIfNeeded()
        }
    }

    private fun notifyIfNeeded() {
        accountResponse?.let { offerList?.let { that ->
            liveData.postValue(BalanceAvailabilityImpl(it, that)) }
        }
    }

    fun loadBalance(forceRefresh:Boolean = false): LiveData<BalanceAvailability> {
        if (forceRefresh) {
          refresh()
        }
        return liveData
    }

    fun refresh() {
        accountResponse = null
        offerList = null
        AccountRepository.refresh()
        OffersRepository.refresh()
    }

    fun clear(){
        liveData = MutableLiveData()
        offerList = null
        accountResponse = null
    }
}
