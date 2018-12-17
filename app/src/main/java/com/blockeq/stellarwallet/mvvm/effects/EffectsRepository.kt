package com.blockeq.stellarwallet.mvvm.effects

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.mvvm.effects.remote.OnLoadEffects
import com.blockeq.stellarwallet.mvvm.effects.remote.RemoteRepository
import org.stellar.sdk.responses.effects.EffectResponse
import java.util.*

/**
 * Tried to implement (https://github.com/JoaquimLey/transport-eta/blob/26ce1a7f4b2dff12c6efa2292531035e70bfc4ae/app/src/main/java/com/joaquimley/buseta/repository/BusRepository.java)
 * While at the same time only using remote, and not local or Room db
 */

class EffectsRepository private constructor(private val remoteRepository: RemoteRepository) {

    private var effectList = MutableLiveData<ArrayList<EffectResponse>>()
    /**
     * Returns an observable for ALL the effects table changes
     */
    fun loadList(): LiveData<ArrayList<EffectResponse>> {
        fetchEffectsList(object : OnLoadEffects {
            override fun onLoadEffects(result: ArrayList<EffectResponse>?) {
                if (result != null) {
                    effectList.postValue(result)
                }
            }
        })
        return effectList
    }

    /**
     * Makes a call to the webservice. Keep it private since the view/viewModel should be 100% abstracted
     * from the data sources implementation.
     */
    private fun fetchEffectsList(listener: OnLoadEffects) {
        remoteRepository.getEffects(listener)
    }

    companion object {

        private var instance: EffectsRepository? = null

        fun getInstance(remoteRepository: RemoteRepository): EffectsRepository {
            if (instance == null) {
                instance = EffectsRepository(remoteRepository)
            }
            return instance!!
        }
    }
}