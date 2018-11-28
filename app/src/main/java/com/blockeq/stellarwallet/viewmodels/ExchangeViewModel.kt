package com.blockeq.stellarwallet.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.blockeq.stellarwallet.models.ExchangeApiModel

class ExchangeViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private val appContext: Context = application.applicationContext
    private var localList : List<ExchangeEntity>

    private val repository : ExchangeRepository = ExchangeRepository(application)
    val exchangeList = MutableLiveData<List<ExchangeApiModel>>()

    fun getExchangeAddress(address: String): ExchangeEntity? {
        return localList.find { it -> it.address == address  }
    }

    init {
        localList = repository.getAllExchangeProviders()
//        loadExchangeProviderAddresses()
    }

}