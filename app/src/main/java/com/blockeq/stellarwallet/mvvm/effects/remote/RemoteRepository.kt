package com.blockeq.stellarwallet.mvvm.effects.remote

import com.blockeq.stellarwallet.remote.Horizon
import org.glassfish.jersey.media.sse.EventSource
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.responses.effects.EffectResponse

class RemoteRepository {

    fun getEffects(cursor : String, limit:Int, listener: OnLoadEffects) {
        Horizon.getLoadEffectsTask(cursor, limit, listener).execute()
    }

    fun registerForEffects(cursor : String, listener: EventListener<EffectResponse>) : EventSource? {
        return Horizon.registerForEffects(cursor, listener)
    }
}
