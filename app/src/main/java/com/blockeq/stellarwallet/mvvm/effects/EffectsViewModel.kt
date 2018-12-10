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

    private var effectsList: LiveData<ArrayList<EffectResponse>?>? = null

    fun getEffectsList(): LiveData<ArrayList<EffectResponse>?> {
        if (effectsList == null) {
            effectsList = MutableLiveData()
            loadEffects()
        }
        return effectsList!!
    }

    private fun loadEffects() {
        // Observe if/when database is created.
        effectsList = effectsRepository.loadList()
    }
}
