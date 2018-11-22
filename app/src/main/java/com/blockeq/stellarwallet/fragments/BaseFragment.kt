package com.blockeq.stellarwallet.fragments

import android.support.v4.app.Fragment
import com.blockeq.stellarwallet.flowcontrollers.PinFlowController
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.models.PinViewState

abstract class BaseFragment : Fragment() {
    //region Helper Functions

    open fun launchPINView(pinType: PinType, message: String, mnemonic: String, isLogin: Boolean) {
        val pinViewState = PinViewState(pinType, message, "", mnemonic, null)
        PinFlowController.launchPinActivity(activity!!, pinViewState, isLogin)
    }

    //endregion
}
