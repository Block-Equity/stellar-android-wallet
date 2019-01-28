package com.blockeq.stellarwallet.mvvm.exchange

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

class ExchangeViewModel(application: Application) : AndroidViewModel(application) {
    private var localList : List<ExchangeEntity> = ArrayList()
    private val repository : ExchangeRepository = ExchangeRepository(application)
    private val matchingLiveData : MutableLiveData<ExchangeEntity> = MutableLiveData()
    private var observedAddress : String? = null

    init {
        repository.getAllExchangeProviders().observeForever {
            if (it != null) {
                localList = it
                notifyIfNeeded(observedAddress)
            }
        }
    }

    //TODO: multi-address effectsList support or move view model to only one address support now is hybrid
    fun exchangeMatching(address: String): LiveData<ExchangeEntity> {
        observedAddress = address
        notifyIfNeeded(address)
        return matchingLiveData
    }

    private fun matchExchange(address : String) : ExchangeEntity? {
           return localList.find { it -> it.address == address  }
    }

    private fun notifyIfNeeded(address: String?){
        if (address != null && localList.isNotEmpty()) {
            val exchangeEntity = matchExchange(address)
            if (exchangeEntity != null) {
                matchingLiveData.postValue(exchangeEntity)
            }
        }
    }
}