package com.blockeq.stellarwallet.mvvm.effects.remote

import com.blockeq.stellarwallet.remote.Horizon


class RemoteRepository {

    fun getEffects(listener: OnLoadEffects) {
        Horizon.getLoadEffectsTask(listener).execute()
    }

}
