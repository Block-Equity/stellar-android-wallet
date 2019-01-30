package com.blockeq.stellarwallet.utils

import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentActivity
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.LaunchActivity
import com.blockeq.stellarwallet.activities.WalletActivity
import com.blockeq.stellarwallet.encryption.KeyStoreWrapper
import com.blockeq.stellarwallet.mvvm.effects.EffectsRepository

class GlobalGraphHelper {
    companion object {
        fun wipeAndRestart(activity : FragmentActivity) {
           wipe(activity.applicationContext)
           restart(activity)
        }

        fun isExistingWallet() : Boolean {
            return !WalletApplication.wallet.getEncryptedPhrase().isNullOrEmpty()
                    && !WalletApplication.wallet.getStellarAccountId().isNullOrEmpty()
        }

        fun launchWallet(activity : FragmentActivity) {
            val intent = Intent(activity, WalletActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }

        fun wipe(context: Context) : Boolean {
            clearSession()
            EffectsRepository.getInstance().clear()
            val keyStoreWrapper = KeyStoreWrapper(context)
            keyStoreWrapper.clear()
            return WalletApplication.wallet.clearLocalStore()
        }

        fun restart(activity : FragmentActivity){
            val intent = Intent(activity, LaunchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }

        fun clearSession() {
            WalletApplication.userSession.setPin(null)
            EffectsRepository.getInstance().closeStream()
        }
    }
}
