package com.blockeq.stellarwallet.activities

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.models.Diagnostic
import com.blockeq.stellarwallet.models.Fields
import com.blockeq.stellarwallet.utils.AccountUtils
import com.blockeq.stellarwallet.utils.DiagnosticUtils
import com.blockeq.stellarwallet.utils.StringFormat
import kotlinx.android.synthetic.main.activity_diagnostic.*
import org.json.JSONObject


class DiagnosticActivity : BaseActivity() {

    private var recoveryType : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic)

        setupUI()
    }

    fun setupUI() {
        val isPassphrase = WalletApplication.localStore.encryptedPassphrase != null
        recoveryType = getRecoveryType()

        deviceModelTextView.text = DiagnosticUtils.getDeviceName()
        androidVersionTextView.text = DiagnosticUtils.getAndroidVersion()
        localeTextView.text = DiagnosticUtils.getLocale()
        appVersionTextView.text = DiagnosticUtils.getAppVersion()
        publicAddressTextView.text = WalletApplication.localStore.stellarAccountId
        passphraseUsedTextView.text = isPassphrase.toString()
        recoveryTypeTextView.text = recoveryType

        sendReportButton.setOnClickListener {
            if (recoveryType.isEmpty()) {
                Toast.makeText(applicationContext, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)

                val fields = Fields(appVersionTextView.text.toString(), deviceModelTextView.text.toString(),
                        localeTextView.text.toString(), "Android " + androidVersionTextView.text,
                        publicAddressTextView.text.toString(), explanationEditText.text.toString(), isPassphrase, recoveryType)

                val json = Diagnostic(fields).toJSON()

                val emailBody = "Issue report details:\n" + explanationEditText.text + "\n\nJSON format details:\n\n" + json.toString()

                val postRequest = object : JsonObjectRequest(Request.Method.POST,
                        Constants.BLOCKEQ_DIAGNOSTIC_URL, json,
                        Response.Listener {
                            val issueId = (it["fields"] as JSONObject).get("Report Id").toString()
                            callEmailClient(emailBody, issueId)
                        },
                        Response.ErrorListener {
                            Toast.makeText(applicationContext, "Problem sending diagnostic", Toast.LENGTH_SHORT).show()
                        }) {}

                queue.add(postRequest)
                finish()
            }
        }
    }

    private fun getRecoveryType(): String {
        val recoveryType : String
        val encryptedPhrase = WalletApplication.localStore.encryptedPhrase
        val masterKey = AccountUtils.getPinMasterKey(this, WalletApplication.userSession.pin!!)

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
}
