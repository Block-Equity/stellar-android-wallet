package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.WalletApplication
import blockeq.com.stellarwallet.utils.DiagnosticUtils
import kotlinx.android.synthetic.main.activity_diagnostic.*
import org.json.JSONObject


class DiagnosticActivity : BaseActivity() {

    private var recoveryType : String = ""
    private var isPassphrase : Boolean = false

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
            val json = JSONObject()
            json.put("device", deviceModelTextView.text)
            json.put("androidVersion", androidVersionTextView.text)
            json.put("locale", localeTextView.text)
            json.put("appVersion", appVersionTextView.text)
            json.put("publicAddress", publicAddressTextView.text)

            json.put("recoveryType", recoveryType)
            json.put("passphrase", isPassphrase)
            json.put("detail", explanationEditText.text)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("daniel@blockeq.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Bug report")
            intent.putExtra(Intent.EXTRA_TEXT, json.toString())
            try {
                startActivity(Intent.createChooser(intent, "Send mail..."))
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(applicationContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
