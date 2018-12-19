package com.blockeq.stellarwallet.helpers

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import com.blockeq.stellarwallet.BuildConfig
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper

class WalletLifecycleListener(val context: Context) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        WalletApplication.appReturnedFromBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if (BuildConfig.DEBUG && DebugPreferencesHelper(context).isPinDisabled) {
            // in debug builds it is possible to disable pin.
        } else {
            WalletApplication.appReturnedFromBackground = false
            WalletApplication.userSession.pin = null
        }
    }
}
