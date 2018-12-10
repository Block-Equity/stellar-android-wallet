package com.blockeq.stellarwallet.mvvm.effects

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*


class EffectsViewModel @JvmOverloads constructor(effectsRepository: EffectsRepository? = null) : ViewModel() {

    private var effectsRepository: EffectsRepository? = null
    private var effectsList: LiveData<ArrayList<EffectResponse>?>? = null

    init {
        if (this.effectsRepository == null) {
            // ViewModel is created per Activity, so instantiate once
            // we know the userId won't change
            if (effectsRepository != null) {
                this.effectsRepository = effectsRepository
            }
        }
    }

    fun getEffectsList(): LiveData<ArrayList<EffectResponse>?> {
        if (effectsList == null) {
            effectsList = MutableLiveData()
            loadEffects()
        }
        return effectsList!!
    }

    private fun loadEffects() {
        // Observe if/when database is created.
        effectsList = effectsRepository!!.loadList()
    }
}
