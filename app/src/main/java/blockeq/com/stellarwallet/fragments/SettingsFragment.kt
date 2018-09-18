package blockeq.com.stellarwallet.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    //region User Interface

    private fun setupUI() {
        viewPhraseButton.setOnClickListener {
            val phrase = WalletApplication.localStore!!.encryptedPhrase!!
            launchPINView(phrase, PinType.VIEW_PHRASE)
        }

        clearWalletButton.setOnClickListener {
            val phrase = WalletApplication.localStore!!.encryptedPhrase!!
            launchPINView(phrase, PinType.CLEAR_WALLET)
        }
    }

    //endregion

    //region Helper functions
    private fun launchPINView(mnemonic : String, type: PinType) {
        val pinViewState = PinViewState(type, "", "", mnemonic)
        PinFlowController.launchPinActivity(activity!!, pinViewState, false)
    }
    //endregion
}