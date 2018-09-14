package blockeq.com.stellarwallet.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.activities.LoginActivity
import blockeq.com.stellarwallet.activities.PinActivity.Companion.RESULT_FAIL
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {

    var lastClicked : Int? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
        const val PIN_REQUEST_CODE = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK-> {
                    if (lastClicked == clearWalletButton.id) {
                       wipeAndRestart()
                    } else  {
                        // Launch view phrase activity?

                    }
                }
                RESULT_FAIL -> wipeAndRestart()
            }
        }
    }

    //region User Interface

    private fun setupUI() {
        viewPhraseButton.setOnClickListener {
            lastClicked = it.id

        }

        clearWalletButton.setOnClickListener {
            lastClicked = it.id
            val phrase = WalletApplication.localStore!!.encryptedPhrase!!
            launchPINView(phrase)

        }
    }

    //endregion

    //region Helper functions
    private fun launchPINView(mnemonic : String) {
        val pinViewState = PinViewState(PinType.CHECK, "", "", mnemonic)
        PinFlowController.launchPinActivity(activity!!, pinViewState)
    }

    private fun wipeAndRestart() {
        WalletApplication.localStore!!.clearUserData()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
    //endregion
}