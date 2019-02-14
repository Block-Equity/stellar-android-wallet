package com.blockeq.stellarwallet.helpers

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.blockeq.stellarwallet.BuildConfig
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.mvvm.effects.EffectsRepository
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.blockeq.stellarwallet.utils.GlobalGraphHelper

class WalletLifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        WalletApplication.appReturnedFromBackground = true

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if (BuildConfig.DEBUG && DebugPreferencesHelper(context).isPinDisabled) {
            // in debug builds it is possible to disable pin in the session
        } else {
            WalletApplication.appReturnedFromBackground = false
            GlobalGraphHelper.clearSession()
        }
    }
}
