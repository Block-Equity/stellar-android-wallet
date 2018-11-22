package blockeq.com.stellarwallet.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.DiagnosticActivity
import blockeq.com.stellarwallet.models.PinType
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {

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

    override fun onResume() {
        super.onResume()
        setSavedSettings()
    }

    private fun setupUI() {
        val phrase = WalletApplication.localStore.encryptedPhrase!!
        
        viewPhraseButton.setOnClickListener {
            launchPINView(PinType.VIEW_PHRASE, "", phrase, false)
        }

        viewSeedButton.setOnClickListener {
            launchPINView(PinType.VIEW_SEED, "", phrase, false)
        }

        clearWalletButton.setOnClickListener {
            launchPINView(PinType.CLEAR_WALLET, "", phrase, false)
        }

        pinOnSendPaymentsButton.setOnClickListener {
            launchPINView(PinType.TOGGLE_PIN_ON_SENDING, "", phrase, false)
        }

        diagnosticButton.setOnClickListener {
            startActivity(Intent(context, DiagnosticActivity::class.java))
        }
    }

    private fun setSavedSettings() {
        pinOnSendPaymentsButton.isChecked = WalletApplication.localStore.showPinOnSend
    }

}
