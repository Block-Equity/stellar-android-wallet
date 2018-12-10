package com.blockeq.stellarwallet.mvvm.effects.remote

import com.blockeq.stellarwallet.remote.Horizon


class RemoteRepository {

    fun getEffects(listener: OnLoadEffects) {
        Horizon.getLoadEffectsTask(listener).execute()
    }

    companion object {

        private var sInstance: RemoteRepository? = null

        val instance: RemoteRepository
            get() {
                if (sInstance == null) {
                    sInstance = RemoteRepository()
                }
                return sInstance!!
            }
    }
}
