package com.blockeq.stellarwallet.activities

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.models.Diagnostic
import com.blockeq.stellarwallet.models.Values
import com.blockeq.stellarwallet.remote.BlockEqRetrofit
import com.blockeq.stellarwallet.remote.DiagnosticApi
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DiagnosticUtils
import com.blockeq.stellarwallet.utils.StringFormat
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_diagnostic.*
import retrofit2.Call
import retrofit2.Callback
import timber.log.Timber


class DiagnosticActivity : BaseActivity() {

    private var recoveryType : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic)

        setupUI()
    }

    fun setupUI() {
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val isPassphrase = WalletApplication.wallet.getEncryptedPhrase() != null
        recoveryType = getRecoveryType()

        deviceModelTextView.text = DiagnosticUtils.getDeviceName()
        androidVersionTextView.text = DiagnosticUtils.getAndroidVersion()
        localeTextView.text = DiagnosticUtils.getLocale()
        appVersionTextView.text = DiagnosticUtils.getAppVersion()
        publicAddressTextView.text = WalletApplication.wallet.getStellarAccountId()
        passphraseUsedTextView.text = isPassphrase.toString()
        recoveryTypeTextView.text = recoveryType

        sendReportButton.setOnClickListener {
            if (recoveryType.isEmpty()) {
                Toast.makeText(applicationContext, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            } else {
                sendDiagnostic(isPassphrase)
                finish()
            }
        }
    }

    private fun sendDiagnostic(isPassphrase:Boolean) {
        val diagnosticModel = Diagnostic(
                Values(
                        appVersionTextView.text.toString(),
                        deviceModelTextView.text.toString(),
                        localeTextView.text.toString(),
                        "Android ${androidVersionTextView.text}",
                        publicAddressTextView.text.toString(),
                        explanationEditText.text.toString(),
                        isPassphrase,
                        recoveryType)
        )

        val emailBody = "Issue report details:\n" + explanationEditText.text + "\n\nJSON format details:\n\n" + diagnosticModel.toString()

        BlockEqRetrofit.create(DiagnosticApi::class.java).uploadDiagnostic(
                diagnosticModel).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: retrofit2.Response<JsonObject>) {
                val issueId = ((response.body() as JsonObject)["fields"] as JsonObject).get("Report Id").toString()
                callEmailClient(emailBody, issueId)
                Timber.v("diagnostic created id = {$issueId}")
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Timber.e("Error uploading diagnostic")
            }
        })

    }

    private fun getRecoveryType(): String {
        val recoveryType : String
        val encryptedPhrase = WalletApplication.wallet.getEncryptedPhrase()
        val masterKey = AccountUtils.getPinMasterKey(this, WalletApplication.userSession.getPin()!!)

        if (encryptedPhrase != null && masterKey!= null) {
            val decryptedPhrase = AccountUtils.getDecryptedString(encryptedPhrase, masterKey)
            val wordCount = StringFormat.getWordCount(decryptedPhrase)

            recoveryType = when (wordCount) {
                1 -> getString(R.string.recovered_secret_seed)
                12 -> getString(R.string.mnemonic_phrase_12_words)
                24 -> getString(R.string.mnemonic_phrase_24_words)
                else -> "Unknown"
            }

        } else {
            recoveryType = "Unknown"
        }
        return recoveryType
    }

    private fun callEmailClient(emailBody: String, issueId : String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:support@blockeq.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Issue report [$issueId]")
        intent.putExtra(Intent.EXTRA_TEXT, emailBody)

        // fallback component was found in most emulators without email app
        val emailApp = intent.resolveActivity(packageManager)
        val unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback")
        if (emailApp != null && emailApp != unsupportedAction) {
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
                return true
            }
        }
        return false
    }
}
