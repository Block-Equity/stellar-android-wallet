package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState

abstract class BaseActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()

        if (WalletApplication.appReturnedFromBackground) {
            WalletApplication.appReturnedFromBackground =  false

            if (!WalletApplication.localStore.encryptedPhrase.isNullOrEmpty()) {
                launchPINView(PinType.LOGIN, "", "", null, true)
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

    //endregion
}
