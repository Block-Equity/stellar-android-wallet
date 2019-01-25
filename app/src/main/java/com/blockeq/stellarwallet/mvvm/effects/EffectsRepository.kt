package com.blockeq.stellarwallet.mvvm.effects

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.blockeq.stellarwallet.mvvm.account.AccountRepository
import com.blockeq.stellarwallet.mvvm.effects.remote.OnLoadEffects
import com.blockeq.stellarwallet.mvvm.effects.remote.RemoteRepository
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.requests.SSEStream
import org.stellar.sdk.responses.effects.EffectResponse
import timber.log.Timber

class EffectsRepository private constructor(private val remoteRepository: RemoteRepository) {
    private var effectsList: ArrayList<EffectResponse> = ArrayList()
    private var effectListLiveData = MutableLiveData<ArrayList<EffectResponse>>()
    private var eventSource : SSEStream<EffectResponse>? = null
    /**
     * Returns an observable for ALL the effects table changes
     */
    fun loadList(): LiveData<ArrayList<EffectResponse>> {
        fetchEffectsList(object : OnLoadEffects {
            override fun onLoadEffects(result: ArrayList<EffectResponse>?) {
                if (result != null) {
                    notifyLiveData(result)
                }
            }
        })
        return effectListLiveData
    }

    fun clear() {
        effectsList.clear()
    }

    private fun notifyLiveData(data : ArrayList<EffectResponse>){
        effectListLiveData.postValue(data)
    }

    /**
     * Makes a call to the webservice. Keep it private since the view/viewModel should be 100% abstracted
     * from the data sources implementation.
     */
    private fun fetchEffectsList(listener: OnLoadEffects?) {
        var cursor = ""
        if (!effectsList.isEmpty()) {
            cursor = effectsList.last().pagingToken
            listener?.onLoadEffects(effectsList)
        }

        closeStream()
        remoteRepository.getEffects(cursor, 200, object : OnLoadEffects {
            override fun onLoadEffects(result: java.util.ArrayList<EffectResponse>?) {
                Timber.d("fetched ${result?.size} effects from cursor $cursor")
                if (result != null) {
                    if (!result.isEmpty()) {
                        effectsList.addAll(result)
                        // it will notify the ui only in the first call.
                        listener?.onLoadEffects(effectsList)
                        fetchEffectsList(null)
                    } else {
                        Timber.d("Opening the stream")
                        eventSource = remoteRepository.registerForEffects("now", EventListener {
                            Timber.d("Stream response {$it}")
                            effectsList.add(0, it)
                            notifyLiveData(effectsList)
                            //TODO: use stream on account and remove this refresh
                            //refresh account
                            AccountRepository.loadAccount()
                        })
                    }
                }
            }
        })
    }

    fun closeStream() {
        eventSource?.let {
            Timber.d("Closing the stream")
            it.close()
        }
    }

    companion object {

        private var instance: EffectsRepository? = null

        fun getInstance(): EffectsRepository {
            if (instance == null) {
                instance = EffectsRepository(RemoteRepository())
            }
            return instance as EffectsRepository
        }
    }
}