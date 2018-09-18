package blockeq.com.stellarwallet.fragments

import android.app.Fragment
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState

open class BaseFragment : Fragment() {
    //region Helper Functions

    open fun launchLoginPINView(mnemonic : String) {
        val pinViewState = PinViewState(PinType.CHECK, "", "", mnemonic)
        PinFlowController.launchPinActivity(activity, pinViewState, true)
    }

    open fun launchPINView(pinType: PinType, message: String, mnemonic : String) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic)
        PinFlowController.launchPinActivity(activity, pinViewState, true)
    }

    //endregion
}
