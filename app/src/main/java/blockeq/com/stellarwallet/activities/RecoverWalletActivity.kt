package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
import blockeq.com.stellarwallet.flowcontrollers.PinFlowController
import blockeq.com.stellarwallet.models.PinType
import blockeq.com.stellarwallet.models.PinViewState
import kotlinx.android.synthetic.main.activity_recover_wallet.*


class RecoverWalletActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)

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

        nextButton.setOnClickListener {
            val wordCount = getWordCount(phraseEditText.text.toString())
            if (wordCount == 12 || wordCount == 24) {
                launchPINView(phraseEditText.text.toString())
            } else {
                showErrorMessage()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.recoverToolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun showErrorMessage() {
        invalidPhraseTextView.visibility = View.VISIBLE
    }

    //endregion

    //region Helper functions
    private fun getWordCount(word : String) : Int {
        return word.split(" ".toRegex()).size
    }

    private fun launchPINView(mnemonic: String) {
        val pinViewState = PinViewState(PinType.CREATE, getString(R.string.please_create_a_pin), "", mnemonic)
        PinFlowController.launchPinActivity(this, pinViewState, false)
    }
    //endregions
}
