package com.blockeq.stellarwallet.mvvm.trading

import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.remote.Horizon
import org.stellar.sdk.responses.OfferResponse
import timber.log.Timber
import java.util.*

object OffersRepository {
    private var liveData = MutableLiveData<ArrayList<OfferResponse>>()
    fun loadOffers(): MutableLiveData<ArrayList<OfferResponse>> {
        return liveData
    }

    fun refresh(){
        Timber.d("refreshing offers")
        Horizon.getOffers(object: Horizon.OnOffersListener {
            override fun onOffers(offers: ArrayList<OfferResponse>) {
                liveData.postValue(offers)
            }

            override fun onFailed(errorMessage: String) {
                Timber.e(errorMessage)
                liveData.postValue(null)
            }
        })
    }
}
