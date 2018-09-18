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
        val data = WalletApplication.localStore!!.encryptedPhrase

        if (data != null && !data.isEmpty()) {
            launchPINView(data)
        }
    }

    override fun onPause() {
        super.onPause()
        WalletApplication.session = null
    }

    //region Helper Functions
    fun launchPINView(mnemonic : String) {
        val pinViewState = PinViewState(PinType.CHECK, "", "", mnemonic)
        PinFlowController.launchPinActivity(this, pinViewState)
    }
    //endregion

}