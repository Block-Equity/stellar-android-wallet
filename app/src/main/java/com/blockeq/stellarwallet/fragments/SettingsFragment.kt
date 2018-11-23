package com.blockeq.stellarwallet.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.DiagnosticActivity
import com.blockeq.stellarwallet.activities.WebViewActivity
import com.blockeq.stellarwallet.models.PinType
import com.blockeq.stellarwallet.utils.DiagnosticUtils
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {
    private lateinit var appContext : Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_settings, container, false)

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext = view.context.applicationContext
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
            startActivity(Intent(appContext, DiagnosticActivity::class.java))
        }

        privacyPolicyButton.setOnClickListener {
            startActivity(WebViewActivity.newIntent(appContext, "Privacy Policy","https://www.blockeq.com/privacy.html"))
        }

        termsOfServiceButton.setOnClickListener {
            startActivity(WebViewActivity.newIntent(appContext, "Terms of Service", "https://www.blockeq.com/terms.html"))
        }

        val versionText = "Version: " + DiagnosticUtils.getAppVersion()
        appVersionTextView.text = versionText
    }

    private fun setSavedSettings() {
        pinOnSendPaymentsButton.isChecked = WalletApplication.localStore.showPinOnSend
    }

}
