package blockeq.com.stellarwallet.activities

import android.content.Intent
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
                launchPINView(PinType.LOGIN, "", data, true)
            }
        }
    }


    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, isLogin: Boolean) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic)
        PinFlowController.launchPinActivity(this, pinViewState, isLogin)
    }

    fun launchWallet() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    //endregion
}
