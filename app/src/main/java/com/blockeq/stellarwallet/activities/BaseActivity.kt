package com.blockeq.stellarwallet.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.flowcontrollers.PinFlowController
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.models.PinViewState
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DebugPreferencesHelper

abstract class BaseActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()

        val pinDisabled = this !is LaunchActivity && DebugPreferencesHelper(applicationContext).isPinDisabled
        if (WalletApplication.appReturnedFromBackground && !pinDisabled) {
            WalletApplication.appReturnedFromBackground =  false

            if (!WalletApplication.localStore.encryptedPhrase.isNullOrEmpty()
                    && !WalletApplication.localStore.stellarAccountId.isNullOrEmpty()) {
                launchPINView(PinType.LOGIN, "", "", null, true)
            } else {
                AccountUtils.wipe(this)
            }
        }
    }


    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, passphrase: String?, isLogin: Boolean) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic, passphrase)
        PinFlowController.launchPinActivity(this, pinViewState, isLogin)
    }

    fun launchWallet() {
        val intent = Intent(this, WalletActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PinActivity.PIN_REQUEST_CODE) {
            if (resultCode == PinActivity.SUCCESS_VOID) {
               launchWallet()
            }
        }
    }

    //endregion
}
