package com.blockeq.stellarwallet.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import com.blockeq.stellarwallet.R
import com.blockeq.stellarwallet.WalletApplication
import com.blockeq.stellarwallet.utils.DiagnosticUtils
import kotlinx.android.synthetic.main.activity_diagnostic.*
import org.json.JSONObject
import android.content.ComponentName


class DiagnosticActivity : BaseActivity() {

    private var recoveryType : String = ""
    private var isPassphrase : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic)

        setupUI()
    }

    fun setupUI() {
        deviceModelTextView.text = DiagnosticUtils.getDeviceName()
        androidVersionTextView.text = DiagnosticUtils.getAndroidVersion()
        localeTextView.text = DiagnosticUtils.getLocale()
        appVersionTextView.text = DiagnosticUtils.getAppVersion()
        publicAddressTextView.text = WalletApplication.localStore.stellarAccountId

        phraseRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val id = radioGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(id)
            recoveryType = radioButton.text.toString()
        }

        passphraseRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            val id = radioGroup.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(id)
            isPassphrase = (radioButton.text.toString() == getString(R.string.yes))
        }

        sendReportButton.setOnClickListener {
            if (recoveryType.isEmpty() || isPassphrase == null || explanationEditText.text.isEmpty()) {
                Toast.makeText(applicationContext, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show()
            } else {
                val json = JSONObject()
                json.put("device", deviceModelTextView.text)
                json.put("androidVersion", androidVersionTextView.text)
                json.put("locale", localeTextView.text)
                json.put("appVersion", appVersionTextView.text)
                json.put("publicAddress", publicAddressTextView.text)

                json.put("recoveryType", recoveryType)
                json.put("passphrase", isPassphrase!!)
                json.put("detail", explanationEditText.text)

                val emailBody = "Bug report details:\n" + explanationEditText.text + "\n\nJSON format details:\n\n" + json.toString()

                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:hello@com.blockeq")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report")
                intent.putExtra(Intent.EXTRA_TEXT, emailBody)

                // fallback component was found in most emulators without email app
                val emailApp = intent.resolveActivity(packageManager)
                val unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback")
                if (emailApp != null && emailApp != unsupportedAction) {
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }
}
