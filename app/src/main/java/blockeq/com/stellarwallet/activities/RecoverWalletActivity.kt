package blockeq.com.stellarwallet.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import blockeq.com.stellarwallet.R
import blockeq.com.stellarwallet.activities.PinActivity.Companion.PIN_REQUEST_CODE
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
                startActivityForResult(Intent(this, PinActivity::class.java), PIN_REQUEST_CODE)
                overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
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

    private fun getWordCount(word : String) : Int {
        return word.split(" ".toRegex()).size
    }
}
