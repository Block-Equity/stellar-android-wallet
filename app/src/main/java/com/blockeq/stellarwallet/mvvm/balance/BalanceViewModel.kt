package com.blockeq.stellarwallet.mvvm.balance

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.blockeq.stellarwallet.interfaces.BalanceAvailability

class BalanceViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private lateinit var liveData : LiveData<BalanceAvailability>

    fun loadBalance(): LiveData<BalanceAvailability> {
        if (!::liveData.isInitialized) liveData = BalanceRepository.loadBalance()
        return liveData
    }
}
