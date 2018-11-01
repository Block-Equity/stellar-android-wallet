package blockeq.com.stellarwallet.fragments

import android.support.v4.app.Fragment
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState

abstract class BaseFragment : Fragment() {
    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, isLogin: Boolean) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic, null)
        PinFlowController.launchPinActivity(activity!!, pinViewState, isLogin)
    }

    //endregion
}
