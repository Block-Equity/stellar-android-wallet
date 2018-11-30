package com.blockeq.stellarwallet.activities

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.helpers.Constants
import com.blockeq.stellarwallet.utils.DiagnosticUtils
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

        deviceModelTextView.text = DiagnosticUtils.getDeviceName()
        androidVersionTextView.text = DiagnosticUtils.getAndroidVersion()
        localeTextView.text = DiagnosticUtils.getLocale()
        appVersionTextView.text = DiagnosticUtils.getAppVersion()
        publicAddressTextView.text = WalletApplication.localStore.stellarAccountId
        passphraseUsedTextView.text = isPassphrase.toString()

        phraseRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            val id = radioGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(id)
            recoveryType = radioButton.text.toString()
        }

        sendReportButton.setOnClickListener {
            if (recoveryType.isEmpty()) {
                Toast.makeText(applicationContext, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            } else {
                val queue = Volley.newRequestQueue(this)
                // Build json body

                val json = JSONObject()
                val fields = JSONObject()

                fields.put("Device Hardware", deviceModelTextView.text)
                fields.put("Platform", "Android " + androidVersionTextView.text)
                fields.put("Locale", localeTextView.text)
                fields.put("App Version", appVersionTextView.text)
                fields.put("Public Wallet Address", publicAddressTextView.text)
                fields.put("Summary", explanationEditText.text)

                fields.put("Wallet Creation Method", recoveryType)
                fields.put("Used Passphrase", isPassphrase)
                fields.put("Battery State", "Charging (20%)")

                json.put("fields", fields)

                val emailBody = "Bug report details:\n" + explanationEditText.text + "\n\nJSON format details:\n\n" + json.toString()

                val postRequest = object : JsonObjectRequest(Request.Method.POST,
                        Constants.BLOCKEQ_DIAGNOSTIC_URL, json,
                        Response.Listener {
                            val issueId = it["id"].toString()
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

    private fun callEmailClient(emailBody: String, issueId : String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:support@com.blockeq")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report [$issueId]")
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
