package blockeq.com.stellarwallet.helpers

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import blockeq.com.stellarwallet.WalletApplication

class WalletLifecycleListener : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        WalletApplication.appReturnedFromBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        WalletApplication.appReturnedFromBackground = false
        WalletApplication.userSession.pin = null
    }
}
