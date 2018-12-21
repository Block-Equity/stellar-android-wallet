package com.blockeq.stellarwallet.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import com.blockeq.stellarwallet.flowcontrollers.PinFlowController
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.models.PinViewState
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper

abstract class BaseActivity : AppCompatActivity() {
    private val VERIFY_PIN_REQUEST : Int = 0x01

    override fun onResume() {
        super.onResume()

        val pinDisabled = this !is LaunchActivity && DebugPreferencesHelper(applicationContext).isPinDisabled
        if (WalletApplication.appReturnedFromBackground && !pinDisabled) {
            WalletApplication.appReturnedFromBackground =  false

            if (!WalletApplication.localStore.encryptedPhrase.isNullOrEmpty()
                    && !WalletApplication.localStore.stellarAccountId.isNullOrEmpty()) {
                startActivityForResult(WalletManagerActivity.verifyPin(this), VERIFY_PIN_REQUEST)
            } else {
                AccountUtils.wipe(this)
            }
        }
    }

    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, passphrase: String?) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic, passphrase)
        PinFlowController.launchPinActivity(this, pinViewState)
    }

    fun launchWallet() {
        val intent = Intent(this, WalletActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VERIFY_PIN_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                launchWallet()
            }
        }
    }

    //endregion
}
