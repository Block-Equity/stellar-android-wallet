package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper
import com.blockeq.stellarwallet.utils.GlobalGraphHelper
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {
    private val VERIFY_PIN_REQUEST : Int = 0x01

    override fun onResume() {
        super.onResume()
        val askForPin = this !is PinActivity && !DebugPreferencesHelper(applicationContext).isPinDisabled
        if (WalletApplication.appReturnedFromBackground && askForPin) {
            WalletApplication.appReturnedFromBackground =  false

            if (isExistingWallet()) {
                Timber.d("Opening WalletManagerActivity to verify the pin")
                startActivityForResult(WalletManagerActivity.verifyPin(this), VERIFY_PIN_REQUEST)
            } else {
                // bad state, let's clean the wallet
                GlobalGraphHelper.wipe(applicationContext)
            }
        }
    }

    //region Helper Functions

    internal fun isExistingWallet() : Boolean {
        return !WalletApplication.wallet.getEncryptedPhrase().isNullOrEmpty()
                && !WalletApplication.wallet.getStellarAccountId().isNullOrEmpty()
    }

    fun launchWallet() {
        val intent = Intent(this, WalletActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VERIFY_PIN_REQUEST) {
            when(resultCode) {
                Activity.RESULT_OK -> launchWallet()
                Activity.RESULT_CANCELED -> finish()
            }
        }
    }

    //endregion
}
