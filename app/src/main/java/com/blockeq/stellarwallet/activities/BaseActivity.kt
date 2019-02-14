package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.blockeq.stellarwallet.utils.GlobalGraphHelper
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {
    private val VERIFY_PIN_REQUEST : Int = 0x01

    override fun onResume() {
        super.onResume()
        val askForPin = !DebugPreferencesHelper(applicationContext).isPinDisabled
        if (WalletApplication.appReturnedFromBackground && askForPin){
            WalletApplication.appReturnedFromBackground = false

            if (GlobalGraphHelper.isExistingWallet()) {
                Timber.d("Existing wallet, opening WalletManagerActivity to verify the pin")
                startActivityForResult(WalletManagerActivity.verifyPin(this), VERIFY_PIN_REQUEST)
            } else {
                Timber.d("Bad state, wiping wallet")
                // bad state, let's clean the wallet
                GlobalGraphHelper.wipe(applicationContext)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VERIFY_PIN_REQUEST) {
            when(resultCode) {
                Activity.RESULT_OK -> Timber.d("pin was successful, user will go back to the screen")
                Activity.RESULT_CANCELED -> finish()
            }
        }
    }
}
