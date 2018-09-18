package blockeq.com.stellarwallet.activities

import android.support.v7.app.AppCompatActivity
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState

open class BaseActivity : AppCompatActivity() {

    open fun setupUI() {
        throw IllegalStateException("Please override this function.")
    }

    override fun onResume() {
        super.onResume()

        if (WalletApplication.appReturnedFromBackground) {
            WalletApplication.appReturnedFromBackground =  false

            val data = WalletApplication.localStore!!.encryptedPhrase

            if (data != null && !data.isEmpty()) {
                launchPINView(PinType.CHECK, "", data, true)
            }
        }
    }


    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, isLoginLaunch: Boolean) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic)
        PinFlowController.launchPinActivity(this, pinViewState, isLoginLaunch)
    }

    //endregion
}
