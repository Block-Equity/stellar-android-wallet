package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.MenuItem
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.models.PinType
import kotlinx.android.synthetic.main.activity_recover_wallet.*


class RecoverWalletActivity : BaseActivity() {

    var isRecoveryPhrase = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)

        isRecoveryPhrase = intent.getBooleanExtra("isPhraseRecovery", true)
        setupUI()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIN_REQUEST_CODE) {
            finish()
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

    //region User Interface
    override fun setupUI() {
        setupToolbar()

        if (!isRecoveryPhrase) {
            phraseEditText.keyListener = DigitsKeyListener.getInstance(getString(R.string.stellar_address_alphabet))
        }

        nextButton.setOnClickListener {
            val recoveryString = getMnemonicString()
            val wordCount = getWordCount(recoveryString)
            if (isRecoveryPhrase) {
                if (wordCount == 12 || wordCount == 24) {
                    launchPINView(PinType.CREATE,
                            getString(R.string.please_create_a_pin),
                            recoveryString,
                            false)
                } else {
                    showErrorMessage()
                }
            } else {
                if (wordCount == 1 && recoveryString[0] == 'S') {

                } else {
                    showErrorMessage()
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.recoverToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        supportActionBar!!.title = if (isRecoveryPhrase) {
            getString(R.string.enter_phrase)
        } else {
            getString(R.string.enter_secret_key)
        }
    }

    private fun showErrorMessage() {
        invalidPhraseTextView.visibility = View.VISIBLE
    }

    //endregion

    //region Helper functions
    private fun getWordCount(word : String) : Int {
        return word.split(" ".toRegex()).size
    }

    private fun getMnemonicString() : String {
        return phraseEditText.text.toString().trim()
    }
    //endregions
}
