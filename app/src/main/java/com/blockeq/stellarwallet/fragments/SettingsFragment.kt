package com.blockeq.stellarwallet.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockeq.stellarwallet.BuildConfig
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.activities.*
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DiagnosticUtils
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {
    private lateinit var appContext : Context

    enum class SettingsAction {
        CLEAR_WALLET, TOGGLE_PIN_ON_SENDING
    }

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
            startActivity(WalletManagerActivity.showMnemonic(it.context))
        }

        viewSeedButton.setOnClickListener {
            startActivity(WalletManagerActivity.showSecretSeed(it.context))
        }

        clearWalletButton.setOnClickListener {
            startActivityForResult(WalletManagerActivity.verifyPin(it.context), SettingsAction.CLEAR_WALLET.ordinal)
        }

        pinOnSendPaymentsButton.setOnClickListener {
            startActivityForResult(WalletManagerActivity.verifyPin(it.context), SettingsAction.TOGGLE_PIN_ON_SENDING.ordinal)
        }

        diagnosticButton.setOnClickListener {
            startActivity(Intent(appContext, DiagnosticActivity::class.java))
        }

        privacyPolicyButton.setOnClickListener {
            startActivity(WebViewActivity.newIntent(appContext, getString(R.string.privacy_policy),"https://www.blockeq.com/privacy.html"))
        }

        termsOfServiceButton.setOnClickListener {
            startActivity(WebViewActivity.newIntent(appContext, getString(R.string.terms_of_service), "https://www.blockeq.com/terms.html"))
        }

        if (BuildConfig.DEBUG) {
            debug.visibility = View.VISIBLE
            debug.setOnClickListener {
                startActivity(Intent(it.context, DebugPreferenceActivity::class.java))
            }

        } else {
            debug.visibility = View.GONE
        }

        val appVersion = DiagnosticUtils.getAppVersion()

        @SuppressLint("SetTextI18n")
        appVersionTextView.text = "Version: $appVersion"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            SettingsAction.CLEAR_WALLET.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    wipeAndRestart()
                }
            }

            SettingsAction.TOGGLE_PIN_ON_SENDING.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    WalletApplication.localStore.showPinOnSend = !WalletApplication.localStore.showPinOnSend
                }
            }
        }
    }

    private fun setSavedSettings() {
        pinOnSendPaymentsButton.isChecked = WalletApplication.localStore.showPinOnSend
    }

    private fun wipeAndRestart() {
        activity?.let {
            AccountUtils.wipe(appContext)
            val intent = Intent(activity, LaunchActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

}
