package com.blockeq.stellarwallet.mvvm.effects

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.mvvm.effects.remote.RemoteRepository
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*


class EffectsViewModel(application: Application) : AndroidViewModel(application) {

    private val effectsRepository : EffectsRepository = EffectsRepository.getInstance(RemoteRepository.instance)

    var liveData: LiveData<ArrayList<EffectResponse>> = MutableLiveData()

    fun getEffectsList(): LiveData<ArrayList<EffectResponse>> {
            liveData = MutableLiveData()
            loadEffects()

        return liveData
    }

    private fun loadEffects() {
        // Observe if/when database is created.
        liveData = effectsRepository.loadList()
    }
}
